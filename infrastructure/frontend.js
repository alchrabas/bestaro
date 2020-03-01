const { getDomainAndSubdomain, crawlDirectory } = require('./helpers');

const aws = require('@pulumi/aws');
const pulumi = require('@pulumi/pulumi');

const mime = require('mime');
const path = require('path');

const config = {
    pathToWebsiteContents: '../new-front/build',
    targetDomain: 'mapazwierzat.pl',
};

const contentBucket = new aws.s3.Bucket('bestaro-front',
    {
        bucket: config.targetDomain,
        website: {
            indexDocument: 'index.html',
            errorDocument: '404.html',
        },
    });


const webContentsRootPath = path.join(process.cwd(), config.pathToWebsiteContents);
console.log('Syncing contents from local disk at', webContentsRootPath);
crawlDirectory(
    webContentsRootPath,
    (filePath) => {
        const relativeFilePath = filePath.replace(webContentsRootPath + '/', '');
        const contentFile = new aws.s3.BucketObject(
            relativeFilePath,
            {
                key: relativeFilePath,

                acl: 'public-read',
                bucket: contentBucket,
                contentType: mime.getType(filePath) || undefined,
                source: new pulumi.asset.FileAsset(filePath),
            },
            {
                parent: contentBucket,
            });
    });

const logsBucket = new aws.s3.Bucket('bestaro-front-logs',
    {
        bucket: `${config.targetDomain}-logs`,
        acl: 'private',
    });

const tenMinutes = 60 * 10;


const provisionCertificate = () => {
    const eastRegion = new aws.Provider('east', {
        profile: aws.config.profile,
        region: 'us-east-1', // Per AWS, ACM certificate must be in the us-east-1 region.
    });

    const certificate = new aws.acm.Certificate('certificate', {
        domainName: config.targetDomain,
        validationMethod: 'DNS',
    }, { provider: eastRegion });

    const domainParts = getDomainAndSubdomain(config.targetDomain);
    const hostedZoneId = aws.route53.getZone({ name: domainParts.parentDomain }, { async: true }).then(zone => zone.zoneId);

    const certificateValidationDomain = new aws.route53.Record(`${config.targetDomain}-validation`, {
        name: certificate.domainValidationOptions[0].resourceRecordName,
        zoneId: hostedZoneId,
        type: certificate.domainValidationOptions[0].resourceRecordType,
        records: [certificate.domainValidationOptions[0].resourceRecordValue],
        ttl: tenMinutes,
    });

    const certificateValidation = new aws.acm.CertificateValidation('certificateValidation', {
        certificateArn: certificate.arn,
        validationRecordFqdns: [certificateValidationDomain.fqdn],
    }, { provider: eastRegion });

    return certificateValidation.certificateArn;
};

const certificateArn = provisionCertificate();

// distributionArgs configures the CloudFront distribution. Relevant documentation:
// https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/distribution-web-values-specify.html
// https://www.terraform.io/docs/providers/aws/r/cloudfront_distribution.html
const cdn = new aws.cloudfront.Distribution('cdn', {
    enabled: true,
    aliases: [config.targetDomain],

    origins: [
        {
            originId: contentBucket.arn,
            domainName: contentBucket.websiteEndpoint,
            customOriginConfig: {
                originProtocolPolicy: 'http-only',
                httpPort: 80,
                httpsPort: 443,
                originSslProtocols: ['TLSv1.2'],
            },
        },
        {
            domainName: 'https://hblyf3mk62.execute-api.eu-central-1.amazonaws.com/stage/'.replace(/^https?:\/\/([^/]*).*/, '$1'),
            originId: 'api',
            originPath: '/stage',
            customOriginConfig: {
                originProtocolPolicy: 'https-only',
                httpPort: 80,
                httpsPort: 443,
                originSslProtocols: ['TLSv1.2'],
            }
        }
    ],

    defaultRootObject: 'index.html',

    defaultCacheBehavior: {
        targetOriginId: contentBucket.arn,

        viewerProtocolPolicy: 'redirect-to-https',
        allowedMethods: ['GET', 'HEAD', 'OPTIONS'],
        cachedMethods: ['GET', 'HEAD', 'OPTIONS'],

        forwardedValues: {
            cookies: { forward: 'none' },
            queryString: false,
        },

        minTtl: 0,
        defaultTtl: tenMinutes,
        maxTtl: tenMinutes,
    },

    orderedCacheBehaviors: [
        {
            pathPattern: '/api/*',
            allowedMethods: ['DELETE', 'GET', 'HEAD', 'OPTIONS', 'PATCH', 'POST', 'PUT'],
            cachedMethods: ['HEAD', 'GET', 'OPTIONS'],
            targetOriginId: 'api',

            defaultTtl: 0,
            minTtl: 0,
            maxTtl: 0,

            forwardedValues: {
                queryString: true,
                cookies: {
                    forward: 'all',
                },
            },
            viewerProtocolPolicy: 'redirect-to-https',
        },
    ],

    priceClass: 'PriceClass_100',

    customErrorResponses: [
        {
            errorCode: 404,
            responseCode: 404,
            responsePagePath: '/404.html',
        },
        {
            errorCode: 403,
            responseCode: 200,
            responsePagePath: '/index.html',
        }
    ],

    restrictions: {
        geoRestriction: {
            restrictionType: 'none',
        },
    },

    viewerCertificate: {
        acmCertificateArn: certificateArn,
        sslSupportMethod: 'sni-only',
    },

    loggingConfig: {
        bucket: logsBucket.bucketDomainName,
        includeCookies: false,
        prefix: `${config.targetDomain}/`,
    },
});

function createAliasRecord(targetDomain, distribution) {
    const domainParts = getDomainAndSubdomain(targetDomain);
    const hostedZoneId = aws.route53.getZone({ name: domainParts.parentDomain }, { async: true }).then(zone => zone.zoneId);
    return new aws.route53.Record(
        targetDomain,
        {
            name: domainParts.subdomain,
            zoneId: hostedZoneId,
            type: 'A',
            aliases: [
                {
                    name: distribution.domainName,
                    zoneId: distribution.hostedZoneId,
                    evaluateTargetHealth: true,
                },
            ],
        });
}

const aRecord = createAliasRecord(config.targetDomain, cdn);

module.exports = {
    contentBucketUri: pulumi.interpolate`s3://${contentBucket.bucket}`,
    contentBucketWebsiteEndpoint: contentBucket.websiteEndpoint,
    cloudFrontDomain: cdn.domainName,
    targetDomainEndpoint: `https://${config.targetDomain}/`,
};

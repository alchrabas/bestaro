'use strict';

const aws = require('@pulumi/aws');
const awsx = require('@pulumi/awsx');
const pulumi = require('@pulumi/pulumi');
const moment = require('moment');
const mime = require('mime');
const path = require('path');
const functions = require('./functions');
const { crawlDirectory } = require('./helpers');
const { StaticFrontendWithLambdaBackend } = require('pulumi-s3-lambda-webstack');

const stackName = pulumi.getStack();
const pathToWebsiteContents = '../frontend/build';
const config = new pulumi.Config();
const targetDomain = config.require('targetDomain');

const imagesBucket = new aws.s3.Bucket(`bestaro-images-${stackName}`, {
    acl: 'public-read',
    corsRules: [{
        allowedHeaders: ['*'],
        allowedMethods: [
            'GET',
        ],
        allowedOrigins: ['*'],
        exposeHeaders: ['ETag'],
        maxAgeSeconds: 3000,
    }],
});

const backendUser = new aws.iam.User(`bestaro-backend-user-${stackName}`);

const backendUserAccessKey = new aws.iam.AccessKey('bestaro-backend-access-key', {
    user: backendUser,
});

new aws.iam.UserPolicy('bestaro-backend-user-policy', {
    user: backendUser,
    policy: imagesBucket.bucket.apply((bucketName) => ({
        'Version': '2012-10-17',
        'Statement': [
            {
                'Sid': 'ListObjectsInBucket',
                'Effect': 'Allow',
                'Action': ['s3:ListBucket'],
                'Resource': [`arn:aws:s3:::${bucketName}`],
            },
            {
                'Sid': 'AllObjectActions',
                'Effect': 'Allow',
                'Action': 's3:*Object',
                'Resource': [`arn:aws:s3:::${bucketName}/*`],
            }
        ]
    })),
});


new aws.s3.BucketPolicy('bestaro-public-bucket-policy', {
    bucket: imagesBucket.bucket,
    policy: imagesBucket.bucket.apply((bucketName) => ({
        Version: '2012-10-17',
        Statement: [{
            Effect: 'Allow',
            Principal: '*',
            Action: [
                's3:GetObject'
            ],
            Resource: [
                `arn:aws:s3:::${bucketName}/*`
            ]
        }]
    }))
});

const globalSecondaryIndexesMap = {
    globalEmail: {
        name: 'GlobalRecordId',
        hashKey: 'id',
        projectionType: 'ALL',
        readCapacity: 1,
        writeCapacity: 1,
    },
};

const recordsSchema = {
    name: 'records',
    hashKey: 'yearAndMonth',
    rangeKey: 'geohash',
    attributes: [
        { name: 'yearAndMonth', type: 'N' },
        { name: 'id', type: 'S' },
        { name: 'geohash', type: 'S' },
    ],
    localSecondaryIndexes: [],
    globalSecondaryIndexes: Object.values(globalSecondaryIndexesMap),
    readCapacity: 1,
    writeCapacity: 1,
};

const table = new aws.dynamodb.Table(recordsSchema.name, recordsSchema);

const endpoint = new awsx.apigateway.API('bestaro-frontend-api', {
    routes: [
        {
            path: '/api/upload',
            method: 'POST',
            eventHandler: async (req, ctx) => {
                console.log(req.headers.Authorization);
                const record = getJsonBody(req);
                await functions.addRecord(record, table.name.get());

                return successResponse({ ok: true });
            },
        },
        {
            path: '/api/{minLat}/{minLon}/{maxLat}/{maxLon}/{dateFrom}/{dateTo}/{eventType}',
            method: 'GET',
            eventHandler: async (req, ctx) => {
                const { minLat, minLon, maxLat, maxLon, dateFrom, dateTo, eventType } = req.pathParameters;

                const momentFrom = moment(dateFrom);
                const momentTo = moment(dateTo);
                const markers = await functions.getMarkers(parseFloat(minLat), parseFloat(minLon),
                    parseFloat(maxLat), parseFloat(maxLon),
                    momentFrom, momentTo, eventType, table.name.get());
                console.log(markers);

                return successResponse(markers);
            },
        },
    ]
});

const getJsonBody = (req) => {
    const body = req.isBase64Encoded
        ? Buffer.from(req.body, 'base64').toString('utf8')
        : req.body;
    return JSON.parse(body);
};

const successResponse = (response) => ({
    statusCode: 200,
    body: JSON.stringify(response),
    headers: { 'content-type': 'application/json' },
});


const contentBucket = new aws.s3.Bucket('bestaro-front',
    {
        bucket: targetDomain,
        website: {
            indexDocument: 'index.html',
            errorDocument: '404.html',
        },
    });

const frontendWithBackend = new StaticFrontendWithLambdaBackend('bestaro', targetDomain, contentBucket, endpoint);

const webContentsRootPath = path.join(process.cwd(), pathToWebsiteContents);
console.log('Syncing contents from local disk at', webContentsRootPath);
crawlDirectory(
    webContentsRootPath,
    (filePath) => {
        const relativeFilePath = filePath.replace(webContentsRootPath + '/', '');
        new aws.s3.BucketObject(
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


exports.bucketName = imagesBucket.id;
exports.backendUserAccessKeyId = backendUserAccessKey.id;
exports.backendUserSecretAccessKey = backendUserAccessKey.secret;
exports.regionalDomain = imagesBucket.bucketRegionalDomainName;
exports.api = endpoint.url;
exports.table = table.name;
exports.contentBucketUri = pulumi.interpolate`s3://${contentBucket.bucket}`;
exports.contentBucketWebsiteEndpoint = contentBucket.websiteEndpoint;
exports.cloudFrontDomain = frontendWithBackend.cloudFrontDistribution.domainName;
exports.targetDomainEndpoint = `https://${targetDomain}/`;


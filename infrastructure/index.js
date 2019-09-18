'use strict';
const aws = require('@pulumi/aws');

const imagesBucket = new aws.s3.Bucket('bestaro-images', {
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

const backendUser = new aws.iam.User('bestaro-backend-user');

const backendUserAccessKey = new aws.iam.AccessKey('bestaro-backend-access-key', {
    user: backendUser,
});

const backendUserPolicy = new aws.iam.UserPolicy('bestaro-backend-user-policy', {
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


const publicBucketPolicy = new aws.s3.BucketPolicy('bestaro-public-bucket-policy', {
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


exports.bucketName = imagesBucket.id;
exports.backendUserAccessKeyId = backendUserAccessKey.id;
exports.backendUserSecretAccessKey = backendUserAccessKey.secret;
exports.regionalDomain = imagesBucket.bucketRegionalDomainName;

#!/bin/bash

set -e

PULUMI_ENV=${1:-dev}

pulumi stack select $PULUMI_ENV
pulumi up -y

echo "accessKeyId=$(pulumi stack output backendUserAccessKeyId)
secretAccessKey=$(pulumi stack output backendUserSecretAccessKey)
imagesBucketName=$(pulumi stack output bucketName)" > s3credentials.properties

echo "imagesBucketDomain=https://$(pulumi stack output regionalDomain)" > s3frontend.properties

cd ..

sbt backend/package

# TODO upload and run backend somewhere
# scp infrastructure/s3credentials.properties mapazwierzat:/home/bestaro/

scp infrastructure/s3frontend.properties mapazwierzat:/home/bestaro/

sbt frontend/dist
scp frontend/target/universal/bestaro-frontend-0.1-SNAPSHOT.zip mapazwierzat:/home/bestaro/
ssh mapazwierzat "/home/bestaro/script.sh"


cd new-front
# .env contains google maps api key
npm run build

echo "Copying react frontend to be served as static files"
scp -r build/* mapazwierzat:/home/bestaro/frontend

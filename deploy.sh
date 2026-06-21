#!/bin/bash

set -e

APP_NAME="product-search-assistant"
ENV_NAME="product-search-assistant-env"
BUCKET="product-search-assistant-deploy"
JAR="target/product-search-assistant-0.0.1-SNAPSHOT.jar"
JAR_NAME="product-search-assistant-0.0.1-SNAPSHOT.jar"
VERSION="v$(date +%Y%m%d%H%M%S)"
ZIP="$APP_NAME-$VERSION.zip"

echo "Building JAR..."
./mvnw clean package -DskipTests

echo "Creating deployment bundle with Procfile..."
echo "web: java -Xms256m -Xmx512m -Dserver.port=5000 -jar $JAR_NAME" > Procfile
zip -j $ZIP $JAR Procfile
rm Procfile

echo "Uploading to S3..."
aws s3 cp $ZIP s3://$BUCKET/$ZIPaww
rm $ZIP

echo "Creating application version: $VERSION"
aws elasticbeanstalk create-application-version \
  --application-name $APP_NAME \
  --version-label $VERSION \
  --source-bundle S3Bucket=$BUCKET,S3Key=$ZIP

echo "Deploying to Elastic Beanstalk..."
aws elasticbeanstalk update-environment \
  --environment-name $ENV_NAME \
  --version-label $VERSION

echo "Deploy started."
echo "URL: http://$ENV_NAME.eba-sbkpvm4w.eu-west-1.elasticbeanstalk.com"

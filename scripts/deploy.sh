#!/bin/bash

# Exit immediately if any command fails
set -e

# Variables
BUCKET_NAME=openaq-data-bucket
STACK_NAME=openaq-airquality-stack
REGION=ap-south-1
JAR_PATH=target/spark-processor-1.0-SNAPSHOT.jar
JSON_PATH=src/main/resources/location_sensor_selected.json

# 1. Build Spark JAR
printf "\n📦 Building Spark JAR...\n"
mvn clean package

# 2. Upload files to S3
printf "\n☁️ Uploading files to S3 bucket: $BUCKET_NAME...\n"
aws s3 mb s3://$BUCKET_NAME --region $REGION || echo "Bucket already exists"
aws s3 cp $JAR_PATH s3://$BUCKET_NAME/target/
aws s3 cp $JSON_PATH s3://$BUCKET_NAME/src/main/resources/

# 3. Deploy CloudFormation Stack
printf "\n🚀 Deploying CloudFormation stack: $STACK_NAME...\n"
aws cloudformation deploy \
  --template-file cloudformation/airquality-stack.yaml \
  --stack-name $STACK_NAME \
  --parameter-overrides BucketName=$BUCKET_NAME \
  --capabilities CAPABILITY_NAMED_IAM \
  --region $REGION

# 4. Output Useful Info
printf "\n✅ Deployment Complete.\n"
aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION \
  --query "Stacks[0].Outputs" --output table

printf "\n🧪 You can now manually invoke Lambda via AWS Console or AWS CLI:\n"
printf "aws lambda invoke --function-name TriggerOpenAQJob output.json --region $REGION\n"

#!/bin/bash

set -e

# Load API key from .env
if [ -f .env ]; then
  export $(grep OPENAQ_API_KEY .env | xargs)
else
  echo ".env file not found! Please create one with OPENAQ_API_KEY."
  exit 1
fi

# Define variables
STACK_NAME=openaq-airquality-stack
BUCKET_NAME=openaq-rahul-bucket
REGION=ap-south-1
OUTPUT_PREFIX=output/openaq_latest_data
TIMESTAMP=$(date +%Y%m%d%H%M%S)

##################################
# 1. Build Spark JAR
##################################
echo "Building Spark JAR..."
mvn -f pom.xml clean package

##################################
# 2. Run Spark Job Locally
##################################
echo "🧪 Running Spark job locally..."

# Load .env if available (optional)
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Run the spark-submit command using OPENAQ_API_KEY if available
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
  echo "Detected Windows - running spark-submit using PowerShell..."
  powershell -Command " \
    \$env:OPENAQ_API_KEY='$OPENAQ_API_KEY'; \
    spark-submit --class com.openaq.pipeline.OpenAQLatestFetcher target/spark-processor-1.0-SNAPSHOT.jar \
  "
else
  echo "Running on Unix/Linux..."
  spark-submit \
    --conf "spark.driver.extraJavaOptions=-DOPENAQ_API_KEY=$OPENAQ_API_KEY" \
    --class com.openaq.pipeline.OpenAQLatestFetcher \
    target/spark-processor-1.0-SNAPSHOT.jar
fi


##################################
# 3. Upload Artifacts to S3
##################################
echo "Uploading files to S3 bucket: $BUCKET_NAME..."

#aws s3 cp target/spark-processor-1.0-SNAPSHOT.jar s3://$BUCKET_NAME/target/spark-processor-1.0-SNAPSHOT.jar
#aws s3 cp src/main/resources/location_sensor_selected.json s3://$BUCKET_NAME/src/main/resources/location_sensor_selected.json

# Upload output CSVs with timestamp to avoid overwrite
for file in output/openaq_latest_data/*.csv; do
  aws s3 cp "$file" "s3://$BUCKET_NAME/$OUTPUT_PREFIX/$TIMESTAMP/"
done

##################################
# 4. Deploy CloudFormation Stack
##################################
#echo "🚀 Deploying CloudFormation stack: $STACK_NAME..."
#aws cloudformation deploy \
#  --template-file spark-processor/cloudformation/airquality-stack.yaml \
#  --stack-name $STACK_NAME \
#  --capabilities CAPABILITY_NAMED_IAM \
#  --region $REGION

##################################
# 5. Print Completion
##################################
echo "✅ Deployment Complete. Data uploaded to s3://$BUCKET_NAME/$OUTPUT_PREFIX/$TIMESTAMP/"
echo "   View in AWS QuickSight or trigger Lambda to process via EMR."

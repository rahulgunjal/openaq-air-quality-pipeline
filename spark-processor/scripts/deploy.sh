#!/bin/bash

set -e

# ------------------------
# LOAD ENVIRONMENT VARIABLES FROM .env
# ------------------------
if [ -f .env ]; then
  echo "🔐 Loading environment variables from .env"
  export $(grep -v '^#' .env | xargs)
else
  echo "⚠️  .env file not found! Exiting."
  exit 1
fi

# ------------------------
# CONFIGURATION
# ------------------------
REGION="ap-south-1"
BUCKET="openaq-rahul-bucket-20250629"
STACK_NAME="openaq-airquality-stack"
OUTPUT_PREFIX="output/openaq_latest_data"
JAR_PATH="target/spark-processor-1.0-SNAPSHOT.jar"
CLASS_NAME="com.openaq.pipeline.OpenAQLatestFetcher"
MAPPING_JSON="src/main/resources/location_sensor_selected.json"
TEMPLATE_PATH="cloudformation/airquality-stack.yaml"
OUTPUT_DIR="output/openaq_latest_data"

# ------------------------
# BUILD SPARK JAR LOCALLY
# ------------------------
echo "📦 Building Spark JAR..."
mvn clean package

# ------------------------
# COPY FILES TO S3 (Jar + Mapping)
# ------------------------
echo "☁️ Uploading JAR and mapping JSON to S3..."
aws s3 mb s3://$BUCKET --region $REGION || echo "Bucket already exists"
aws s3 cp $JAR_PATH s3://$BUCKET/target/$(basename $JAR_PATH) --region $REGION
aws s3 cp $MAPPING_JSON s3://$BUCKET/$MAPPING_JSON --region $REGION

# ------------------------
# DEPLOY CLOUDFORMATION STACK
# ------------------------

#echo "🚀 Deploying CloudFormation stack: $STACK_NAME..."
#aws cloudformation deploy \
#  --template-file $TEMPLATE_PATH \
#  --stack-name $STACK_NAME \
#  --capabilities CAPABILITY_NAMED_IAM \
#  --region $REGION

# ------------------------
# RUN SPARK JOB LOCALLY
# ------------------------
echo "🧪 Running Spark job locally..."
spark-submit \
  --class $CLASS_NAME \
  --master "local[*]" \
  --conf "spark.driver.extraJavaOptions=-DOPENAQ_API_KEY=$OPENAQ_API_KEY" \
  $JAR_PATH

# ------------------------
# COPY SPARK OUTPUT TO S3 WITH TIMESTAMP
# ------------------------
echo "📂 Uploading local output to S3 (preserving previous files)..."
TS=$(date +%Y%m%d_%H%M%S)

for f in $OUTPUT_DIR/*; do
  fname=$(basename "$f")
  aws s3 cp "$f" "s3://$BUCKET/$OUTPUT_PREFIX/$TS-$fname" --region $REGION
done

echo "✅ Deployment, local run, and upload complete. You can now connect QuickSight or Athena to s3://$BUCKET/$OUTPUT_PREFIX"

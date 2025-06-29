#!/bin/bash

# === Configuration ===
STACK_NAME="openaq-airquality-stack"
BUCKET_NAME="openaq-rahul-bucket-20250629"
REGION="ap-south-1"

echo "⚠️ WARNING: This will delete your entire stack ($STACK_NAME) and S3 bucket ($BUCKET_NAME)."
read -p "Are you sure you want to continue? (y/n): " CONFIRM

if [[ "$CONFIRM" != "y" ]]; then
  echo "❌ Cleanup aborted by user."
  exit 1
fi

# === Empty S3 Bucket ===
echo "🪣 Emptying S3 bucket: $BUCKET_NAME"
aws s3 rm "s3://$BUCKET_NAME" --recursive --region "$REGION"

# === Delete S3 Bucket ===
echo "🗑️ Deleting S3 bucket: $BUCKET_NAME"
aws s3api delete-bucket --bucket "$BUCKET_NAME" --region "$REGION"

# === Delete CloudFormation Stack ===
echo "🧨 Deleting CloudFormation stack: $STACK_NAME"
aws cloudformation delete-stack --stack-name "$STACK_NAME" --region "$REGION"

echo "⏳ Waiting for stack to be deleted..."
aws cloudformation wait stack-delete-complete --stack-name "$STACK_NAME" --region "$REGION"

echo "✅ Cleanup complete!"

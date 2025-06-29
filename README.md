# 🌍 OpenAQ Air Quality Pipeline using Spark, Scala, and AWS

This project demonstrates a **batch-style air quality alert pipeline** using real-world data from [OpenAQ](https://openaq.org/), built with:
- **Apache Spark** (Scala)
- **Amazon EMR Serverless** to run Spark jobs
- **Amazon S3** for cloud storage
- **Amazon Lambda** to trigger the pipeline
- **Amazon QuickSight** for dashboarding

> 🚀 Designed to simulate a real-time monitoring use case using batch data pulled from a public API and fully automated on AWS.

---

## 🧠 What It Does

✅ Fetches recent air quality data from the OpenAQ API (v3)  
✅ Filters by selected Indian cities (Pune, Mumbai, Delhi, Bengaluru, Aurangabad)  
✅ Stores results in versioned folders in S3 for analytics  
✅ Automates deployment via CloudFormation  
✅ Visualizes results in Amazon QuickSight dashboards

---

## 📂 Project Structure

```bash
openaq-air-quality-pipeline/
├── spark-processor/        # Spark + Scala project to process API data
│   ├── src/main/scala/     # Scala code
│   ├── src/main/resources/ # Input mapping JSON
│   └── target/             # Output JAR & results
├── scripts/                # deploy.sh and cleanup.sh automation scripts
├── cloudformation/         # CloudFormation template for full AWS infra
├── .env                    # API Key (not tracked in Git)
└── README.md               # This file
```

---

## 🛠️ Tech Stack

- Apache Spark 3.x
- Scala 2.12
- Maven
- AWS CloudFormation, S3, Lambda, EMR Serverless, QuickSight
- OpenAQ Public API (v3)

---

## 🤖 AI-Assisted Development

This project significantly leveraged the power of **AI-driven software engineering assistants**:

### 🧠 OpenAI ChatGPT (GPT-4)
Used for:
- Infrastructure-as-Code templating via CloudFormation
- Translating high-level architecture into deployable artifacts
- Generating deploy and cleanup shell scripts with idempotent logic
- Composing and refining project documentation, including this README.md, with consistent formatting and developer-focused clarity.

### 💡 GitHub Copilot
Used in:
- Accelerating Spark + Scala boilerplate code generation
- Suggesting functional transformations on nested JSON fetched via HTTP
- Assisting with Maven POM setup and dependency resolution
- Code completion and LSP-aware inline documentation

> Together, these AI tools enabled **high-throughput development**, **reduced cognitive overhead**, and **accelerated DevOps automation**, exemplifying the synergy between human creativity and LLM-augmented engineering workflows.

---

## 🚀 How to Run This Project

### 1. 🔧 Prerequisites
- AWS CLI installed & configured
- Git Bash or PowerShell
- Java 8 and Spark 3.x installed locally
- Maven (`mvn`) available on PATH
- IAM permissions to create resources via CloudFormation
- QuickSight set up in your AWS region (Mumbai: `ap-south-1`)

---

### 2. 🧪 Local Development & Test

```bash
# Clone the repo
$ git clone https://github.com/yourusername/openaq-air-quality-pipeline.git
$ cd openaq-air-quality-pipeline

# Add your OpenAQ API key in `.env` (not committed to git)
$ echo "OPENAQ_API_KEY=your_api_key_here" > .env

# Build the Spark JAR
$ cd spark-processor
$ mvn clean package

# Run locally via Spark Submit
$ spark-submit --class com.openaq.pipeline.OpenAQLatestFetcher target/spark-processor-1.0-SNAPSHOT.jar
```

---

### 3. ☁️ Deploy to AWS (Automated with `deploy.sh`)

```bash
# Back to project root
$ bash scripts/deploy.sh
```

This script performs the following steps **automatically**:
- Loads your API key from `.env`
- Builds the JAR
- Runs the Spark job locally
- Uploads generated output to S3 (avoids overwriting previous runs)
- Deploys AWS infrastructure using CloudFormation
- Triggers the Spark job via Lambda
- Prepares data for QuickSight dashboarding

> 🔐 Important: `.env` file is used to securely inject your API key. Keep it out of version control!

---

### 4. 📊 View Dashboard in QuickSight

- Open [Amazon QuickSight](https://quicksight.aws.amazon.com/)
- Create a new dataset using S3 as source (point to the output folder in S3)
- Create a dashboard to compare air quality levels across different cities and sensors
- Use timestamp or sensorId to filter and trend the data over time

---

### 5. 🧹 Clean Up AWS Resources with `cleanup.sh`

To delete all AWS resources and **avoid unwanted AWS charges**:

```bash
$ bash scripts/cleanup.sh
```

This script will:
- Delete the CloudFormation stack
- Optionally remove S3 output and logs
- Clean up Lambda, EMR Serverless, IAM roles, etc.

---

## ✅ Status

- [x] Repository created & structured  
- [x] Spark project setup  
- [x] Data fetch & transformation  
- [x] S3 output integration  
- [x] AWS deployment with CloudFormation  
- [x] Lambda trigger integration  
- [x] QuickSight dashboard setup  
- [x] Cleanup automation

---

## 📌 Author

👨‍💻 *This project is built by Rahul Gunjal as a portfolio showcase for data engineering and cloud skills.*

---

> ⭐ Don’t forget to star this repo if you find it helpful or inspiring!

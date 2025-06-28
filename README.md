# openaq-air-quality-pipeline
# 🌍 OpenAQ Air Quality Pipeline using Spark, Scala, and AWS

This project demonstrates a **batch-style air quality alert pipeline** using real-world data from [OpenAQ](https://openaq.org/), built with:
- **Apache Spark** (Scala)
- **Amazon S3** for cloud storage
- **Amazon SNS** for email alerts
- **Amazon QuickSight** for dashboarding

> 🚀 Designed to simulate a real-time monitoring use case using batch data pulled from a public API.

---

## 🧠 What It Does

✅ Fetches recent air quality data from the OpenAQ API (v3)  
✅ Filters by Indian cities (Pune, Mumbai, Delhi, Bengaluru, Aurangabad)  
✅ Detects critical pollution levels (e.g., PM2.5 > 150)  
✅ Sends email alerts if unsafe readings are found (via AWS SNS)  
✅ Stores full + alert data to AWS S3  
✅ Visualizes results in Amazon QuickSight dashboards

---

## 📂 Project Structure

```bash
openaq-air-quality-pipeline/
├── spark-processor/      # Spark + Scala project to process API data
├── infra/                # CloudFormation templates and IAM policies
├── docs/                 # Architecture diagrams, screenshots
└── README.md             # This file

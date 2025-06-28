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
```

---

## 🛠️ Tech Stack

- Apache Spark 3.x
- Scala 2.12
- SBT
- AWS S3, SNS, QuickSight (All Free Tier compatible)
- OpenAQ Public API (v3)

---

## 📊 Alert Criteria (Sample)

| Parameter | Alert Condition        | Description                  |
|-----------|------------------------|------------------------------|
| PM2.5     | > 150 µg/m³            | Hazardous air quality        |
| PM10      | > 200 µg/m³            | Unsafe dust levels           |
| NO2       | > 100 µg/m³            | Nitrogen dioxide alert       |

---

## ✅ Status

- [x] Repository created & structured  
- [ ] Spark project setup  
- [ ] Data fetch & transformation  
- [ ] S3 output integration  
- [ ] SNS email alerts  
- [ ] QuickSight dashboard

---

## 📌 Author

👨‍💻 *This project is built by Rahul Gunjal as a portfolio showcase for data engineering and cloud skills.*

---

> ⭐ Don’t forget to ⭐ this repo if you find the idea useful!

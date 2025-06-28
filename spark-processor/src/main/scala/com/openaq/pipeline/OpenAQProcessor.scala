package com.openaq.pipeline

import org.apache.spark.sql.{SparkSession, DataFrame}
import java.net.{HttpURLConnection, URL}
import scala.io.Source
import java.io.{BufferedReader, InputStreamReader}

object OpenAQProcessor {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("OpenAQ Air Quality Fetcher")
      .master("local[*]")
      .getOrCreate()

    val apiUrl =
      "https://api.openaq.org/v3/latest?country=IN&city=Pune,Mumbai,Delhi,Bengaluru,Aurangabad&parameter=pm25,pm10,so2,no2,o3"

    try {
      println("🔄 Fetching data from OpenAQ API...")

      val conn = new URL(apiUrl).openConnection().asInstanceOf[HttpURLConnection]
      conn.setRequestMethod("GET")

      val inputStream = conn.getInputStream
      val content = Source.fromInputStream(inputStream).mkString
      inputStream.close()
      conn.disconnect()

      import spark.implicits._
      import org.apache.spark.sql.functions._

      val jsonRDD = spark.sparkContext.parallelize(Seq(content))
      val df = spark.read.json(jsonRDD)

      println("✅ Raw JSON parsed into DataFrame. Showing top-level structure:")
      df.printSchema()

      // Flattening the 'results' array
      val resultsDF = df.select(explode($"results").alias("result"))
      val flatDF = resultsDF.select(
        $"result.city",
        $"result.measurements.parameter",
        $"result.measurements.value",
        $"result.measurements.unit",
        $"result.measurements.lastUpdated"
      )

      println("📊 Flattened result preview:")
      flatDF.show(false)

    } catch {
      case e: Exception =>
        println("❌ Error while fetching or processing API data:")
        e.printStackTrace()
    }

    spark.stop()
  }
}

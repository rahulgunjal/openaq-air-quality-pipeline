package com.openaq.pipeline

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import scala.io.Source
import java.net.HttpURLConnection
import java.net.URL
import scala.collection.mutable.ListBuffer

object OpenAQProcessor {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("OpenAQ Air Quality Processor")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val baseUrl = "https://api.openaq.org/v3/locations?limit=1000&order_by=id&sort_order=asc&countries_id=9"
    val apiKey = sys.env.getOrElse("OPENAQ_API_KEY", "")
    if (apiKey.isEmpty) {
      throw new RuntimeException("Environment variable OPENAQ_API_KEY is not set.")
    }

    // Get total pages
    val firstResponse = fetchJson(baseUrl, apiKey)
    val firstRDD = spark.sparkContext.parallelize(Seq(firstResponse))
    val firstDF = spark.read.json(firstRDD)
    val meta = firstDF.selectExpr("meta.found as found", "meta.limit as limit").first()
    val totalRecords = meta.getLong(0)
    val limit = meta.getLong(1)
    val totalPages = Math.ceil(totalRecords.toDouble / limit).toInt

    println(s"Total records: $totalRecords, Pages to fetch: $totalPages")

    val allJsonResponses = new ListBuffer[String]()
    allJsonResponses += firstResponse

    for (page <- 2 to totalPages) {
      val pagedUrl = s"$baseUrl&page=$page"
      allJsonResponses += fetchJson(pagedUrl, apiKey)
      println(s"Fetched page $page of $totalPages")
    }

    val resultsDFs = allJsonResponses.map { json =>
      val rdd = spark.sparkContext.parallelize(Seq(json))
      val df = spark.read.json(rdd)
      df.selectExpr("explode(results) as result")
    }

    val combinedResultsDF = resultsDFs.reduce(_ union _)
    val resultDF = combinedResultsDF.select("result.*")

    // Extract location id, name and sensor array
    val sensorDF = resultDF.select(
      $"id".alias("location_id"),
      $"name".alias("location_name"),
      $"sensors"
    )

    // Create a struct of (sensor_id, sensor_name)
    val transformedDF = sensorDF.withColumn("sensors_info",
      expr("transform(sensors, s -> struct(s.id as sensor_id, s.name as sensor_name))")
    ).select(
      $"location_id",
      $"location_name",
      $"sensors_info".alias("sensors")
    )

    transformedDF.coalesce(1)
      .write
      .option("header", "true")
      .mode("overwrite")
      .json("output/openaq_location_sensors") // use JSON since CSV can't hold arrays

    println("Sensor data written to: output/openaq_location_sensors")
    spark.stop()
  }

  def fetchJson(apiUrl: String, apiKey: String): String = {
    val conn = new URL(apiUrl).openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setRequestProperty("X-API-Key", apiKey)

    val inputStream = conn.getInputStream
    val content = Source.fromInputStream(inputStream, "UTF-8").mkString
    inputStream.close()
    conn.disconnect()
    content
  }
}

package com.openaq.pipeline

import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.sql.functions._
import scala.io.Source
import java.net.{HttpURLConnection, URL}
import scala.collection.mutable.ListBuffer
import org.apache.spark.sql.types._
import org.apache.spark.sql.AnalysisException

object OpenAQLatestFetcher {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("OpenAQ Latest Data Fetcher")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val resourceStream = Option(
      this.getClass.getClassLoader.getResourceAsStream("location_sensor_selected.json")
    ).getOrElse(throw new RuntimeException("location_sensor_mapping.json not found in resources"))

    val jsonString = Source.fromInputStream(resourceStream).mkString
    resourceStream.close()

    val rawJsonDF = spark.read.json(Seq(jsonString).toDS)

    val sensorsExploded = explodeSensorData(rawJsonDF, spark)


    val apiKey = sys.env.getOrElse("OPENAQ_API_KEY", "")
    if (apiKey.isEmpty) throw new RuntimeException("OPENAQ_API_KEY env not set")
    val resultsBuffer = new ListBuffer[Row]()
    val rowsArray = sensorsExploded.collect()

    for (i <- rowsArray.indices) {
      val row = rowsArray(i)
      try {
        resultsBuffer ++= processLocation(row, spark, apiKey)
      } catch {
        case e: AnalysisException =>
          println(s"AnalysisException caught for row $i: ${e.getMessage}")
        // You can log row-specific information or skip
        case e: Exception =>
          println(s"Other exception caught for row $i: ${e.getMessage}")
        // You can log row-specific information or skip
      }
    }

    val finalSchema = getFinalSchema()
    val finalDF = spark.createDataFrame(spark.sparkContext.parallelize(resultsBuffer), finalSchema)

    finalDF.coalesce(1)
      .write
      .option("header", "true")
      .mode("overwrite")
      .csv("output/openaq_latest_data")

    println("Latest data written to: output/openaq_latest_data")
    spark.stop()
  }

  def explodeSensorData(df: org.apache.spark.sql.DataFrame, spark: SparkSession): org.apache.spark.sql.DataFrame = {
    import spark.implicits._
    df.withColumn("sensor", explode($"sensors"))
      .select(
        $"location_id",
        $"location_name",
        $"sensor.sensor_id",
        $"sensor.sensor_name"
      )
  }

  def processLocation(row: Row, spark: SparkSession, apiKey: String): Seq[Row] = {
    import spark.implicits._
    val locId = row.getAs[Long]("location_id")
    val locName = row.getAs[String]("location_name")
    val sensorId = row.getAs[Long]("sensor_id")
    val sensorName = row.getAs[String]("sensor_name")

    val apiUrl = s"https://api.openaq.org/v3/locations/$locId/latest"
    val json = fetchJson(apiUrl, apiKey)

    val rdd = spark.sparkContext.parallelize(Seq(json))
    val df = spark.read.json(rdd)

    val exploded = df.selectExpr("explode(results) as result")

    val selected = exploded.select("result.datetime.utc", "result.datetime.local", "result.value", "result.sensorsId", "result.locationsId")

    val filtered = selected.filter($"sensorsId" === sensorId)

    val collected = filtered.collect()
    val result = new ListBuffer[Row]()
    for (i <- collected.indices) {
      val r = collected(i)
      result += Row(
        locId,
        locName,
        sensorId,
        sensorName,
        r.getAs[String]("utc"),
        r.getAs[String]("local"),
        r.getAs[Double]("value")
      )
    }
    result.toList
  }

  def fetchJson(apiUrl: String, apiKey: String): String = {
    val conn = new URL(apiUrl).openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setRequestProperty("X-API-Key", apiKey)

    val inputStream = conn.getInputStream
    val content = Source.fromInputStream(inputStream,"UTF-8").mkString
    inputStream.close()
    conn.disconnect()
    content
  }

  def getFinalSchema(): StructType = {
    StructType(Seq(
      StructField("location_id", LongType, true),
      StructField("location_name", StringType, true),
      StructField("sensor_id", LongType, true),
      StructField("sensor_name", StringType, true),
      StructField("datetime_utc", StringType, true),
      StructField("datetime_local", StringType, true),
      StructField("value", DoubleType, true)
    ))
  }
}

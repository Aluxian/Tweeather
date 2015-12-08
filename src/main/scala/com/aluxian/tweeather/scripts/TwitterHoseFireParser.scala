package com.aluxian.tweeather.scripts

import com.aluxian.tweeather.RichSeq
import com.aluxian.tweeather.transformers._
import org.apache.spark.Logging
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.Row

object TwitterHoseFireParser extends Script with Logging {

  val locationBox = TwitterHoseFireCollector.locationBox // Europe

  override def main(args: Array[String]) {
    super.main(args)
    import sqlc.implicits._

    // Import data
    logInfo("Parsing text files")
    var data = sc.textFile("/tw/fire/collected/*.text")
      .map(_.split(','))
      .map(parts => (parts(0).toDouble, parts(1).toDouble, parts(2).toLong, parts(3)))
      .toDF("lat", "lon", "createdAt", "raw_text")

    // Analyse sentiment
    logInfo("Analysing sentiment")
    data = PipelineModel.load("/tw/sentiment/models/emo.model").transform(data)

    // Get weather
    logInfo("Getting weather data")
    data = Seq(
      new GribUrlGenerator().setLocationBox(locationBox).setInputCol("createdAt").setOutputCol("grib_url"),
      new WeatherProvider().setGribsPath("/tw/fire/gribs/")
    ).mapCompose(data)(_.transform)

    // Export data
    logInfo("Exporting data")
    data
      .select("probability", "temperature", "pressure", "humidity")
      .map({ case Row(probability: Array[Double], temperature: Double, pressure: Double, humidity: Double) =>
        Seq(probability(1), temperature, pressure, humidity).mkString(",")
      })
      .saveAsTextFile("/tw/fire/parsed/data.libsvm.txt")
  }

}

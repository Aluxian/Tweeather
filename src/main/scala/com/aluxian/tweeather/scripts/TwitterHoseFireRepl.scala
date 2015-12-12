package com.aluxian.tweeather.scripts

import org.apache.spark.Logging
import org.apache.spark.ml.ann.MultilayerPerceptronModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.functions._

import scala.io.Source

object TwitterHoseFireRepl extends Script with Logging {

  override def main(args: Array[String]) {
    super.main(args)
    import sqlc.implicits._

    println("Loading fire model...")
    sc // dummy call to init the context
    val model = MultilayerPerceptronModel.load("/tw/fire/fire.model")
    println("Done. Write the input as <temperature>,<pressure>,<humidity> and press <enter>")

    for (input <- Source.stdin.getLines) {
      val t = udf { (input: String) =>
        val values = input.split(",").map(_.toDouble)
        Vectors.dense(values)
      }

      val data = sc
        .parallelize(Seq(input), 1)
        .toDF("raw_input")
        .withColumn("input", t(col("raw_input")))

      model
        .transform(data)
        .select("output")
        .foreach(println)
    }
  }

}
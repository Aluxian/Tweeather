package com.aluxian.tweeather.transformers

import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.types._

/**
  * A feature transformer that removes urls, @usernames, punctuation and symbols from input text.
  */
class StringSanitizer(override val uid: String) extends UnaryTransformer[String, String, StringSanitizer] {

  def this() = this(Identifiable.randomUID("stringSanitizer"))

  override protected def createTransformFunc: String => String = {
    _
      .toLowerCase()
      .replaceAll("\\B@\\w*", "") // @ mentions
      .replaceAll("https?:\\/\\/\\S*", "") // urls
      .replaceAll("[^a-z0-9\\s]+", "") // punctuation
      .replaceAll("\\s+", " ") // multiple white spaces
      .trim()
  }

  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType == StringType, s"Input type must be string type but got $inputType.")
  }

  override protected def outputDataType: DataType = DataTypes.StringType

  override def copy(extra: ParamMap): StringSanitizer = defaultCopy(extra)

}
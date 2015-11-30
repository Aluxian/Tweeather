package com.aluxian

import java.io.ObjectOutputStream
import java.util.{Calendar, Date, Locale}

import com.aluxian.tweeather.models.Coordinates
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.ml.Model
import twitter4j.{GeoLocation, Status}

package object tweeather {

  implicit class RichDate(date: Date) {
    def toCalendar: Calendar = {
      val calendar = Calendar.getInstance(Locale.US)
      calendar.setTime(date)
      calendar
    }
  }

  implicit class RichGeoLocation(location: GeoLocation) {
    def toCoordinate: Coordinates = {
      Coordinates(location.getLatitude, location.getLongitude)
    }
  }

  implicit class RichStatus(status: Status) {
    def getApproximateLocation: Coordinates = {
      if (status.getGeoLocation != null) {
        status.getGeoLocation.toCoordinate
      } else {
        val coordinates = status.getPlace.getBoundingBoxCoordinates()(0).map(_.toCoordinate)
        val sum = coordinates.reduce({ (c1, c2) => Coordinates(c1.lat + c2.lat, c1.lon + c2.lon) })
        Coordinates(sum.lat / coordinates.length, sum.lon / coordinates.length)
      }
    }
  }

  implicit class RichBoolean(boolean: Boolean) {
    def toDouble: Double = {
      if (boolean) 1d else 0d
    }
  }

  implicit class RichSeq[+A](seq: Seq[A]) {
    def mapCompose[B](z: B)(f: A => (B => B)): B = {
      seq.map(f).reduceRight(_ compose _).apply(z)
    }
  }

  implicit class RichModel[M <: Model[M]](model: Model[M]) {
    def serialize(hdfs: FileSystem, path: String): Unit = {
      val output = new ObjectOutputStream(hdfs.create(new Path(path)))
      output.writeObject(model)
      output.close()
    }
  }

}
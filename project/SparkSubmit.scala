import sbtsparksubmit.SparkSubmitPlugin.autoImport._

import scala.collection.mutable

object SparkSubmit {

  private lazy val sparkMaster = sys.props.getOrElse("SPARK_MASTER", "local[*]")
  private lazy val executorHighMem = "14.4g"

  private sealed case class Script(scriptName: String, highMem: Boolean = false) {
    def toSparkSubmit = {
      val params = mutable.MutableList(
        "--class", s"com.aluxian.tweeather.scripts.$scriptName",
        "--master", sparkMaster
      )

      if (highMem) {
        params ++= "--executor-memory" :: executorHighMem :: Nil
      }

      SparkSubmitSetting(s"submit-$scriptName", params)
    }
  }

  private lazy val configs = Seq(
    Script("ClusterTest"),
    Script("Sentiment140Downloader"),
    Script("Sentiment140Parser"),
    Script("Sentiment140Trainer"),
    Script("Sentiment140Repl"),
    Script("TwitterEmoCollector"),
    Script("TwitterEmoCounter", highMem = true),
    Script("TwitterEmoParser", highMem = true),
    Script("TwitterEmoTrainer", highMem = true),
    Script("TwitterEmoRepl"),
    Script("TwitterFireCollector", highMem = true),
    Script("TwitterFireCounter", highMem = true),
    Script("TwitterFireParser", highMem = true),
    Script("TwitterFireTrainer"),
    Script("TwitterFireRepl")
  )

  lazy val configurations = SparkSubmitSetting(configs.map(_.toSparkSubmit): _*)

}
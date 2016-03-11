package cz.cvut.fit.palicand.vocloud.ssl

/**
  * Created by palickaa on 08/03/16.
  */

import java.io.File

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s._
import org.json4s.native.JsonMethods._

case class CmdArgs(configFile : File = new File(".")) {

}

/**
  *
  * @param inputData URI to input data. May be either a path to a local system, or an HDFS URI
  * @param outputData URI to output data. Same value format as for the inputData param.
  * @param neighbourhoodKernel Kernel to use for computing neighbours. May be either KNN or Gaussian.
  * @param kernelParameters Parameters of the kernel. For details, see the documentation for the kernel.
  *
  */
case class Config(inputData: String, outputData: String, neighbourhoodKernel: String, kernelParameters: Map[String, Any],
                  method: String, methodParameters: Map[String, Any])

object GraphSSL {
  def main(args: Array[String]) : Unit = {
    val parser = new scopt.OptionParser[CmdArgs]("Graph-SSL") {
      head("Semi-Supervised learning using label propagation", "0.1.0")
      arg[File]("config") required() valueName("<file>") action {(x, c) =>
        c.copy(configFile = x) } text("you need to input config file in json format")
      }
    implicit val formats = DefaultFormats
    parser.parse(args, CmdArgs()) match {
      case Some(config) => {
        val conf = new SparkConf().setAppName("Graph-SSL")
        val sc = new SparkContext(conf)
        val jsonConfig = parse(file2JsonInput(config.configFile)).extract[Config]
        val sqlContext = new SQLContext(sc)
        val df = sqlContext.read
          .format("com.databricks.spark.csv")
          .option("header", "true") // Use first line of all files as header
          .option("inferSchema", "true") // Automatically infer data types
          .load(jsonConfig.inputData)
        df("class")
      }
      case None =>

    }
  }
}

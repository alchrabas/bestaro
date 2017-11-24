package bestaro.backend.service

import bestaro.locator.util.InflectionUtil
import morfologik.stemming.polish.PolishStemmer

import scala.collection.JavaConverters._
import scala.io.StdIn

object MorfologikApp {

  def main(args: Array[String]): Unit = {
    println("Select word")
    val stemmer = new PolishStemmer
    while (true) {
      val line = StdIn.readLine()
      val results = stemmer.lookup(line.toLowerCase).asScala

      results.foreach { lookupResult =>
        val genders = InflectionUtil.getGenders(lookupResult)
        val partsOfSpeech = InflectionUtil.getPartsOfSpeech(lookupResult)
        println(lookupResult.getWord + " - " + genders + " " + partsOfSpeech + " " + lookupResult.getTag)
      }
      if (results.isEmpty) {
        println("No matches!")
      }
    }
  }
}

package bestaro.core.processors

import collection.JavaConverters._
import morfologik.stemming.polish.PolishStemmer

class BaseNameProducer {

  private val stemmer = new PolishStemmer

  def strippedForStemming(original: String): String = {
    original.toLowerCase.replaceAll("[^0-9a-ząćęłńóśżź]", "").trim
  }

  def maybeBestBaseName(original: String): Option[String] = {
    val strippedOriginal = strippedForStemming(original)
    val matchedStems = stemmer.lookup(strippedOriginal)
    if (matchedStems.isEmpty || isExcludedFromMorfologik(original)) {
      None
    } else {
      val stems = matchedStems
        .asScala
        .map(_.getStem.toString)
      Some(stems.find(stem => stem.endsWith("y")).getOrElse(stems.head))
    }
  }

  def getBestBaseName(original: String): String = {
    maybeBestBaseName(original).getOrElse(strippedForStemming(original))
  }

  private def isExcludedFromMorfologik(word: String): Boolean = {
    Set("w", "i", "m", "o") contains word.toLowerCase
  }
}

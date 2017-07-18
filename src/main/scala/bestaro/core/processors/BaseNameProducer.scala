package bestaro.core.processors

import collection.JavaConverters._
import morfologik.stemming.polish.PolishStemmer

class BaseNameProducer {

  private val stemmer = new PolishStemmer

  def getBestBaseName(original: String): Option[String] = {
    val matchedStems = stemmer.lookup(original.toLowerCase)
    if (matchedStems.isEmpty || isExcludedFromMorfologik(original)) {
      None
    } else {
      val stems = matchedStems
        .asScala
        .map(_.getStem.toString)
      Some(stems.find(stem => stem.endsWith("y")).getOrElse(stems.head))
    }
  }

  private def isExcludedFromMorfologik(word: String): Boolean = {
    Set("w", "i", "m", "o") contains word.toLowerCase
  }
}

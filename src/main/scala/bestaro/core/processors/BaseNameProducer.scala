package bestaro.core.processors

import morfologik.stemming.WordData
import morfologik.stemming.polish.PolishStemmer

import scala.collection.JavaConverters._

class BaseNameProducer {

  private val stemmer = new PolishStemmer

  def strippedForStemming(original: String): String = {
    original.toLowerCase.replaceAll("[^0-9a-ząćęłńóśżź]", "").trim
  }

  def maybeBestBaseToken(original: String): Option[Token] = {
    val strippedOriginal = strippedForStemming(original)
    var matchedStems = stemmer.lookup(strippedOriginal)
    if (matchedStems.isEmpty && strippedOriginal.nonEmpty && strippedOriginal.charAt(0).isUpper) {
      matchedStems = stemmer.lookup(strippedOriginal.capitalize)
    }

    if (matchedStems.isEmpty || isExcludedFromMorfologik(original)) {
      None
    } else {
      Some(
        matchedStems
          .asScala
          .map(tagInfo => Token(original,
            strippedOriginal,
            tagInfo.getStem.toString,
            getPartsOfSpeech(tagInfo),
            0))
          .head
      )
    }
  }

  private def getPartsOfSpeech(tagInfo: WordData): List[PartOfSpeech] = {
    tagInfo.getTag
      .toString
      .split("\\+")
      .toList
      .flatMap(extractPartOfSpeechFromSingleTag)
  }

  private def extractPartOfSpeechFromSingleTag(tag: String): Option[PartOfSpeech] = {
    tag.split(":").toList
      .flatMap(TAG_TO_PART_OF_SPEECH.get).headOption
  }

  private val TAG_TO_PART_OF_SPEECH = Map(
    "adj" -> PartOfSpeech.ADJECTIVE,
    "subst" -> PartOfSpeech.NOUN,
    "ger" -> PartOfSpeech.NOUN,
    "prep" -> PartOfSpeech.PREPOSITION
  )

  def getBestBaseToken(original: String): Token = {
    maybeBestBaseToken(original).getOrElse(Token(original,
      strippedForStemming(original),
      strippedForStemming(original),
      List(PartOfSpeech.OTHER),
      0
    ))
  }

  def getBestBaseName(original: String): String = {
    getBestBaseToken(original).stem
  }

  private def isExcludedFromMorfologik(word: String): Boolean = {
    Set("w", "i", "m", "o") contains word.toLowerCase
  }
}

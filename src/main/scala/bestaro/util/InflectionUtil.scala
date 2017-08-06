package bestaro.util

import bestaro.core.processors.{Gender, PartOfSpeech}
import morfologik.stemming.WordData

object InflectionUtil {

  def getPartsOfSpeech(tagInfo: WordData): List[PartOfSpeech] = {
    splitTagInfos(tagInfo)
      .flatMap(extractPartOfSpeechFromSingleTag).distinct
  }

  private def extractPartOfSpeechFromSingleTag(tag: String): List[PartOfSpeech] = {
    tag.split("[:.]").toList
      .flatMap(TAG_TO_PART_OF_SPEECH.get)
  }

  private val TAG_TO_PART_OF_SPEECH = Map(
    "adj" -> PartOfSpeech.ADJECTIVE,
    "subst" -> PartOfSpeech.NOUN,
    "ger" -> PartOfSpeech.NOUN,
    "prep" -> PartOfSpeech.PREPOSITION,
    "verb" -> PartOfSpeech.VERB
  )

  def getGenders(tagInfo: WordData): List[Gender] = {
    splitTagInfos(tagInfo)
      .flatMap(extractGendersFromSingleTag).distinct
  }

  private def extractGendersFromSingleTag(tag: String): List[Gender] = {
    tag.split("[:\\.]").toList
      .flatMap(TAG_TO_GENDER.get)
  }

  private val TAG_TO_GENDER = Map(
    "m1" -> Gender.M,
    "m2" -> Gender.M,
    "m3" -> Gender.M,
    "f" -> Gender.F,
    "n1" -> Gender.N,
    "n2" -> Gender.N
  )

  private def splitTagInfos(tagInfo: WordData) = {
    tagInfo.getTag
      .toString
      .split("\\+")
      .toList
  }
}

package bestaro.extractors

import bestaro.core.processors._

import scala.collection.mutable.ListBuffer

case class MatchedStreet(street: StreetEntry, position: Int, wordCount: Int)

abstract class AbstractLocationExtractor {

  private val LOC_NAME_TRAIT_SCORE = 5
  private val CAPITALIZED_WORD_SCORE = 5
  private val PRECEDED_BY_LOC_NAME_TRAIT_SCORE = 5
  private val PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE = 5

  protected val baseNameProducer = new BaseNameProducer

  def extractLocationName(tokens: List[String]): (List[Token], List[MatchedStreet]) = {

    var stemmedTokens = tokens.map { tokenText =>
      val strippedTokenText = baseNameProducer.strippedForStemming(tokenText)
      val stemmedTokenText = baseNameProducer.getBestBaseName(tokenText)
      if (isLocationNameTrait(strippedTokenText,
        stemmedTokenText)) {
        val gender = getGenderOfLocationNameTrait(strippedTokenText, stemmedTokenText)
        Token(tokenText,
          strippedTokenText,
          stemmedTokenText,
          List(PartOfSpeech.NOUN),
          List(gender),
          LOC_NAME_TRAIT_SCORE)
      } else {
        evaluateMostAccurateBaseName(tokenText)
      }
    }

    println("########################")

    if (stemmedTokens.nonEmpty) {
      stemmedTokens = updateTokenEvaluationUsingContext(stemmedTokens)
    }
    val (mutableTokens, matchedStreets) = specificExtract(stemmedTokens)
    (mutableTokens.toList, matchedStreets.toList)
  }

  protected def specificExtract(stemmedTokens: List[Token]): (ListBuffer[Token], ListBuffer[MatchedStreet])


  private def updateTokenEvaluationUsingContext(tokens: List[Token]): List[Token] = {
    import PlaintextProcessor._
    tokens.slidingPrefixedByEmptyTokens(2).map { case List(nameTrait, toReturn) =>
      if (isLocationNameTrait(nameTrait)) {
        toReturn.withAlteredPlacenessScore(PRECEDED_BY_LOC_NAME_TRAIT_SCORE)
      } else {
        toReturn
      }
    }.toList.slidingPrefixedByEmptyTokens(2).map { case List(preposition, toReturn) =>
      if (isLocationSpecificPreposition(preposition)) {
        toReturn.withAlteredPlacenessScore(PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE)
      } else {
        toReturn
      }
    }.toList.slidingPrefixedByEmptyTokens(2).map { case List(previous, toReturn) =>
      if (isCapitalized(toReturn.original)
        && !tokenIsEndOfSentence(previous)
        && !isUpperCase(toReturn.original)
        && !previous.flags.contains(Flag.EMPTY_TOKEN)) {
        toReturn.withAlteredPlacenessScore(CAPITALIZED_WORD_SCORE)
      } else {
        toReturn
      }
    }.toList.slidingPrefixedByEmptyTokens(3).map { case List(preposition, nameTrait, toReturn) =>
      val isPrepositionFollowedByKind = isLocationSpecificPreposition(preposition) && isLocationNameTrait(nameTrait)
      if (isPrepositionFollowedByKind) {
        toReturn.withAlteredPlacenessScore(PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE)
      } else {
        toReturn
      }
    }.toList
  }

  private def isLocationNameTrait(token: Token): Boolean = {
    isLocationNameTrait(token.stripped, token.stem)
  }

  private def isLocationNameTrait(stripped: String, stem: String): Boolean = {
    (Set("ul", "pl", "os", "al") contains stripped) ||
      (Set("plac", "ulica", "osiedle", "aleja") contains stem)
  }

  private val strippedLocNameTraitToGender = Map(
    "ul" -> Gender.F,
    "pl" -> Gender.M,
    "os" -> Gender.N,
    "al" -> Gender.F
  )

  private val stemmedLocNameTraitToGender = Map(
    "plac" -> Gender.M,
    "ulica" -> Gender.F,
    "osiedle" -> Gender.N,
    "aleja" -> Gender.F
  )

  private def getGenderOfLocationNameTrait(stripped: String, stemmed: String): Gender = {
    strippedLocNameTraitToGender.getOrElse(stripped, stemmedLocNameTraitToGender(stemmed))
  }

  private def isLocationSpecificPreposition(token: Token): Boolean = {
    (Set("w", "we", "nad", "na") contains token.stripped) ||
      (Set("okolica", "pobliże") contains token.stem)
  }

  private def evaluateMostAccurateBaseName(original: String): Token = {
    val strippedText = baseNameProducer.strippedForStemming(original)
    baseNameProducer.maybeBestBaseToken(original).map(_.copy(placenessScore = 1)).getOrElse(
      Token(original,
        strippedText, strippedText,
        List(PartOfSpeech.OTHER),
        List(Gender.F), // because the most common "ulica" is feminine
        0)
    )
  }

  private def isCapitalized(original: String): Boolean = {
    !original.isEmpty && original(0).isUpper
  }

  def isUpperCase(original: String): Boolean = {
    original.toUpperCase == original
  }

  private def tokenIsEndOfSentence(token: Token): Boolean = {
    (token.original.endsWith(".") && !token.flags.contains(Flag.PUNCTUATED_WORD)) ||
      token.original.endsWith("!") || token.original.endsWith("?")
  }
}

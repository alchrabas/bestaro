package bestaro.extractors

import bestaro.core.processors.{BaseNameProducer, PlaintextProcessor, StreetEntry, Token}

import scala.collection.mutable.ListBuffer

case class MatchedStreet(street: StreetEntry, position: Int, wordCount: Int)

abstract class AbstractLocationExtractor {

  private val LOC_NAME_TRAIT_SCORE = 5
  private val CAPITALIZED_WORD_SCORE = 5
  private val PRECEDED_BY_LOC_NAME_TRAIT_SCORE = 5
  private val PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE = 5

  def extractLocationName(tokens: List[String]): (List[Token], List[MatchedStreet]) = {

    var stemmedTokens = tokens.map { tokenText =>
      if (isLocationNameTrait(baseNameProducer.strippedForStemming(tokenText),
        baseNameProducer.getBestBaseName(tokenText))) {
        Token(tokenText,
          baseNameProducer.strippedForStemming(tokenText),
          baseNameProducer.getBestBaseName(tokenText), LOC_NAME_TRAIT_SCORE)
      } else {
        evaluateMostAccurateBaseName(tokenText)
      }
    }.map { token =>
      if (isCapitalized(token.original)) {
        alterScore(token, by = CAPITALIZED_WORD_SCORE)
      } else {
        token
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
        alterScore(toReturn, by = PRECEDED_BY_LOC_NAME_TRAIT_SCORE)
      } else {
        toReturn
      }
    }.toList.slidingPrefixedByEmptyTokens(2).map { case List(preposition, toReturn) =>
      if (isLocationSpecificPreposition(preposition)) {
        alterScore(toReturn, by = PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE)
      } else {
        toReturn
      }
    }.toList.slidingPrefixedByEmptyTokens(3).map { case List(preposition, nameTrait, toReturn) =>
      val isPrepositionFollowedByKind = isLocationSpecificPreposition(preposition) && isLocationNameTrait(nameTrait)
      if (isPrepositionFollowedByKind) {
        alterScore(toReturn, by = PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE)
      } else {
        toReturn
      }
    }.toList
  }

  protected def alterScore(token: Token, by: Int): Token = {
    token.copy(placenessScore = token.placenessScore + by)
  }

  private def isLocationNameTrait(token: Token): Boolean = {
    isLocationNameTrait(token.stripped, token.stem)
  }

  private def isLocationNameTrait(stripped: String, stem: String): Boolean = {
    (Set("ul.", "pl.", "os.", "al.") contains stripped) ||
      (Set("plac", "ulica", "osiedle", "aleja") contains stem)
  }

  private def isLocationSpecificPreposition(token: Token): Boolean = {
    (Set("w", "we", "nad", "na") contains token.stripped) ||
      (Set("okolica", "pobliże") contains token.stem)
  }

  private val baseNameProducer = new BaseNameProducer

  private def evaluateMostAccurateBaseName(original: String): Token = {
    val strippedText = baseNameProducer.strippedForStemming(original)
    baseNameProducer.maybeBestBaseName(original) match {
      case Some(stemmedText) => Token(original,
        strippedText,
        stemmedText, 1)
      case None => Token(original,
        strippedText,
        strippedText, 0)
    }
  }

  private def isCapitalized(original: String): Boolean = {
    !original.isEmpty && original(0).isUpper
  }
}

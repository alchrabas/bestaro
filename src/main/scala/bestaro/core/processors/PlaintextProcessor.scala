package bestaro.core.processors

import java.util

import bestaro.core.{RawRecord, RecordId}
import morfologik.stemming.WordData

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

case class MatchedStreet(street: StreetEntry, position: Int)

object PlaintextProcessor {

  implicit class TokenListOps(private val tokens: List[Token]) extends AnyVal {
    def slidingPrefixedByEmptyTokens(size: Int): Iterator[List[Token]] = {
      (List.fill(size - 1)(EMPTY_TOKEN) ++ tokens).sliding(size)
    }
  }

  private val EMPTY_TOKEN = Token("", "", "", 0)
}

class PlaintextProcessor {

  private val gusData = new GusDataReader
  private val streets = gusData.streetsInKrakow
  private val streetsByFirstStemmedWord: Map[String, Seq[StreetEntry]] = streets.groupBy(_.stemmedName
    .split(" ")(0))
  private val streetsByFirstSimpleWord: Map[String, Seq[StreetEntry]] = streets.groupBy(_.strippedName
    .split(" ")(0))

  private val LOC_NAME_TRAIT_SCORE = 5
  private val CAPITALIZED_WORD_SCORE = 5
  private val EXISTING_STREET_SCORE = 10
  private val PRECEDED_BY_LOC_NAME_TRAIT_SCORE = 5
  private val PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE = 5

  def process(record: RawRecord): List[MatchedStreet] = {
    val inputText = record.message
    val tokens = Option(inputText)
      // remove everything except letters, numbers, dots, commas and white spaces
      .map(stripLinks)
      .map(_.replaceAll("[^.,!0-9a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ ]", " "))
      .map(_.replaceAll("([.,!])", "$1 "))
      .map(_.split("\\s+").toList)
      .getOrElse(List())

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
    val mutableTokens = stemmedTokens.to[ListBuffer]
    val matchedStreets = new ListBuffer[MatchedStreet]
    for (idx <- mutableTokens.indices) {
      if (streetsByFirstSimpleWord.contains(mutableTokens(idx).stripped)) {
        streetsByFirstSimpleWord(mutableTokens(idx).stripped).find(streetEntry =>
          managedToMatchStrippedStreetName(mutableTokens, idx, streetEntry, matchedStreets))
      } else if (streetsByFirstStemmedWord.contains(mutableTokens(idx).stem)) {
        streetsByFirstStemmedWord(mutableTokens(idx).stem).find(streetEntry =>
          managedToMatchStemmedStreetName(mutableTokens, idx, streetEntry, matchedStreets))
      }
    }
    stemmedTokens = mutableTokens.toList

    println(inputText)
    println(" ====> ")
    println(stemmedTokens.mkString(" "))
    val bestLocations = stemmedTokens.sortBy(_.placenessScore).reverse.slice(0, 3)
    println("BEST CANDIDATES: " + bestLocations)

    matchedStreets.toList
  }

  private def managedToMatchStemmedStreetName(mutableTokens: ListBuffer[Token],
                                              idx: Int, streetEntry: StreetEntry,
                                              matchedStreets: ListBuffer[MatchedStreet]): Boolean = {
    managedToMatchStreetName(mutableTokens, idx, streetEntry, _.stemmedName, _.stem, matchedStreets)
  }

  private def managedToMatchStrippedStreetName(mutableTokens: ListBuffer[Token],
                                               idx: Int, streetEntry: StreetEntry,
                                               matchedStreets: ListBuffer[MatchedStreet]): Boolean = {
    managedToMatchStreetName(mutableTokens, idx, streetEntry, _.strippedName, _.stripped, matchedStreets)
  }

  private def managedToMatchStreetName(mutableTokens: ListBuffer[Token],
                                       startPos: Int, streetEntry: StreetEntry,
                                       streetProperty: StreetEntry => String,
                                       tokenProperty: Token => String,
                                       matchedStreets: ListBuffer[MatchedStreet]): Boolean = {
    if (streetNameFullyMatches(mutableTokens, startPos, streetEntry, streetProperty, tokenProperty)) {
      for (wordToReplace <- streetProperty(streetEntry).split(" ").indices) {
        increaseScoreForExistingStreet(mutableTokens, startPos + wordToReplace)
      }
      matchedStreets.append(MatchedStreet(streetEntry, startPos))
      return true
    }
    false
  }

  private def increaseScoreForExistingStreet(mutableTokens: ListBuffer[Token], idx: Int) = {
    mutableTokens(idx) = alterScore(mutableTokens(idx), EXISTING_STREET_SCORE)
  }

  private def streetNameFullyMatches(tokens: ListBuffer[Token],
                                     firstTokenPos: Int,
                                     street: StreetEntry,
                                     streetProperty: StreetEntry => String,
                                     tokenProperty: Token => String
                                    ): Boolean = {
    val streetTokens = streetProperty(street).split(" ")
    val streetNameExceedsTextLength = firstTokenPos + streetTokens.length > tokens.length
    if (streetNameExceedsTextLength) {
      return false
    }
    for (wordId <- streetTokens.indices) {
      if (tokenProperty(tokens(firstTokenPos + wordId)) != streetTokens(wordId)) {
        return false
      }
    }
    true
  }

  private def stripLinks(text: String): String = {
    text.replaceAll("http[^\\s+]+", "")
  }

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

  private def alterScore(token: Token, by: Int): Token = {
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

  private def isLocativus(results: util.List[WordData]) = {
    results.asScala.exists(a => {
      a.getTag.toString.split("[:+]").contains("loc")
    })
  }

  private def isCapitalized(original: String): Boolean = {
    !original.isEmpty && original(0).isUpper
  }
}

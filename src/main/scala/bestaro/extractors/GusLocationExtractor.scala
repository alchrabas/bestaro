package bestaro.extractors

import bestaro.core.processors._
import bestaro.service.GusDataReader

import scala.collection.mutable.ListBuffer


class GusLocationExtractor extends AbstractLocationExtractor {

  private val EXISTING_STREET_SCORE = 10

  private val gusData = new GusDataReader
  private val streets = gusData.streetsInKrakow
  private val streetsByFirstStemmedWord: Map[String, Seq[StreetEntry]] = streets.groupBy(_.stemmedName
    .split(" ")(0))
  private val streetsByFirstSimpleWord: Map[String, Seq[StreetEntry]] = streets.groupBy(_.strippedName
    .split(" ")(0))

  override protected def specificExtract(stemmedTokens: List[Token]): (ListBuffer[Token], ListBuffer[MatchedStreet]) = {
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
    (mutableTokens, matchedStreets)
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

}


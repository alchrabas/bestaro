package bestaro.extractors

import bestaro.core.processors._
import bestaro.service.GusDataReader

import scala.collection.mutable.ListBuffer


class GusLocationExtractor extends AbstractLocationExtractor {

  private val EXISTING_STREET_SCORE = 10

  private val gusData = new GusDataReader
  private val streets = gusData.streetsInKrakow
  private val streetsByFirstStemmedWord: Map[String, Seq[Location]] = streets.groupBy(strippedName(_)
    .split(" ")(0)
  )
  private val streetsByFirstSimpleWord: Map[String, Seq[Location]] = streets.groupBy(_.stripped
    .split(" ")(0))

  override protected def specificExtract(stemmedTokens: List[Token]): (ListBuffer[Token], ListBuffer[MatchedLocation]) = {
    val mutableTokens = stemmedTokens.to[ListBuffer]
    val matchedStreets = new ListBuffer[MatchedLocation]
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

  private def strippedName(location: Location): String = {
    baseNameProducer.getBestBaseName(location.stripped)
  }

  private def managedToMatchStemmedStreetName(mutableTokens: ListBuffer[Token],
                                              idx: Int, streetEntry: Location,
                                              matchedStreets: ListBuffer[MatchedLocation]): Boolean = {
    managedToMatchStreetName(mutableTokens, idx, streetEntry, strippedName, _.stem, matchedStreets)
  }

  private def managedToMatchStrippedStreetName(mutableTokens: ListBuffer[Token],
                                               idx: Int, streetEntry: Location,
                                               matchedStreets: ListBuffer[MatchedLocation]): Boolean = {
    managedToMatchStreetName(mutableTokens, idx, streetEntry, _.stripped, _.stripped, matchedStreets)
  }

  private def managedToMatchStreetName(mutableTokens: ListBuffer[Token],
                                       startPos: Int, streetEntry: Location,
                                       streetProperty: Location => String,
                                       tokenProperty: Token => String,
                                       matchedStreets: ListBuffer[MatchedLocation]): Boolean = {
    val streetWords = streetProperty(streetEntry).split(" ")
    if (streetNameFullyMatches(mutableTokens, startPos, streetWords, tokenProperty)) {
      for (wordToReplace <- streetWords.indices) {
        increaseScoreForExistingStreet(mutableTokens, startPos + wordToReplace)
      }
      matchedStreets.append(MatchedLocation(streetEntry, startPos, streetWords.size))
      return true
    }
    false
  }

  private def increaseScoreForExistingStreet(mutableTokens: ListBuffer[Token], idx: Int) = {
    mutableTokens(idx) = mutableTokens(idx).withAlteredPlacenessScore(EXISTING_STREET_SCORE)
  }

  private def streetNameFullyMatches(tokens: ListBuffer[Token],
                                     firstTokenPos: Int,
                                     streetWords: Array[String],
                                     tokenProperty: Token => String
                                    ): Boolean = {
    val streetNameExceedsTextLength = firstTokenPos + streetWords.length > tokens.length
    if (streetNameExceedsTextLength) {
      return false
    }
    streetWords.indices.forall(wordId => tokenProperty(tokens(firstTokenPos + wordId)) == streetWords(wordId))
  }

}


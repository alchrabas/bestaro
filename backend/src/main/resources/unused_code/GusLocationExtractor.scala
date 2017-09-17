package bestaro.extractors

import bestaro.common.types.Location
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

  override protected def specificExtract(stemmedTokens: List[Token],
                                         foundLocationNames: Seq[MatchedInflectedLocation]):
  (ListBuffer[Token], ListBuffer[MatchedFullLocation]) = {
    null
  }

  private def strippedName(location: Location): String = {
    baseNameProducer.getBestBaseName(location.stripped)
  }

  override protected def specificExtract(stemmedTokens: List[Token]): (ListBuffer[Token], ListBuffer[MatchedFullLocation]) = {
    val mutableTokens = stemmedTokens.to[ListBuffer]
    val matchedLocations = new ListBuffer[MatchedLocation]
    for (idx <- mutableTokens.indices) {
      if (streetsByFirstSimpleWord.contains(mutableTokens(idx).stripped)) {
        streetsByFirstSimpleWord(mutableTokens(idx).stripped).find(streetEntry =>
          managedToMatchStrippedStreetName(mutableTokens, idx, streetEntry, matchedLocations))
      } else if (streetsByFirstStemmedWord.contains(mutableTokens(idx).stem)) {
        streetsByFirstStemmedWord(mutableTokens(idx).stem).find(streetEntry =>
          managedToMatchStemmedStreetName(mutableTokens, idx, streetEntry, matchedLocations))
      }
    }
    (mutableTokens, matchedLocations)
  }

  private def managedToMatchStemmedStreetName(mutableTokens: ListBuffer[Token],
                                              idx: Int, streetEntry: Location,
                                              matchedStreets: ListBuffer[MatchedFullLocation]): Boolean = {
    managedToMatchStreetName(mutableTokens, idx, streetEntry, strippedName, _.stem, matchedStreets)
  }

  private def managedToMatchStrippedStreetName(mutableTokens: ListBuffer[Token],
                                               idx: Int, streetEntry: Location,
                                               matchedStreets: ListBuffer[MatchedFullLocation]): Boolean = {
    managedToMatchStreetName(mutableTokens, idx, streetEntry, _.stripped, _.stripped, matchedStreets)
  }

  private def managedToMatchStreetName(mutableTokens: ListBuffer[Token],
                                       startPos: Int, streetEntry: Location,
                                       streetProperty: Location => String,
                                       tokenProperty: Token => String,
                                       matchedStreets: ListBuffer[MatchedFullLocation]): Boolean = {
    val streetWords = streetProperty(streetEntry).split(" ")
    if (streetNameFullyMatches(mutableTokens, startPos, streetWords, tokenProperty)) {
      for (wordToReplace <- streetWords.indices) {
        increaseScoreForExistingStreet(mutableTokens, startPos + wordToReplace)
      }
      matchedStreets.append(MatchedFullLocation(streetEntry, startPos, streetWords.size))
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

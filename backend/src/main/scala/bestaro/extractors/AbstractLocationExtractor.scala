package bestaro.extractors

import bestaro.common.types.{FullLocation, Location, Voivodeship}
import bestaro.core.RawRecord
import bestaro.core.processors._

import scala.collection.mutable.ListBuffer

case class MatchedLocation(location: Location, position: Int, wordCount: Int)

case class MatchedFullLocation(fullLocation: FullLocation, position: Int, wordCount: Int)

abstract class AbstractLocationExtractor {

  private val NOUN_PRECEDING_NAME_SCORE = 11
  private val CAPITALIZED_WORD_SCORE = 5
  private val PRECEDED_BY_NOUN_THAT_SUGGESTS_LOCATION_SCORE = 8
  private val PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE = 5
  private val NAME_ON_IGNORE_LIST_SCORE = -20

  protected val baseNameProducer = new BaseNameProducer

  protected val townNamesExtractor = new InflectedTownNamesExtractor

  def extractLocation(tokens: List[String], record: RawRecord): (List[Token], List[MatchedFullLocation]) = {

    println("########################")
    var stemmedTokens = tokens.map { tokenText =>
      val strippedTokenText = baseNameProducer.strippedForStemming(tokenText)
      val stemmedTokenText = baseNameProducer.getBestBaseName(tokenText)
      if (isNounThatSuggestsLocationName(strippedTokenText,
        stemmedTokenText)) {
        newNounToken(tokenText, strippedTokenText, stemmedTokenText)
      } else {
        evaluateMostAccurateBaseName(tokenText)
      }
    }.map(token => {
      if (onIgnoreList(token, record.fullLocation.voivodeship)) {
        token.withAlteredPlacenessScore(NAME_ON_IGNORE_LIST_SCORE)
      } else {
        token
      }
    })

    if (stemmedTokens.nonEmpty) {
      stemmedTokens = updateTokenEvaluationUsingContext(stemmedTokens)
    }
    val foundLocationNames = townNamesExtractor.findLocationNamesFromDatabase(stemmedTokens.map(_.stripped),
      record.fullLocation.voivodeship)
    if (stemmedTokens.nonEmpty) {
      println(">>> " + foundLocationNames)
      stemmedTokens = stemmedTokens.zipWithIndex.map { case (token, position) =>
        val tokenMatches = foundLocationNames.exists(townName =>
          (townName.initialPos until (townName.initialPos + townName.wordCount)) contains position)
        if (tokenMatches) {
          token.withAlteredPlacenessScore(5)
        } else {
          token
        }
      }
    }

    val (mutableTokens, matchedStreets) = specificExtract(record.fullLocation, stemmedTokens, foundLocationNames)
    (mutableTokens.toList, matchedStreets.toList)
  }

  private def newNounToken(tokenText: String, strippedTokenText: String, stemmedTokenText: String) = {
    val gender = getGenderOfNounPrecedingName(strippedTokenText, stemmedTokenText)
    Token(tokenText,
      strippedTokenText,
      stemmedTokenText,
      List(PartOfSpeech.NOUN),
      List(gender),
      NOUN_PRECEDING_NAME_SCORE,
      flags = createFlagsForNounPrecedingLocationName(tokenText))
  }

  private def createFlagsForNounPrecedingLocationName(tokenText: String) = {
    Set(Flag.NOUN_PRECEDING_LOCATION_NAME) ++
      (if (tokenText.endsWith(".")) {
        Set(Flag.PUNCTUATED_WORD)
      } else {
        Set()
      })
  }

  protected def specificExtract(alreadyKnownLocation: FullLocation, stemmedTokens: List[Token],
                                foundLocationNames: Seq[MatchedInflectedLocation]
                               ): (ListBuffer[Token], ListBuffer[MatchedFullLocation])


  private def updateTokenEvaluationUsingContext(tokens: List[Token]): List[Token] = {
    import PlaintextProcessor._
    tokens.slidingPrefixedByEmptyTokens(2).map { case List(nameTrait, toReturn) =>
      if (isNounThatSuggestsName(nameTrait)) {
        toReturn.withAlteredPlacenessScore(PRECEDED_BY_NOUN_THAT_SUGGESTS_LOCATION_SCORE)
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
        && !previous.isEndOfSentence
        && !isUpperCase(toReturn.original)
        && !previous.flags.contains(Flag.EMPTY_TOKEN)) {
        toReturn.withAlteredPlacenessScore(CAPITALIZED_WORD_SCORE)
      } else {
        toReturn
      }
    }.toList.slidingPrefixedByEmptyTokens(3).map { case List(preposition, nameTrait, toReturn) =>
      val isPrepositionFollowedByKind = isLocationSpecificPreposition(preposition) && isNounThatSuggestsName(nameTrait)
      if (isPrepositionFollowedByKind) {
        toReturn.withAlteredPlacenessScore(PRECEDED_BY_LOC_SPECIFIC_PREPOSITION_SCORE)
      } else {
        toReturn
      }
    }.toList
  }

  private def isNounThatSuggestsName(token: Token): Boolean = {
    isNounThatSuggestsLocationName(token.stripped, token.stem)
  }

  private def isNounThatSuggestsLocationName(stripped: String, stem: String): Boolean = {
    (Set("ul", "pl", "os", "al") contains stripped) ||
      (Set("plac", "ulica", "osiedle", "aleja") contains stem)
  }

  private val strippedNounPrecedingNameToGender = Map(
    "ul" -> Gender.F,
    "pl" -> Gender.M,
    "os" -> Gender.N,
    "al" -> Gender.F
  )

  private val stemmedNounPrecedingNameToGender = Map(
    "plac" -> Gender.M,
    "ulica" -> Gender.F,
    "osiedle" -> Gender.N,
    "aleja" -> Gender.F
  )

  private def getGenderOfNounPrecedingName(stripped: String, stemmed: String): Gender = {
    strippedNounPrecedingNameToGender.getOrElse(stripped, stemmedNounPrecedingNameToGender(stemmed))
  }

  private def isLocationSpecificPreposition(token: Token): Boolean = {
    (Set("w", "we", "nad", "na", "przy") contains token.stripped) ||
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

  private def onIgnoreList(token: Token, voivodeship: Option[Voivodeship]): Boolean = {
    val ignoreList = voivodeship.map {
      case Voivodeship.MALOPOLSKIE => Set("rybna", "rybną", "rybnej")
      case Voivodeship.MAZOWIECKIE => Set("paluch", "palucha", "paluchu")
      case _ => Set[String]()
    }.getOrElse(Set())

    ignoreList.contains(token.stripped) // for example names/streets of animal shelters in the area
  }

  private def isCapitalized(original: String): Boolean = {
    !original.isEmpty && original(0).isUpper
  }

  def isUpperCase(original: String): Boolean = {
    original.toUpperCase == original
  }
}

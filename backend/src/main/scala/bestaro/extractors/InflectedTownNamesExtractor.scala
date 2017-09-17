package bestaro.extractors

import java.util

import bestaro.common.types.Voivodeship
import bestaro.core.processors.Token
import bestaro.service.{InflectedLocation, PolishTownNamesInflector}

case class MatchedInflectedLocation(inflectedLocation: InflectedLocation, initialPos: Int, wordCount: Int)

class InflectedTownNamesExtractor {

  private val townEntryByVoivodeshipAndFirstWord = setUpTownEntryByVoivodeshipAndFirstWord()

  private def setUpTownEntryByVoivodeshipAndFirstWord(): util.HashMap[Voivodeship, util.HashMap[String, Seq[InflectedLocation]]] = {
    val townNamesInflector = new PolishTownNamesInflector
    val inflectedForms = townNamesInflector.loadCachedInflectedTownNames()
    val inflectedFormsByVoivodeship = inflectedForms.groupBy(_.location.voivodeship.get)
    val scalaMap = inflectedFormsByVoivodeship
      .mapValues(_.groupBy(townEntry => townEntry.stripped.split(" ")(0)))
      .mapValues { scalaMap =>
        val newMap = new util.HashMap[String, Seq[InflectedLocation]]()
        scalaMap.foreach { case (a, b) => newMap.put(a, b) }
        newMap
      }
    val newMap = new util.HashMap[Voivodeship, util.HashMap[String, Seq[InflectedLocation]]]()
    scalaMap.foreach { case (a, b) => newMap.put(a, b) }
    newMap
  }

  def findLocationNamesFromDatabase(tokens: List[Token], voivodeship: Voivodeship): Seq[MatchedInflectedLocation] = {

    val potentialMatches = tokens
      .zipWithIndex
      .flatMap { case (token, position) =>
        val firstWord = token.stripped.split(" ")(0)
        Option(townEntryByVoivodeshipAndFirstWord.get(voivodeship).get(firstWord))
          .toSeq.flatten
          .map(inflectedLocaton => MatchedInflectedLocation(inflectedLocaton, position,
            inflectedLocaton.stripped.split(" ").length))
      }
    potentialMatches.filter {
      townEntryMatch =>
        val townNameTokens = townEntryMatch.inflectedLocation.stripped.split(" ")
        val tokensToUse = tokens.slice(
          townEntryMatch.initialPos,
          townEntryMatch.initialPos + townNameTokens.length
        )
        (townEntryMatch.initialPos + townNameTokens.length <= tokens.length) &&
          tokensToUse.zip(townNameTokens).forall {
            case (token, townNamePart) => token.stripped == townNamePart
          }
    }
  }
}

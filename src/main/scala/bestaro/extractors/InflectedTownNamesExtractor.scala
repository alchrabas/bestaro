package bestaro.extractors

import java.util

import bestaro.core.processors.Token
import bestaro.service.{InflectedLocation, PolishTownNamesInflector, Voivodeship}

class InflectedTownNamesExtractor {

  private val townEntryByVoivodeshipAndFirstWord = setUpTownEntryByVoivodeshipAndFirstWord()

  case class TownEntryMatch(townEntry: InflectedLocation, initialPos: Int, wordCount: Int)

  private def setUpTownEntryByVoivodeshipAndFirstWord(): util.HashMap[Voivodeship, util.HashMap[String, Seq[InflectedLocation]]] = {
    val townNamesInflector = new PolishTownNamesInflector
    val inflectedForms = townNamesInflector.generateInflectedForms(
      townNamesInflector.loadTownEntriesFromUrzedowyWykazNazwMiejscowosci())
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

  def findTownNames(tokens: List[Token], voivodeship: Voivodeship): Seq[TownEntryMatch] = {
    val potentialMatches = tokens
      .zipWithIndex
      .flatMap { case (token, position) =>
        val firstWord = token.stripped.split(" ")(0)
        Option(townEntryByVoivodeshipAndFirstWord.get(voivodeship).get(firstWord))
          .toSeq.flatten
          .map(TownEntryMatch(_, position, token.stripped.split(" ").length))
      }
    potentialMatches.filter {
      townEntryMatch =>
        val townNameTokens = townEntryMatch.townEntry.stripped.split(" ")
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

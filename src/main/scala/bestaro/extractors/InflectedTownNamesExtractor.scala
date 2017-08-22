package bestaro.extractors

import bestaro.core.processors.Token
import bestaro.service.{PolishTownNamesInflector, InflectedLocation, Voivodeship}

class InflectedTownNamesExtractor {

  private val townNamesInflector = new PolishTownNamesInflector
  private val inflectedForms = townNamesInflector.generateInflectedForms(
    townNamesInflector.loadTownEntriesFromUrzedowyWykazNazwMiejscowosci())
  private val inflectedFormsByVoivodeship = inflectedForms.groupBy(_.location.voivodeship.get)
  private val townEntryByVoivodeshipAndFirstWord = inflectedFormsByVoivodeship
    .mapValues(_.groupBy(townEntry => townEntry.stripped.split(" ")(0)))

  case class TownEntryMatch(townEntry: InflectedLocation, initialPos: Int, wordCount: Int)

  def findTownNames(tokens: List[Token], voivodeship: Voivodeship): Seq[TownEntryMatch] = {
    val potentialMatches = tokens
      .zipWithIndex
      .flatMap { case (token, position) =>
        townEntryByVoivodeshipAndFirstWord(voivodeship).get(token.stripped).toSeq.flatten
          .map(TownEntryMatch(_, position, token.stripped.split(" ").length))
      }
    potentialMatches.filter {
      townEntryMatch =>
        val townNameTokens = townEntryMatch.townEntry.stripped.split(" ")
        val tokensToUse = tokens.slice(
          townEntryMatch.initialPos,
          townEntryMatch.initialPos + townNameTokens.length
        )
        tokensToUse.zip(townNameTokens).forall {
          case (token, townNamePart) => token.stripped == townNamePart
        }
    }
  }
}

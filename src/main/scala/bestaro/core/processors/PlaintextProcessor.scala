package bestaro.core.processors

import bestaro.core.RawRecord
import bestaro.extractors.{GusLocationExtractor, NominatimLocationExtractor}


object PlaintextProcessor {

  implicit class TokenListOps(private val tokens: List[Token]) extends AnyVal {
    def slidingPrefixedByEmptyTokens(size: Int): Iterator[List[Token]] = {
      (List.fill(size - 1)(EMPTY_TOKEN) ++ tokens).sliding(size)
    }
  }

  private val EMPTY_TOKEN = Token("", "", "", List(), 0)
}

class PlaintextProcessor {
//    val locationExtractor = new GusLocationExtractor()
  val locationExtractor = new NominatimLocationExtractor()

  def process(record: RawRecord): RawRecord = {
    val inputText = record.message
    val tokens = tokenize(inputText)

    val (stemmedTokens, matchedStreets) = locationExtractor.extractLocationName(tokens)
    println(inputText)
    println(" ====> ")
    println(stemmedTokens.mkString(" "))
    val bestLocations = stemmedTokens.sortBy(_.placenessScore).reverse.slice(0, 3)
    println("BEST CANDIDATES: " + bestLocations)
    println(s"ALL MATCHED STREETS ${matchedStreets.size} " + matchedStreets.mkString("\n"))
    record.copy(location = matchedStreets.headOption.map(_.street.strippedName).orNull)
  }

  def tokenize(inputText: String): List[String] = {
    Option(inputText)
      // remove everything except letters, numbers, dots, commas and white spaces
      .map(stripLinks)
      .map(_.replaceAll("[^.,!0-9a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ ]", " "))
      .map(_.replaceAll("([.,!])", "$1 "))
      .map(_.split("\\s+").toList)
      .getOrElse(List())
  }

  private def stripLinks(text: String): String = {
    text.replaceAll("http[^\\s+]+", "")
  }

}

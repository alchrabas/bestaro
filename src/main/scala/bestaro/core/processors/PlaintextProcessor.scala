package bestaro.core.processors

import bestaro.core.{RawRecord, Tokenizer}
import bestaro.extractors.{GusLocationExtractor, NominatimLocationExtractor}


object PlaintextProcessor {

  implicit class TokenListOps(private val tokens: List[Token]) extends AnyVal {
    def slidingPrefixedByEmptyTokens(size: Int): Iterator[List[Token]] = {
      (List.fill(size - 1)(EMPTY_TOKEN) ++ tokens).sliding(size)
    }
  }

  private val EMPTY_TOKEN = Token("", "", "", List(), List(), 0)
}

class PlaintextProcessor {
  //    val locationExtractor = new GusLocationExtractor()
  val locationExtractor = new NominatimLocationExtractor()

  def process(record: RawRecord): RawRecord = {
    val inputText = record.message
    val tokenizer = new Tokenizer()
    val tokens = tokenizer.tokenize(inputText)

    val (stemmedTokens, matchedStreets) = locationExtractor.extractLocationName(tokens)
    println(inputText)
    println(" ====> ")
    println(stemmedTokens.mkString(" "))
    val bestLocations = stemmedTokens.sortBy(_.placenessScore).reverse.slice(0, 3)
    println("BEST CANDIDATES: " + bestLocations)
    println(s"ALL MATCHED STREETS ${matchedStreets.size} " + matchedStreets.mkString("\n"))
    record.copy(location = matchedStreets.headOption.map(_.street.strippedName).orNull)
  }

}

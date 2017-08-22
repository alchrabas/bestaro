package bestaro.core.processors

import bestaro.core.{FbId, RawRecord, Tokenizer}
import bestaro.extractors.{GoogleLocationExtractor, GusLocationExtractor, NominatimLocationExtractor}
import bestaro.service.Voivodeship


object PlaintextProcessor {

  implicit class TokenListOps(private val tokens: List[Token]) extends AnyVal {
    def slidingPrefixedByEmptyTokens(size: Int): Iterator[List[Token]] = {
      (List.fill(size - 1)(EMPTY_TOKEN) ++ tokens).sliding(size)
    }
  }

  private val EMPTY_TOKEN = Token("", "", "", List(), List(), 0, flags = Set(Flag.EMPTY_TOKEN))
}

class PlaintextProcessor {
  //    val locationExtractor = new GusLocationExtractor()
//  val locationExtractor = new NominatimLocationExtractor()
  val locationExtractor = new GoogleLocationExtractor()

  def process(record: RawRecord): RawRecord = {
    val inputText = record.message
    val tokenizer = new Tokenizer()
    val tokens = tokenizer.tokenize(inputText)

    val (stemmedTokens, matchedStreets) = locationExtractor.extractLocationName(tokens, Voivodeship("MAŁOPOLSKIE"))
    println(inputText)
//    println(" ====> ")
//    println(stemmedTokens.mkString(" "))
    val bestLocations = stemmedTokens.sortBy(_.placenessScore).reverse.slice(0, 3)
    println("BEST CANDIDATES: " + bestLocations)
    println(s"ALL MATCHED STREETS ${matchedStreets.size} " + matchedStreets.mkString("\n"))
    record.copy(location = matchedStreets.headOption.map(_.location.stripped).orNull)
  }

}

package bestaro.core.processors

import bestaro.core.{RawRecord, Tokenizer}
import bestaro.extractors.GoogleLocationExtractor


object PlaintextProcessor {

  implicit class TokenListOps(private val tokens: List[Token]) extends AnyVal {
    def slidingPrefixedByEmptyTokens(size: Int): Iterator[List[Token]] = {
      (List.fill(size - 1)(EMPTY_TOKEN) ++ tokens).sliding(size)
    }
  }

  private val EMPTY_TOKEN = Token("", "", "", List(), List(), 0, flags = Set(Flag.EMPTY_TOKEN))
}

class PlaintextProcessor {
  val locationExtractor = new GoogleLocationExtractor()

  def process(record: RawRecord): RawRecord = {
    val inputText = getRecordMessage(record)
    val tokenizer = new Tokenizer()
    val tokens = tokenizer.tokenize(inputText)
    println(inputText)

    val recordWithLocation = extractAndUpdateLocation(record, tokens)
    extractAndUpdateEventType(recordWithLocation, tokens)
  }

  private def getRecordMessage(record: RawRecord) = {
    if (record.secondaryMessage.length > 0) {
      record.secondaryMessage
    } else {
      record.message
    }
  }

  private def extractAndUpdateLocation(record: RawRecord, tokens: List[String]): RawRecord = {
    val (stemmedTokens, matchedFullLocations) = locationExtractor.extractLocation(tokens, record.voivodeship)
    val mostPrimisingLocations = stemmedTokens.sortBy(_.placenessScore).reverse.slice(0, 5)
    println("BEST CANDIDATES: " + mostPrimisingLocations)
    println(s"MATCHED ${matchedFullLocations.size} STREETS: " + matchedFullLocations.mkString("\n"))
    if (matchedFullLocations.isEmpty) {
      record
    } else {
      record.copy(fullLocation = matchedFullLocations.head.fullLocation)
    }
  }

  private def extractAndUpdateEventType(record: RawRecord, tokens: Seq[String]): RawRecord = {
    record
  }
}

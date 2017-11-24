package bestaro.backend.core.processors

import bestaro.backend.AppConfig
import bestaro.backend.core.Tokenizer
import bestaro.backend.extractors.EventTypeExtractor
import bestaro.backend.types.RawRecord
import bestaro.locator.LocatorDatabase
import bestaro.locator.extractors.{CacheEfficiency, GoogleLocationExtractor}


class PlaintextProcessor(locatorDatabase: LocatorDatabase) {
  private val bestaroLocatorMemoryCache = AppConfig.getProperty("bestaroLocatorMemoryCache") == "true"
  val locationExtractor = new GoogleLocationExtractor(locatorDatabase, bestaroLocatorMemoryCache)

  def cacheEfficiencyMetrics: CacheEfficiency = locationExtractor.cacheEfficiencyMetrics

  val eventTypeExtractor = new EventTypeExtractor()

  def process(record: RawRecord): RawRecord = {
    val inputText = getRecordMessage(record)
    val tokenizer = new Tokenizer()
    val tokens = tokenizer.tokenize(inputText)
    println(inputText)

    val recordWithLocation = extractAndUpdateLocation(record, tokens)
    extractAndUpdateEventType(recordWithLocation, tokens)
  }

  private def getRecordMessage(record: RawRecord) = {
    record.message + "\n" + record.secondaryMessage
  }

  private def extractAndUpdateLocation(record: RawRecord, tokens: List[String]): RawRecord = {
    val (stemmedTokens, matchedFullLocations) = locationExtractor.extractLocation(tokens, record.fullLocation)
    val mostPromisingLocations = stemmedTokens.sortBy(_.placenessScore).reverse.slice(0, 5)
    println("BEST CANDIDATES: " + mostPromisingLocations)
    println(s"MATCHED ${matchedFullLocations.size} STREETS: " + matchedFullLocations.mkString("\n"))
    if (matchedFullLocations.isEmpty) {
      record
    } else {
      record.copy(fullLocation = matchedFullLocations.head.fullLocation)
    }
  }

  private def extractAndUpdateEventType(record: RawRecord, tokens: Seq[String]): RawRecord = {
    eventTypeExtractor.classify(record)
  }
}

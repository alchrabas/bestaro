package bestaro.backend.core.processors

import bestaro.backend.AppConfig
import bestaro.backend.core.Tokenizer
import bestaro.backend.extractors.EventTypeExtractor
import bestaro.backend.types.RawRecord
import bestaro.locator.LocatorDatabase
import bestaro.locator.extractors.{CacheEfficiency, GoogleLocationExtractor}
import bestaro.locator.types.{FullLocation, Token, Voivodeship}


class PlaintextProcessor(locatorDatabase: LocatorDatabase) {
  private val bestaroLocatorMemoryCache = AppConfig.getProperty("bestaroLocatorMemoryCache") == "true"
  val locationExtractor = new GoogleLocationExtractor(locatorDatabase, bestaroLocatorMemoryCache)

  private def onIgnoreList(token: Token, knownLocation: FullLocation): Boolean = {
    val ignoreList = knownLocation.voivodeship.map {
      case Voivodeship.MALOPOLSKIE => Set("rybna", "rybną", "rybnej")
      case Voivodeship.MAZOWIECKIE => Set("paluch", "palucha", "paluchu")
      case Voivodeship.LODZKIE => Set("marmurowa", "marmurową", "marmurowej")
      case Voivodeship.PODKARPACKIE => Set("ciepłownicza", "ciepłowniczą", "ciepłowniczej", "cieplownicza", "cieplowniczej")
      case Voivodeship.DOLNOSLASKIE => Set("ślazowa", "ślazową", "ślazowej", "slazowa", "slazowej")
      case Voivodeship.LUBUSKIE => Set("szwajcarska", "szwajcarską", "szwajcarskiej", "żurawia", "żurawią", "żurawiej", "zurawia", "zurawiej")
      case Voivodeship.KUJAWSKO_POMORSKIE => Set("przybyszewskiego", "stanisława", "stanislawa", "grunwaldzka", "grunwaldzką", "grunwaldzkiej")
      case Voivodeship.WIELKOPOLSKIE => Set("bukowska", "bukowską", "bukowskiej")
      case Voivodeship.LUBELSKIE => Set("metalurgiczna", "metalurgiczną", "metalurgicznej")
      case Voivodeship.ZACHODNIOPOMORSKIE => Set("wojska", "polskiego")
      case Voivodeship.POMORSKIE => Set("przyrodników", "przyrodnikow", "małokacka", "małokacką", "małokackiej", "malokacka", "malokackiej")
      case Voivodeship.OPOLSKIE => Set("torowa", "torową", "torowej")
      case Voivodeship.PODLASKIE => Set("dolistowska", "dolistowską", "dolistowskiej")
      case Voivodeship.SLASKIE => Set("milowicka", "milowicką", "milowickiej")
      case Voivodeship.SWIETOKRZYSKIE => Set("ściegiennego", "sciegiennego")
      case Voivodeship.WARMINSKO_MAZURSKIE => Set("turystyczna", "turystyczną", "turystycznej")
    }.getOrElse(Set())

    ignoreList.contains(token.stripped) // for example names/streets of animal shelters in the area
  }

  locationExtractor.setOnIgnoreListPredicate(onIgnoreList)

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

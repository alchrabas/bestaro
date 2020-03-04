package unused_code

import bestaro.locator.extractors.{AbstractLocationExtractor, MatchedFullLocation, MatchedInflectedLocation, MultiWordLocationNameExtractor}
import bestaro.locator.types.{Location, Token}
import org.apache.http.impl.client.DefaultHttpClient

import scala.collection.mutable.ListBuffer

case class NominatimMatchedStreet(primary: Location, secondary: Location, position: Int, wordCount: Int, address: Address)

class NominatimLocationExtractor extends AbstractLocationExtractor {

  private val nominatim = new CachedNominatimClient(
    new JsonNominatimClient(new DefaultHttpClient(),
      "nominatim@exeris.org"),
    countingLogger())

  private def countingLogger() = {
    var requests = 0
    _: String => {
      requests += 1
      if (requests % 100 == 0) {
        println(s"Done $requests requests to Nominatim")
      }
    }
  }

  private val STREET_MAX_WORDS = 3

  override protected def specificExtract(stemmedTokens: List[Token],
                                         foundLocationNames: Seq[MatchedInflectedLocation]):
  (ListBuffer[Token], ListBuffer[MatchedFullLocation]) = {
    val mutableTokens = stemmedTokens.to[ListBuffer]

    val multiWordLocationNameExtractor = new MultiWordLocationNameExtractor
    val multiWordNames = multiWordLocationNameExtractor.mostSuitableMultiWordNames(stemmedTokens, foundLocationNames)
    println(multiWordNames.map(_.stripped).mkString(";; "))
    val matchedStreets = multiWordNames.flatMap(a => allAddressesGotFromWordList(mutableTokens, a.startIndex, a.wordCount))

    //    val matchedStreets = allAddressesFoundInWholeMessage(mutableTokens)
    val mostImportantCity = matchedStreets.map(_.address).filter(_.getElementType == "city").sortBy(_.getImportance).reverse.headOption
    val mostImportantCounty = matchedStreets.map(_.address).filter(_.getElementType == "administrative").sortBy(_.getImportance).reverse.headOption
    println("MOST IMPORTANT CITY IS: " + mostImportantCity.flatMap(getAddressKey(_, "city")))
    println("MOST IMPORTANT GMINA IS: " + mostImportantCounty.flatMap(getAddressKey(_, "county")))

    val krakowMatchedStreets = matchedStreets
      .filter(matchedStreet => isInCity(matchedStreet.address, "Kraków"))
      .filter(_.address.getElementType != "administrative")
      .sortBy(matchedStreet => (matchedStreet.wordCount, getTokensPlacenessScore(matchedStreet, mutableTokens)))
      .reverse.slice(0, 3)
    //    println("STREETS ARE: " + krakowMatchedStreets.mkString("\n"))

    (mutableTokens, krakowMatchedStreets.map(matchedStreetFromNominatimMatchedStreet).to[ListBuffer])
  }

  private def matchedStreetFromNominatimMatchedStreet(nominatimStreet: NominatimMatchedStreet): MatchedFullLocation = {
    val streetName = getLeftMostAddressKey(nominatimStreet.address, "road", "residental").map(_.toLowerCase)
      .getOrElse(nominatimStreet.address.getAddressElements.head.getValue)
    val townName = getLeftMostAddressKey(nominatimStreet.address, "town", "county").map(_.toLowerCase)
      .getOrElse(nominatimStreet.address.getAddressElements.head.getValue)
    MatchedFullLocation(
      FullLocation(
        Some(Location(baseNameProducer.strippedForStemming(streetName), streetName,
          LocationType.STREET, Some(Voivodeship.MALOPOLSKIE))),
        Some(Location(baseNameProducer.strippedForStemming(townName), townName,
          LocationType.CITY, Some(Voivodeship.MALOPOLSKIE))), None),
      nominatimStreet.position,
      nominatimStreet.wordCount
    )
  }

  private def getTokensPlacenessScore(nominatimMatchedStreet: NominatimMatchedStreet,
                                      mutableTokens: ListBuffer[Token]): Int = {
    (nominatimMatchedStreet.position until nominatimMatchedStreet.position + nominatimMatchedStreet.wordCount)
      .map(mutableTokens(_))
      .map(_.placenessScore)
      .sum
  }

  private def isInCity(address: Address, cityName: String): Boolean = {
    address.getAddressElements
      .filter(_.getKey == "city")
      .exists(_.getValue == cityName)
  }

  private def getAddressKey(address: Address, key: String): Option[String] = {
    address.getAddressElements.find(_.getKey == key).map(_.getValue)
  }

  private def getLeftMostAddressKey(address: Address, keys: String*): Option[String] = {
    keys.flatMap(getAddressKey(address, _)).headOption
  }

  private def allAddressesFoundInWholeMessage(mutableTokens: ListBuffer[Token]): List[NominatimMatchedStreet] = {
    val matchedStreets = new ListBuffer[NominatimMatchedStreet]
    for (wordCount <- 1 to STREET_MAX_WORDS) {
      for (initialPos <- 0 to (mutableTokens.size - wordCount)) {
        matchedStreets.appendAll(allAddressesGotFromWordList(mutableTokens, initialPos, wordCount))
      }
    }
    matchedStreets.toList
  }

  private def allAddressesGotFromWordList(mutableTokens: ListBuffer[Token],
                                          initialPos: Int, wordCount: Int): List[NominatimMatchedStreet] = {
    val multiwordName = mergeIntoMultiwordName(mutableTokens, initialPos, wordCount)
    val addresses = nominatim.search(multiwordName + ", województwo małopolskie")
    if (addresses.nonEmpty) {
      //      println("@@@ For name: " + multiwordName + " - " + addresses.size + " results")
      //      printAddresses(addresses)
    }
    addresses.map { address =>
      val locationName = getAddressKey(address, "road").getOrElse(address.getDisplayName)
      val townName = getAddressKey(address, "town").getOrElse(address.getDisplayName)

      NominatimMatchedStreet(
        Location(baseNameProducer.strippedForStemming(locationName),
          locationName, LocationType.STREET),
        Location(baseNameProducer.strippedForStemming(townName),
          townName, LocationType.CITY),
        initialPos, wordCount, address)
    }
  }

  private def mergeIntoMultiwordName(mutableTokens: ListBuffer[Token], initialPos: Int, wordSize: Int): String = {
    (mutableTokens(initialPos).stem :: (1 until wordSize)
      .map(wordId => mutableTokens(initialPos + wordId).stripped)
      .toList)
      .mkString(" ")
  }

  private def printAddresses(addresses: List[Address]) {
    addresses.foreach(address => println(" - " + address.getDisplayName))
  }
}

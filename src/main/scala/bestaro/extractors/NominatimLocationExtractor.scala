package bestaro.extractors

import bestaro.core.processors.{StreetEntry, Token}
import bestaro.service.CachedNominatimClient
import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.model.Address
import org.apache.http.impl.client.DefaultHttpClient

import scala.collection.mutable.ListBuffer

case class NominatimMatchedStreet(street: StreetEntry, position: Int, wordCount: Int, address: Address)

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

  override protected def specificExtract(stemmedTokens: List[Token]): (ListBuffer[Token], ListBuffer[MatchedStreet]) = {
    val mutableTokens = stemmedTokens.to[ListBuffer]
    val matchedStreets = allAddressesFoundInWholeMessage(mutableTokens)
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

  private def matchedStreetFromNominatimMatchedStreet(nominatimStreet: NominatimMatchedStreet): MatchedStreet = {
    val streetName = getAddressKey(nominatimStreet.address, "road").map(_.toLowerCase)
      .getOrElse(nominatimStreet.street.strippedName)
    MatchedStreet(
      nominatimStreet.street.copy(strippedName = streetName),
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
    val addresses = nominatim.search(multiwordName + ", Kraków, województwo małopolskie")
    if (addresses.nonEmpty) {
      //      println("@@@ For name: " + multiwordName + " - " + addresses.size + " results")
      //      printAddresses(addresses)
    }
    addresses.map { address =>
      NominatimMatchedStreet(StreetEntry(getAddressKey(address, "road").getOrElse(address.getDisplayName), address.getElementType,
        address.getDisplayName, address.getDisplayName), initialPos, wordCount, address)
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

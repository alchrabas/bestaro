package bestaro.extractors

import bestaro.core.processors.{StreetEntry, Token}
import bestaro.service.CachedGoogleApiClient
import com.google.maps.model.{AddressComponent, AddressComponentType, GeocodingResult}

import scala.collection.mutable.ListBuffer

class GoogleLocationExtractor extends AbstractLocationExtractor {

  private val geocodingClient = new CachedGoogleApiClient(countingLogger())

  private def countingLogger() = {
    var requests = 0
    _: String => {
      requests += 1
      if (requests % 100 == 0) {
        println(s"Done $requests requests to Nominatim")
      }
    }
  }

  override protected def specificExtract(stemmedTokens: List[Token]): (ListBuffer[Token], ListBuffer[MatchedStreet]) = {
    val mutableTokens = stemmedTokens.to[ListBuffer]

    val multiWordLocationNameExtractor = new MultiWordLocationNameExtractor
    val multiWordNames = multiWordLocationNameExtractor.mostSuitableMultiWordNames(stemmedTokens)
    println(multiWordNames.map(_.stripped).mkString(";; "))
    val bestWordAndResult = multiWordNames.map(getGeocodingResultForMultiWordName).find(_._2.nonEmpty)

    val bestResults = bestWordAndResult
      .map { case (name, results) =>
        MatchedStreet(streetEntryFromGeocodingResults(results),
          name.startIndex, name.wordsCount)
      }.to[ListBuffer]

    (mutableTokens, bestResults)
  }

  private def getGeocodingResultForMultiWordName(multiWordName: MultiWordName) = {
    (multiWordName, geocodingClient.search(
      replaceAbbreviatedNouns(multiWordName).stripped + ", województwo małopolskie"))
  }

  private def replaceAbbreviatedNouns(multiWordName: MultiWordName): MultiWordName = {
    multiWordName.copy(tokens = multiWordName.tokens.map(expandAbbreviatedNounsPrecedingLocation))
  }

  private def expandAbbreviatedNounsPrecedingLocation(token: Token): Token = {
    val strippedReplacements = REPLACEMENTS.get(token.stripped)
    if (strippedReplacements.isDefined) {
      token.copy(stripped = strippedReplacements.get, stem = strippedReplacements.get)
    } else {
      token
    }
  }

  private val REPLACEMENTS = Map(
    "os" -> "osiedle",
    "al" -> "aleja",
    "ul" -> "ulica",
    "pl" -> "plac"
  )

  private def streetEntryFromGeocodingResults(results: List[GeocodingResult]): StreetEntry = {
    if (results.size > 1) {
      println("MULTIPLE SOLUTIONS AVAILABLE:\n" + results.map(_.formattedAddress).mkString(";; \n"))
    }
    StreetEntry(getStreetName(results.head), "street",
      baseNameProducer.strippedForStemming(getStreetName(results.head)),
      baseNameProducer.strippedForStemming(getStreetName(results.head)))
  }

  private def getStreetName(geoResult: GeocodingResult): String = {
    val TYPES = List(
      AddressComponentType.ROUTE,
      AddressComponentType.STREET_ADDRESS,
      AddressComponentType.NEIGHBORHOOD,
      AddressComponentType.SUBLOCALITY,
      AddressComponentType.LOCALITY
    )
    TYPES.flatMap(addressType =>
      geoResult.addressComponents.filter(isOfType(_, addressType)).map(_.longName)).headOption
      .getOrElse(geoResult.formattedAddress)
  }

  private def isOfType(component: AddressComponent, addressComponentType: AddressComponentType) = {
    component.types.contains(addressComponentType)
  }
}

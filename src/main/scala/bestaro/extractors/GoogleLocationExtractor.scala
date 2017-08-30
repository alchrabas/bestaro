package bestaro.extractors

import bestaro.core.{Coordinate, FullLocation}
import bestaro.core.processors.{Location, LocationType, Token}
import bestaro.service.{CachedGoogleApiClient, Voivodeship}
import com.google.maps.model.{AddressComponent, AddressComponentType, AddressType, GeocodingResult}

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

  override protected def specificExtract(stemmedTokens: List[Token]): (ListBuffer[Token], ListBuffer[MatchedFullLocation]) = {
    val mutableTokens = stemmedTokens.to[ListBuffer]

    val multiWordLocationNameExtractor = new MultiWordLocationNameExtractor
    val multiWordNames = multiWordLocationNameExtractor.mostSuitableMultiWordNames(stemmedTokens)
    //    println(multiWordNames.map(_.stripped).mkString(";; "))
    val bestWordAndResult = multiWordNames.map(getGeocodingResultForMultiWordName).find(_._2.nonEmpty)
    println(bestWordAndResult)
    val bestResults = bestWordAndResult
      .map { case (name, results) =>
        MatchedFullLocation(locationFromGeocodingResults(results),
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

  private def locationFromGeocodingResults(results: List[GeocodingResult]): FullLocation = {
    if (results.size > 1) {
      println("MULTIPLE SOLUTIONS AVAILABLE:" + results.map(_.formattedAddress).mkString(";; \n"))
    }
    val firstResult = results.head
    val voivodeshipName = firstResult.addressComponents
      .find(_.types.contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1))
      .map(_.shortName).getOrElse("UKNOWN").toLowerCase.replaceAll("województwo ", "")
    val matchedVoivodeship = Voivodeship.values.find(_.name.equalsIgnoreCase(voivodeshipName))

    val locationType = firstFoundType(firstResult.addressComponents)
    val primaryLocation = getMostSpecificLocation(firstResult, PRIMARY_LOCATION_TYPES)
      .map(primaryPart => Location(
        baseNameProducer.strippedForStemming(primaryPart),
        primaryPart,
        locationType,
        matchedVoivodeship))
    val secondaryLocation = getMostSpecificLocation(firstResult, SECONDARY_LOCATION_TYPES)
      .map(secondaryPart => Location(
        baseNameProducer.strippedForStemming(secondaryPart),
        secondaryPart,
        locationType,
        matchedVoivodeship))

    val coords = firstResult.geometry.location
    FullLocation(
      primaryLocation, secondaryLocation,
      Some(Coordinate(coords.lat, coords.lng))
    )
  }

  private val PRIMARY_LOCATION_TYPES = List(
    (AddressComponentType.ROUTE, LocationType.STREET),
    (AddressComponentType.STREET_ADDRESS, LocationType.STREET),
    (AddressComponentType.NEIGHBORHOOD, LocationType.ESTATE),
    (AddressComponentType.SUBLOCALITY, LocationType.DISTRICT)
  )
  private val SECONDARY_LOCATION_TYPES = List(
    (AddressComponentType.LOCALITY, LocationType.TOWN),
    (AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_3, LocationType.MUNCIPALITY)
  )
  private val ALL_TYPES = PRIMARY_LOCATION_TYPES ++ SECONDARY_LOCATION_TYPES

  private def firstFoundType(addressComponents: Array[AddressComponent]): LocationType = {
    ALL_TYPES.find(requiredType => addressComponents.flatMap(_.types).contains(requiredType._1)).map(_._2)
      .getOrElse(LocationType.UNKNOWN)
  }

  private def getMostSpecificLocation(geoResult: GeocodingResult,
                                      types: Seq[(AddressComponentType, LocationType)]
                                     ): Option[String] = {
    types.map(_._1).flatMap(addressType =>
      geoResult.addressComponents.filter(isOfType(_, addressType)).map(_.longName)).headOption
  }

  private def isOfType(component: AddressComponent, addressComponentType: AddressComponentType) = {
    component.types.contains(addressComponentType)
  }
}

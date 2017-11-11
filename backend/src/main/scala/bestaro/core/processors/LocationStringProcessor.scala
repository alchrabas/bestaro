package bestaro.core.processors

import bestaro.common.types.{FullLocation, Location, Voivodeship, VoivodeshipNameVariants}
import bestaro.core.{RawRecord, Tokenizer}
import bestaro.extractors.InflectedTownNamesExtractor
import bestaro.locator.LocatorDatabase

class LocationStringProcessor(locatorDatabase: LocatorDatabase) {

  def process(record: RawRecord): RawRecord = {

    if (nonEmpty(record.location)) {
      val newFullLocation = extractLocationData(record.location.toLowerCase(), record.fullLocation)
      record.copy(fullLocation = newFullLocation)
    } else {
      record
    }
  }

  def extractLocationData(parsableLocation: String, alreadyKnownLocation: FullLocation): FullLocation = {
    var fullLocation = alreadyKnownLocation
    val locationParts = parsableLocation.split(",").map(_.trim).toSeq

    if (fullLocation.voivodeship.isEmpty) {
      fullLocation = fullLocation.copy(voivodeship = locationParts.flatMap(extractVoivodeship).headOption)
    }

    if (fullLocation.secondary.isEmpty) {
      fullLocation = fullLocation.copy(secondary =
        locationParts.flatMap(extractSecondaryLocation(_, fullLocation.voivodeship)).headOption)
    }

    fullLocation
  }

  private def extractVoivodeship(locationPart: String): Option[Voivodeship] = {
    VoivodeshipNameVariants.VARIANTS.find(_._2.contains(locationPart)).map(_._1)
  }

  private def extractSecondaryLocation(locationPart: String,
                                       voivodeshipRestriction: Option[Voivodeship],
                                       memoryCache: Boolean = true): Option[Location] = {
    val tokenizer = new Tokenizer
    val locationTokens = tokenizer.tokenize(locationPart)
    val inflectedTownNamesExtractor = new InflectedTownNamesExtractor(locatorDatabase, memoryCache)
    inflectedTownNamesExtractor.findLocationNamesFromDatabase(locationTokens, voivodeshipRestriction)
      .map(_.inflectedLocation.location).headOption
  }

  def nonEmpty(str: String): Boolean = {
    str != null && str != ""
  }
}

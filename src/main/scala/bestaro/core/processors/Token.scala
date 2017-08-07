package bestaro.core.processors

case class PartOfSpeech(name: String)

object PartOfSpeech {
  val ADJECTIVE = PartOfSpeech("adjective")
  val NOUN = PartOfSpeech("noun")
  val PREPOSITION = PartOfSpeech("preposition")
  val VERB = PartOfSpeech("verb")
  val OTHER = PartOfSpeech("other")
}

case class Gender(symbol: String)

object Gender {
  val M = Gender("masculine")
  val F = Gender("feminine")
  val N = Gender("neuter")
}

case class Importance(name: String)

object Importance {
  val PRIMARY = Importance("primary")
  val SECONDARY = Importance("secondary")
}

case class LocationType(name: String)

object LocationType {
  val STREET = LocationType("street")
  val ESTATE = LocationType("estate") // = "osiedle"
  val CITY = LocationType("city") // or town or village
  val UNKNOWN = LocationType("unknown")
}

case class Token(
                  original: String,
                  stripped: String,
                  stem: String,
                  partsOfSpeech: List[PartOfSpeech],
                  genders: List[Gender],
                  placenessScore: Int,
                  locationType: LocationType = LocationType.UNKNOWN,
                  importance: Importance = Importance.PRIMARY
                ) {
  override def toString: String = {
    original + " (" + stem + ")[" + placenessScore + "]"
  }

  def withAlteredPlacenessScore(alteredBy: Int): Token = {
    copy(placenessScore = placenessScore + alteredBy)
  }
}

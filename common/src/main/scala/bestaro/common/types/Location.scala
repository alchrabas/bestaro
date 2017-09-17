package bestaro.common.types

import play.api.libs.json.{Json, OFormat}

case class Location(stripped: String, original: String, kind: LocationType,
                    voivodeship: Option[Voivodeship] = None, parent: Option[Location] = None)

object Location {
  implicit val locationFormat: OFormat[Location] = Json.format[Location]
}

case class LocationType(name: String)

object LocationType {
  val STREET = LocationType("street")
  val ESTATE = LocationType("estate") // osiedle
  val DISTRICT = LocationType("district") // dzielnica
  val CITY = LocationType("city")
  val VILLAGE = LocationType("village")
  val MUNCIPALITY = LocationType("muncipality") // gmina
  val UNKNOWN = LocationType("unknown")

  implicit val locationTypeFormat: OFormat[LocationType] = Json.format[LocationType]
}

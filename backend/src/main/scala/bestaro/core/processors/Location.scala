package bestaro.core.processors

import bestaro.service.Voivodeship
import play.api.libs.json.{Json, OFormat}

case class Location(stripped: String, original: String, kind: LocationType,
                    voivodeship: Option[Voivodeship] = None, parent: Option[Location] = None)

object Location {
  implicit val locationFormat: OFormat[Location] = Json.format[Location]
}
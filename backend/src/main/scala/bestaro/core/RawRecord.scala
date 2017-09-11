package bestaro.core

import bestaro.core.processors.Location
import bestaro.service.Voivodeship
import play.api.libs.json.{Json, OFormat}

case class RawRecord(recordId: RecordId,
                     status: AnimalStatus,
                     message: String,
                     postDate: Long,
                     voivodeship: Voivodeship,
                     pictures: List[String] = List(),
                     link: String = "",
                     location: String = "",
                     eventDate: Long = 0,
                     title: String = "",
                     fullLocation: FullLocation = FullLocation(None, None, None)
                    )

sealed case class AnimalStatus(value: String)

object AnimalStatus {
  implicit val animalStatusFormat: OFormat[AnimalStatus] = Json.format[AnimalStatus]
}

case class Coordinate(lat: Double, lon: Double)

object Coordinate {
  implicit val coordinateFormat: OFormat[Coordinate] = Json.format[Coordinate]
}

case class FullLocation(primary: Option[Location], secondary: Option[Location], coordinate: Option[Coordinate])

object FullLocation {
  implicit val fullLocationFormat: OFormat[FullLocation] = Json.format[FullLocation]
}

object RawRecord {
  implicit val rawRecordFormat: OFormat[RawRecord] = Json.format[RawRecord]
}

object ProgressStatus {

  object LOST extends AnimalStatus("LOST")

  object FOUND extends AnimalStatus("FOUND")

  object SEEN extends AnimalStatus("SEEN")

  val values = Seq(LOST, FOUND, SEEN)
}

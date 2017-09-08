package bestaro.core

import bestaro.core.processors.Location
import bestaro.service.Voivodeship

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

case class Coordinate(lat: Double, lon: Double)

case class FullLocation(primary: Option[Location], secondary: Option[Location], coordinate: Option[Coordinate])

object ProgressStatus {

  object LOST extends AnimalStatus("LOST")

  object FOUND extends AnimalStatus("FOUND")

  object SEEN extends AnimalStatus("SEEN")

  val values = Seq(LOST, FOUND, SEEN)
}

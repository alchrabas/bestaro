package bestaro.core

import bestaro.common._
import play.api.libs.json.{Json, OFormat}

case class RawRecord(recordId: RecordId,
                     eventType: EventType,
                     animalType: AnimalType,
                     message: String,
                     postDate: Long,
                     voivodeship: Voivodeship,
                     pictures: List[String] = List(),
                     link: String = "",
                     location: String = "",
                     eventDate: Long = 0,
                     title: String = "",
                     fullLocation: FullLocation = FullLocation(None, None, None)
                    ) {

  def buildRecord: Record = {
    Record(recordId,
      eventType, voivodeship, pictures, link,
      eventDate,
      postDate,
      fullLocation)
  }
}

object RawRecord {
  implicit val rawRecordFormat: OFormat[RawRecord] = Json.format[RawRecord]
}

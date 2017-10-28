package bestaro.core

import bestaro.common.types._
import play.api.libs.json.{Json, OFormat}

case class RawRecord(recordId: RecordId,
                     eventType: EventType,
                     animalType: AnimalType,
                     message: String,
                     postDate: Long,
                     dataSource: String,
                     pictures: List[String] = List(),
                     link: String = "",
                     location: String = "",
                     eventDate: Long = 0,
                     title: String = "",
                     fullLocation: FullLocation = FullLocation(None, None, None, None),
                     secondaryMessage: String = "",
                    ) {

  def buildRecord: Record = {
    Record(recordId,
      eventType, pictures, link,
      eventDate,
      postDate,
      fullLocation)
  }
}

object RawRecord {
  implicit val rawRecordFormat: OFormat[RawRecord] = Json.format[RawRecord]
}

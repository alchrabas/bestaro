package bestaro.backend.types

import bestaro.locator.types.FullLocation
import play.api.libs.json.{Json, OFormat}

case class Record(
                   recordId: RecordId,
                   eventType: EventType,
                   animalType: AnimalType,
                   pictures: List[String] = List(),
                   link: String,
                   eventDate: Long,
                   postDate: Long,
                   fullLocation: FullLocation
                 )

object Record {
  implicit val record: OFormat[Record] = Json.format[Record]
}

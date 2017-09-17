package bestaro.common.types

import play.api.libs.json.{Json, OFormat}

case class Record(
                   recordId: RecordId,
                   status: EventType,
                   voivodeship: Voivodeship,
                   pictures: List[String] = List(),
                   link: String,
                   eventDate: Long,
                   publishDate: Long,
                   fullLocation: FullLocation
                 )

object Record {
  implicit val record: OFormat[Record] = Json.format[Record]
}

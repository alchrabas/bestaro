package bestaro.common

import play.api.libs.json.{Json, OFormat}

case class Record(
                   recordId: RecordId,
                   status: AnimalStatus,
                   voivodeship: Voivodeship,
                   pictures: List[String] = List(),
                   link: String,
                   date: Long,
                   fullLocation: FullLocation
                 )

object Record {
  implicit val record: OFormat[Record] = Json.format[Record]
}

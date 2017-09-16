package bestaro.common

import play.api.libs.json.{Json, OFormat}

case class RecordDTO(
                      record: Record,
                      pictures: List[NamedPicture]
                    )

object RecordDTO {
  implicit val recordDTO: OFormat[RecordDTO] = Json.format[RecordDTO]
}

case class NamedPicture(
                         name: String,
                         bytes: Array[Byte]
                       )

object NamedPicture {
  implicit val namedPictureFormat: OFormat[NamedPicture] = Json.format[NamedPicture]
}


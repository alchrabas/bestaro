package bestaro.common.types

import java.util.Base64

import play.api.libs.json._

case class RecordDTO(
                      record: Record,
                      pictures: List[NamedPicture]
                    )

object RecordDTO {
  implicit val recordDTO: OFormat[RecordDTO] = Json.format[RecordDTO]
}

case class NamedPicture(
                         path: String,
                         bytes: Array[Byte],
                         minifiedBytes: Array[Byte]
                       )


object NamedPicture {
  implicit val topWrites: Writes[NamedPicture] = Writes[NamedPicture] {
    picture =>
      JsObject(Map(
        "name" -> JsString(picture.path),
        "bytes" -> JsString(new String(Base64.getEncoder.encode(picture.bytes))),
        "minifiedBytes" -> JsString(new String(Base64.getEncoder.encode(picture.minifiedBytes)))
      ))
  }

  implicit val topReads: Reads[NamedPicture] = Reads[NamedPicture] {
    case JsObject(dataMap) =>
      JsSuccess(NamedPicture(
        dataMap("name").asInstanceOf[JsString].value,
        Base64.getDecoder.decode(dataMap("bytes").asInstanceOf[JsString].value.getBytes),
        Base64.getDecoder.decode(dataMap("minifiedBytes").asInstanceOf[JsString].value.getBytes)
      ))
  }
}

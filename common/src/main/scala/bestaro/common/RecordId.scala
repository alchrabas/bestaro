package bestaro.common

import play.api.libs.json._

sealed trait RecordId {
  def service: String

  def id: String

  override def toString: String = {
    service + "/" + id
  }
}

case class FbId(id: String) extends RecordId {
  def service = "FB"
}

case class OlxId(id: String) extends RecordId {
  def service = "OLX"
}

case class GumtreeId(id: String) extends RecordId {
  def service = "GUMTREE"
}

object FbId {
  implicit val jsonFormat: OFormat[FbId] = Json.format[FbId]
}

object OlxId {
  implicit val jsonFormat: OFormat[OlxId] = Json.format[OlxId]
}

object GumtreeId {
  implicit val jsonFormat: OFormat[GumtreeId] = Json.format[GumtreeId]
}

object RecordId {
  implicit val topWrites: Writes[RecordId] = Writes[RecordId] {
    case fbId: FbId => FbId.jsonFormat.writes(fbId) + (("service", JsString(fbId.service)))
    case olxId: OlxId => OlxId.jsonFormat.writes(olxId) + (("service", JsString(olxId.service)))
    case gumtreeId: GumtreeId => GumtreeId.jsonFormat.writes(gumtreeId) + (("service", JsString(gumtreeId.service)))
    case x => throw new IllegalArgumentException(s"$x is not supported subtype of RecordId")
  }

  implicit val topReads: Reads[RecordId] = Reads[RecordId] {
    case jsValue: JsObject => jsValue("service") match {
      case JsString("FB") => FbId.jsonFormat.reads(jsValue)
      case JsString("OLX") => OlxId.jsonFormat.reads(jsValue)
      case JsString("GUMTREE") => GumtreeId.jsonFormat.reads(jsValue)
      case x => throw new IllegalArgumentException(s"$x is not supported type of serialized RecordId")
    }
    case x => throw new IllegalArgumentException(s"$x is not supported type of serialized RecordId")
  }
}

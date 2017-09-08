package bestaro.core

sealed trait RecordId {
  def service: String

  def id: String

  override def toString: String = {
    service + "/" + id
  }
}

object RecordId {
  import upickle.default._

  implicit val rw: ReadWriter[RecordId] = macroRW[FbId] merge macroRW[OlxId] merge macroRW[GumtreeId]
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
package bestaro.core

case class RawRecord(recordId: RecordId,
                     status: AnimalStatus,
                     message: String,
                     postDate: Long,
                     pictures: List[String] = List(),
                     link: String = "",
                     location: String = "",
                     eventDate: Long = 0,
                     title: String = ""
                 )

sealed case class AnimalStatus(value: String)

object ProgressStatus {

  object LOST extends AnimalStatus("LOST")

  object FOUND extends AnimalStatus("FOUND")

  object SEEN extends AnimalStatus("SEEN")

  val values = Seq(LOST, FOUND, SEEN)
}

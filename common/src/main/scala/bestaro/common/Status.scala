package bestaro.common

import play.api.libs.json.{Json, OFormat}

sealed case class AnimalStatus(value: String)

object AnimalStatus {
  implicit val animalStatusFormat: OFormat[AnimalStatus] = Json.format[AnimalStatus]
}


object ProgressStatus {

  object LOST extends AnimalStatus("LOST")

  object FOUND extends AnimalStatus("FOUND")

  object SEEN extends AnimalStatus("SEEN")

  val values = Seq(LOST, FOUND, SEEN)
}

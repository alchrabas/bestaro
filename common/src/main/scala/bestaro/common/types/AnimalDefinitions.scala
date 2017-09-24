package bestaro.common.types

import play.api.libs.json.{Json, OFormat}

sealed case class EventType(value: String)

object EventType {

  object LOST extends EventType("LOST")

  object FOUND extends EventType("FOUND")

  object SEEN extends EventType("SEEN")

  object UNKNOWN extends EventType("UKNOWN")

  def byName(name: String): EventType = {
    val typesByName = List(LOST, FOUND, SEEN, UNKNOWN).map(et => et.value -> et).toMap
    typesByName(name)
  }

  implicit val eventTypeFormat: OFormat[EventType] = Json.format[EventType]
}

sealed case class AnimalType(value: String)

object AnimalType {

  object CAT extends AnimalType("CAT")

  object DOG extends AnimalType("DOG")

  object PARROT extends AnimalType("PARROT")

  object TORTOISE extends AnimalType("TORTOISE")

  object OTHER extends AnimalType("OTHER")

  object UNKNOWN extends AnimalType("UKNOWN")

  implicit val animalTypeFormat: OFormat[AnimalType] = Json.format[AnimalType]
}
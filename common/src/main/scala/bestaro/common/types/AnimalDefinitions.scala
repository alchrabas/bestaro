package bestaro.common.types

import play.api.libs.json._
import enumeratum._

import scala.collection.immutable

sealed trait EventType extends EnumEntry

object EventType extends Enum[EventType] with PlayJsonEnum[EventType] {

  val values: immutable.IndexedSeq[EventType] = findValues

  case object LOST extends EventType

  case object FOUND extends EventType

  case object SEEN extends EventType

  case object UNKNOWN extends EventType

  implicit val eventTypeWrites: Writes[EventType] = Json.writes[EventType]
  implicit val eventTypeReads: Reads[EventType] = Json.reads[EventType]
}

sealed trait AnimalType extends EnumEntry

object AnimalType extends Enum[AnimalType] with PlayJsonEnum[AnimalType] {

  val values: immutable.IndexedSeq[AnimalType] = findValues

  case object CAT extends AnimalType

  case object DOG extends AnimalType

  case object PARROT extends AnimalType

  case object TORTOISE extends AnimalType

  case object OTHER extends AnimalType

  case object UNKNOWN extends AnimalType

  implicit val animalTypeWrites: Writes[AnimalType] = Json.writes[AnimalType]
  implicit val animalTypeReads: Reads[AnimalType] = Json.reads[AnimalType]
}

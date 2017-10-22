package bestaro.common.types

import play.api.libs.json.{Json, OFormat}

object Voivodeship {
  val MALOPOLSKIE = Voivodeship("MAŁOPOLSKIE")
  val LUBUSKIE = Voivodeship("LUBUSKIE")
  val KUJAWSKO_POMORSKIE = Voivodeship("KUJAWSKO-POMORSKIE")
  val POMORSKIE = Voivodeship("POMORSKIE")
  val SWIETOKRZYSKIE = Voivodeship("ŚWIĘTOKRZYSKIE")
  val SLASKIE = Voivodeship("ŚLĄSKIE")
  val OPOLSKIE = Voivodeship("OPOLSKIE")
  val LODZKIE = Voivodeship("ŁÓDZKIE")
  val ZACHODNIOPOMORSKIE = Voivodeship("ZACHODNIOPOMORSKIE")
  val LUBELSKIE = Voivodeship("LUBELSKIE")
  val MAZOWIECKIE = Voivodeship("MAZOWIECKIE")
  val PODLASKIE = Voivodeship("PODLASKIE")
  val DOLNOSLASKIE = Voivodeship("DOLNOŚLĄSKIE")
  val PODKARPACKIE = Voivodeship("PODKARPACKIE")
  val WIELKOPOLSKIE = Voivodeship("WIELKOPOLSKIE")
  val WARMINSKO_MAZURSKIE = Voivodeship("WARMIŃSKO-MAZURSKIE")

  val values = List(MALOPOLSKIE, LUBUSKIE, KUJAWSKO_POMORSKIE, POMORSKIE,
    SWIETOKRZYSKIE, SLASKIE, OPOLSKIE, LODZKIE, ZACHODNIOPOMORSKIE, LUBELSKIE,
    MAZOWIECKIE, PODLASKIE, DOLNOSLASKIE, PODKARPACKIE, WIELKOPOLSKIE, WARMINSKO_MAZURSKIE)

  implicit val voivodeshipFormat: OFormat[Voivodeship] = Json.format[Voivodeship]
}

case class Voivodeship(name: String) {
  def searchString: String = {
    "województwo " + name.toLowerCase()
  }
}

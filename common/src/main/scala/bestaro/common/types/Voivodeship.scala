package bestaro.common.types

import bestaro.common.util.PolishCharactersAsciizer
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

object VoivodeshipNameVariants {

  private val asciizer = new PolishCharactersAsciizer

  private def appendVoivodeshipPrefixAndAsciizerForm(name: String): Seq[String] = {
    Seq(
      name,
      asciizer.convertToAscii(name),
      "województwo " + name,
      "wojewodztwo " + asciizer.convertToAscii(name)
    )
  }

  /**
    * All lowercase variants of names of Polish voivodeships
    */
  val VARIANTS: Map[Voivodeship, Seq[String]] = Map(
    Voivodeship.MALOPOLSKIE -> Seq("małopolskie", "małopolska"),
    Voivodeship.LUBUSKIE -> Seq("lubuskie"),
    Voivodeship.KUJAWSKO_POMORSKIE -> Seq("kujawsko-pomorskie"),
    Voivodeship.POMORSKIE -> Seq("pomorskie", "pomorze"),
    Voivodeship.SWIETOKRZYSKIE -> Seq("świętokrzyskie"),
    Voivodeship.SLASKIE -> Seq("śląskie", "śląsk"),
    Voivodeship.OPOLSKIE -> Seq("opolskie"),
    Voivodeship.LODZKIE -> Seq("łódzkie"),
    Voivodeship.ZACHODNIOPOMORSKIE -> Seq("zachodniopomorskie"),
    Voivodeship.LUBELSKIE -> Seq("lubelskie"),
    Voivodeship.MAZOWIECKIE -> Seq("mazowieckie", "mazowsze"),
    Voivodeship.PODLASKIE -> Seq("podlaskie", "podlasie"),
    Voivodeship.DOLNOSLASKIE -> Seq("dolnośląskie", "dolny śląsk"),
    Voivodeship.PODKARPACKIE -> Seq("podkarpackie", "podkarpacie"),
    Voivodeship.WIELKOPOLSKIE -> Seq("wielkopolskie", "wielkopolska"),
    Voivodeship.WARMINSKO_MAZURSKIE -> Seq("warmińsko-mazurskie")
  ).mapValues(_.flatMap(appendVoivodeshipPrefixAndAsciizerForm))
}

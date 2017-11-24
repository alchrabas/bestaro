package unused_code;

class GusDataReader {
  //  val STREET_NAMES_CSV = "gus/ULIC_Adresowy_2017-07-17.csv"
  val STREET_NAMES_CSV = "gus/ULIC_Adresowy_Krakow.csv"

  private val baseNameProducer = new BaseNameProducer

  import GusDataReader._

  private val streetNamesResource = getClass.getClassLoader.getResource(STREET_NAMES_CSV)
  private val reader = CSVReader.open(streetNamesResource.getFile)
  val streetsInKrakow: Seq[Location] = reader.allWithHeaders()
    .filter(isInKrakow)
    .map(convertToStreetEntry)

  private def isInKrakow(street: Map[String, String]): Boolean = {
    street.get("WOJ").map(_.toInt).contains(12) &&
      street.get("POW").map(_.toInt).contains(61)
  }

  private def convertToStreetEntry(csvEntry: Map[String, String]): Location = {
    val originalName = csvEntry("NAZWA_1")
    val kind = csvEntry("CECHA")
    val strippedName = stripAndShortenName(originalName)

    Location(strippedName, originalName, LocationType.STREET)
  }

  private def stripAndShortenName(str: String): String = {
    if (str.contains("im.")) {
      str.substring(str.indexOf("im.") + 3)
    } else if (str.contains("ul.")) {
      str.substring(str.indexOf("ul.") + 3)
    } else if (str.startsWith("św.")) {
      str.substring(3)
    } else {
      str
    }.toLowerCase
  }

  private def stemTheWords(original: String): String = {
    original.split("\\s+")
      .map(baseNameProducer.getBestBaseName)
      .mkString(" ")
  }
}

package bestaro.core.processors

import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import morfologik.stemming.polish.PolishStemmer

object GusDataReader {

  implicit object MyFormat extends DefaultCSVFormat {
    override val delimiter = ';'
  }

}

class GusDataReader {
  val STREET_NAMES_CSV = "gus/ULIC_Adresowy_2017-07-17.csv"

  private val baseNameProducer = new BaseNameProducer

  import GusDataReader._

  private val streetNamesResource = getClass.getClassLoader.getResource(STREET_NAMES_CSV)
  private val reader = CSVReader.open(streetNamesResource.getFile)
  val streetsInKrakow: Seq[StreetEntry] = reader.allWithHeaders()
    .filter(isInKrakow)
    .map(convertToStreetEntry)

  private def isInKrakow(street: Map[String, String]): Boolean = {
    street.get("WOJ").map(_.toInt).contains(12) &&
      street.get("POW").map(_.toInt).contains(61)
  }

  private def convertToStreetEntry(csvEntry: Map[String, String]): StreetEntry = {
    val originalName = csvEntry("NAZWA_1")
    val kind = csvEntry("CECHA")
    val stemmedName = stemTheWords(originalName)
    val simpleName = simplifyName(originalName)
    val stemmedSimpleName = stemTheWords(simpleName)

    StreetEntry(originalName, kind, simpleName, stemmedName, stemmedSimpleName)
  }

  private def simplifyName(str: String): String = {
    if (str.contains("im.")) {
      str.substring(str.indexOf("im.") + 3)
    } else if (str.contains("ul.")) {
      str.substring(str.indexOf("ul.") + 3)
    } else if (str.startsWith("św.")) {
      str.substring(3)
    } else {
      str
    }
  }

  private def stemTheWords(original: String): String = {
    original.split("\\s+")
      .map { word =>
        baseNameProducer.getBestBaseName(word).getOrElse(word)
      }.mkString(" ")
  }
}

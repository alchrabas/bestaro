package bestaro.service

import bestaro.core.processors.{BaseNameProducer, Location}
import bestaro.util.FileIO
import com.github.tototoshi.csv.{CSVReader, CSVWriter, DefaultCSVFormat}
import upickle.default.read

object PolishTownNamesInflector {

  implicit object MyFormat extends DefaultCSVFormat {
    override val delimiter = ';'
  }

  def main(args: Array[String]): Unit = {
    val converter = new PolishTownNamesInflector
    //    println(converter.generateInflectedForms(converter.loadTownEntriesFromUrzedowyWykazNazwMiejscowosci())
    //      .filter(_.location.voivodeship.map(_.name).contains("MAŁOPOLSKIE")).mkString("\n"))
    //    converter.printMostIgnoredSuffixes(converter.loadTownEntriesFromFile())
    converter.filterOutUnusedData()
  }
}

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
}

case class Voivodeship(name: String)

case class InflectedLocation(stripped: String, location: Location)

class PolishTownNamesInflector {

  import PolishTownNamesInflector._

  val TOWN_NAMES_CSV = "gus/TERC_Adresowy_2017-07-17.csv"
  val URZEDOWY_WYKAZ_NAZW_CSV = "gus/urzedowy_wykaz_nazw_miejscowosci_2015.csv"
  val FILTERED_URZEDOWY_WYKAZ_NAZW_CSV = "gus/urzedowy_wykaz_nazw_miejscowosci_2015_filtered.csv"

  private type suffixReplacementMapType = Map[String, Set[String]]
  private type allSuffixesMapType = Map[String, suffixReplacementMapType]
  private val suffixesReadFromFile = read[allSuffixesMapType](FileIO.readFile("town_suffixes.json", "{}"))

  private val genetivusSuffixes = suffixesReadFromFile("genetivus")
  private val locativusSuffixes = suffixesReadFromFile("locativus")

  private val baseNameProducer = new BaseNameProducer

  def loadTownEntriesFromTerytFile(): Seq[InflectedLocation] = {
    val townNamesResource = getClass.getClassLoader.getResource(TOWN_NAMES_CSV)
    val reader = CSVReader.open(townNamesResource.getFile)
    reader.allWithHeaders()
      .filterNot(row => row("NAZWA_DOD") == "województwo")
      .map(convertToTownEntry)
  }

  private def convertToTownEntry(csvEntry: Map[String, String]): InflectedLocation = {
    val originalName = csvEntry("NAZWA").toLowerCase
    val voivodeshipId = Integer.parseInt(csvEntry("WOJ"))
    val kind = csvEntry("NAZWA_DOD")

    InflectedLocation(originalName, Location(originalName, originalName, "town",
      Some(VOIVODESHIP_BY_ID(voivodeshipId))))
  }

  private val TOWN_NAME_COLUMN = "Nazwa miejscowości "
  private val KIND_COLUMN = "Rodzaj"
  private val VOIVODESHIP_COLUMN = "Województwo"

  def loadTownEntriesFromUrzedowyWykazNazwMiejscowosci(): Seq[InflectedLocation] = {
    val townNamesResource = getClass.getClassLoader.getResource(FILTERED_URZEDOWY_WYKAZ_NAZW_CSV)
    val reader = CSVReader.open(townNamesResource.getFile)
    reader.allWithHeaders()
      .filter(row => {
        Set("wieś", "miasto").contains(row(KIND_COLUMN)) ||
          row(KIND_COLUMN).startsWith("część miasta")
      })
      .map {
        row =>
          val stripped = baseNameProducer.strippedForStemming(row(TOWN_NAME_COLUMN))
          InflectedLocation(stripped,
            Location(stripped, row(TOWN_NAME_COLUMN), "town",
              Some(Voivodeship(row(VOIVODESHIP_COLUMN).toUpperCase))
            )
          )
      }
  }

  def generateInflectedForms(townsInNominativus: Seq[InflectedLocation]): Seq[InflectedLocation] = {
    townsInNominativus.flatMap(inflectedVersionsOfName).distinct
  }

  def inflectedVersionsOfName(original: InflectedLocation): Seq[InflectedLocation] = {
    makeGenetivus(original) ++ makeLocativus(original)
  }

  private def makeGenetivus(townEntry: InflectedLocation): Seq[InflectedLocation] = {
    makeNonNominativus(townEntry, genetivusSuffixes)
  }

  private def makeLocativus(townEntry: InflectedLocation): Seq[InflectedLocation] = {
    makeNonNominativus(townEntry, locativusSuffixes)
  }

  private def makeNonNominativus(nominativusTown: InflectedLocation,
                                 suffixReplacements: Map[String, Set[String]]): Seq[InflectedLocation] = {
    val wordsOfOriginalName = nominativusTown.location.stripped.split(" ")
    val allWordVariants = wordsOfOriginalName.toSeq.map {
      originalWordOfName =>
        val wordReplacements = suffixReplacements
          .filterKeys(originalWordOfName.endsWith)
          .flatMap {
            case (pattern, replacements) =>
              replacements.map(replacement =>
                replaceSuffix(originalWordOfName, pattern, replacement))
          }.toList
        originalWordOfName :: wordReplacements
    }

    generateAllWordCombinations(allWordVariants)
      .map(_.mkString(" "))
      .map(newName => nominativusTown.copy(stripped = newName))
  }

  private def generateAllWordCombinations(wordVariants: Seq[Seq[String]]): Seq[Seq[String]] = {
    if (wordVariants.isEmpty) {
      return List(List())
    }

    wordVariants.head.flatMap(variant =>
      generateAllWordCombinations(wordVariants.tail)
        .map(variant :: _.toList))
  }

  private def replaceSuffix(subject: String, pattern: String, replacement: String): String = {
    subject.substring(0, subject.length - pattern.length) + replacement
  }

  def printMostIgnoredSuffixes(townsInNominativus: Seq[InflectedLocation]) {
    val mostIgnoredSuffixes = townsInNominativus
      .map(_.stripped)
      .filter(a => !genetivusSuffixes.keys.exists(a.endsWith))
      .map(a => a.substring(a.length - 3, a.length))
      .groupBy(a => a)
      .mapValues(_.size)

    println("# " + townsInNominativus.size + " BROKEN " + mostIgnoredSuffixes.values.sum)

    println(
      mostIgnoredSuffixes
        .toSeq.sortBy(_._2)
        .reverse.mkString("\n")
    )
  }

  def filterOutUnusedData(): Unit = {
    val townNamesResource = getClass.getClassLoader.getResource(URZEDOWY_WYKAZ_NAZW_CSV)
    val reader = CSVReader.open(townNamesResource.getFile)
    val filteredRows = reader.allWithHeaders()
      .filter(row => {
        Set("wieś", "miasto").contains(row(KIND_COLUMN)) ||
          row(KIND_COLUMN).startsWith("część miasta")
      })
      .map(row => List(
        row("Nazwa miejscowości "),
        row("Rodzaj"),
        row("Województwo")
      ))

    val writer = CSVWriter.open("src/main/resources/" + FILTERED_URZEDOWY_WYKAZ_NAZW_CSV)
    writer.writeRow(List("Nazwa miejscowości ", "Rodzaj", "Województwo"))
    writer.writeAll(filteredRows)
  }

  val VOIVODESHIP_BY_ID = Map(
    12 -> Voivodeship.MALOPOLSKIE,
    8 -> Voivodeship.LUBUSKIE,
    4 -> Voivodeship.KUJAWSKO_POMORSKIE,
    22 -> Voivodeship.POMORSKIE,
    26 -> Voivodeship.SWIETOKRZYSKIE,
    24 -> Voivodeship.SLASKIE,
    16 -> Voivodeship.OPOLSKIE,
    10 -> Voivodeship.LODZKIE,
    32 -> Voivodeship.ZACHODNIOPOMORSKIE,
    6 -> Voivodeship.LUBELSKIE,
    14 -> Voivodeship.MAZOWIECKIE,
    20 -> Voivodeship.PODLASKIE,
    2 -> Voivodeship.DOLNOSLASKIE,
    18 -> Voivodeship.PODKARPACKIE,
    30 -> Voivodeship.WIELKOPOLSKIE,
    28 -> Voivodeship.WARMINSKO_MAZURSKIE
  )
}

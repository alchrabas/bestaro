package bestaro.service

import bestaro.util.FileIO
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import upickle.default.read

object PolishTownNamesInflector {

  implicit object MyFormat extends DefaultCSVFormat {
    override val delimiter = ';'
  }

  def main(args: Array[String]): Unit = {
    val converter = new PolishTownNamesInflector
    println(converter.generateInflectedForms(converter.loadTownEntriesFromUrzedowyWykazNazwMiejscowosci())
      .filter(_.voivodeship.name == "MAŁOPOLSKIE").mkString("\n"))
    //    converter.printMostIgnoredSuffixes(converter.loadTownEntriesFromFile())
  }
}

case class Voivodeship(name: String)

case class TownEntry(name: String, originalName: String, voivodeship: Voivodeship)

class PolishTownNamesInflector {

  import PolishTownNamesInflector._

  val TOWN_NAMES_CSV = "gus/TERC_Adresowy_2017-07-17.csv"
  val URZEDOWY_WYKAZ_NAZW_CSV = "gus/urzedowy_wykaz_nazw_miejscowosci_2015_filtered.csv"

  private type suffixReplacementMapType = Map[String, Set[String]]
  private type allSuffixesMapType = Map[String, suffixReplacementMapType]
  private val suffixesReadFromFile = read[allSuffixesMapType](FileIO.readFile("town_suffixes.json", "{}"))

  private val genetivusSuffixes = suffixesReadFromFile("genetivus")
  private val locativusSuffixes = suffixesReadFromFile("locativus")

  def loadTownEntriesFromTerytFile(): Seq[TownEntry] = {
    val townNamesResource = getClass.getClassLoader.getResource(TOWN_NAMES_CSV)
    val reader = CSVReader.open(townNamesResource.getFile)
    reader.allWithHeaders()
      .filterNot(row => row("NAZWA_DOD") == "województwo")
      .map(convertToTownEntry)
  }

  private def convertToTownEntry(csvEntry: Map[String, String]): TownEntry = {
    val originalName = csvEntry("NAZWA").toLowerCase
    val voivodeshipId = Integer.parseInt(csvEntry("WOJ"))
    val kind = csvEntry("NAZWA_DOD")

    TownEntry(originalName, originalName,
      Voivodeship(VOIVODESHIP_ID_TO_NAME(voivodeshipId)))
  }

  def loadTownEntriesFromUrzedowyWykazNazwMiejscowosci(): Seq[TownEntry] = {
    val townNamesResource = getClass.getClassLoader.getResource(URZEDOWY_WYKAZ_NAZW_CSV)
    val reader = CSVReader.open(townNamesResource.getFile)
    reader.allWithHeaders()
      .filter(row =>
        Set("wieś", "miasto").contains(row("Rodzaj")) ||
          row("Rodzaj").startsWith("część miasta"))
      .map {
        row =>
          TownEntry(
            row("Nazwa miejscowości ").toLowerCase,
            row("Nazwa miejscowości ").toLowerCase,
            Voivodeship(row("Województwo").toUpperCase))
      }
  }

  def generateInflectedForms(townsInNominativus: Seq[TownEntry]): Seq[TownEntry] = {
    townsInNominativus.flatMap(inflectedVersionsOfName).distinct
  }

  def inflectedVersionsOfName(original: TownEntry): Seq[TownEntry] = {
    makeGenetivus(original) ++ makeLocativus(original)
  }

  private def makeGenetivus(townEntry: TownEntry): Seq[TownEntry] = {
    makeNonNominativus(townEntry, genetivusSuffixes)
  }

  private def makeLocativus(townEntry: TownEntry): Seq[TownEntry] = {
    makeNonNominativus(townEntry, locativusSuffixes)
  }

  private def makeNonNominativus(nominativusTown: TownEntry,
                                 suffixReplacements: Map[String, Set[String]]): Seq[TownEntry] = {
    val wordsOfOriginalName = nominativusTown.originalName.split(" ")
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
      .map(newName => nominativusTown.copy(name = newName))
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

  def printMostIgnoredSuffixes(townsInNominativus: Seq[TownEntry]) {
    val mostIgnoredSuffixes = townsInNominativus
      .map(_.name)
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

  val VOIVODESHIP_ID_TO_NAME = Map(
    12 -> "MAŁOPOLSKIE",
    8 -> "LUBUSKIE",
    4 -> "KUJAWSKO-POMORSKIE",
    22 -> "POMORSKIE",
    26 -> "ŚWIĘTOKRZYSKIE",
    24 -> "ŚLĄSKIE",
    16 -> "OPOLSKIE",
    10 -> "ŁÓDZKIE",
    32 -> "ZACHODNIOPOMORSKIE",
    6 -> "LUBELSKIE",
    14 -> "MAZOWIECKIE",
    20 -> "PODLASKIE",
    2 -> "DOLNOŚLĄSKIE",
    18 -> "PODKARPACKIE",
    30 -> "WIELKOPOLSKIE",
    28 -> "WARMIŃSKO-MAZURSKIE")
}

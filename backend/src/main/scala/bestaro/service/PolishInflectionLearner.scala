package bestaro.service

import java.io.File
import java.util.zip.ZipFile

import bestaro.common.util.FileIO
import bestaro.core.processors.BaseNameProducer
import morfologik.stemming.WordData
import morfologik.stemming.polish.PolishStemmer
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.Source

object PolishInflectionLearner {


  def main(args: Array[String]): Unit = {
    createSuffixFileFromAspell()
  }

  private def createSuffixFileFromAspell(): Unit = {
    val learner = new PolishInflectionLearner
    val result = learner.readAspellDict()

    val jsonResult = Json.stringify(Json.toJson(result))
    FileIO.saveFile("town_suffixes.json", jsonResult)
  }

  private def createSuffixFileFromBooks(): Unit = {
    val learner = new PolishInflectionLearner

    val jsonResult = Json.stringify(Json.toJson(Map(
      "genetivus" -> learner.learnAboutCaseFromBooks("gen"),
      "locativus" -> learner.learnAboutCaseFromBooks("loc")
    )))
    FileIO.saveFile("town_suffixes.json", jsonResult)
  }
}

class PolishInflectionLearner {

  private val baseNameProducer = new BaseNameProducer

  private val polishStemmer = new PolishStemmer

  def learnAboutCaseFromBooks(caseName: String): Map[String, Set[String]] = {
    val polishTextsDirectory = new File(getClass.getClassLoader.getResource("polish_texts").getFile)
    if (!polishTextsDirectory.isDirectory) {
      throw new IllegalStateException("polish_texts/ is not a directory!")
    }

    polishTextsDirectory.listFiles()
      .map(learnAboutCaseFromBook(caseName))
      .reduce(mergeMaps) ++ supplementarySuffixes(caseName)
  }

  private def mergeMaps(m1: Map[String, Set[String]], m2: Map[String, Set[String]]): Map[String, Set[String]] = {
    (m1.keys.toSet ++ m2.keys.toSeq.toSet).map(key =>
      key -> (m1.getOrElse(key, Set()) ++ m2.getOrElse(key, Set()))).toMap
  }

  private def learnAboutCaseFromBook(caseName: String): File => Map[String, Set[String]] = {
    bookFile => {
      val bufferedSource = Source.fromFile(bookFile)
      val allMappings = bufferedSource.getLines.map { line =>
        val strippedWords = line.split("\\s+").toList.map(baseNameProducer.strippedForStemming)

        val allLookupResults = getWordData(strippedWords)
        val allTransformationsToGenetivus = allLookupResults
          .filter(isSingular)
          .filter(getCases(_).contains(caseName))
          .filter(_.getStem.length() >= 3)
          .map(lookupResult => {
            val inflectedWord = lookupResult.getWord.toString
            val wordInNominativus = lookupResult.getStem.toString
            val commonPrefix = longestCommonPrefix(inflectedWord, wordInNominativus)

            (remainderOfString(wordInNominativus, commonPrefix),
              remainderOfString(inflectedWord, commonPrefix))
          })
          .filter(suffixNotTooLong)

        allTransformationsToGenetivus
      }.flatten.toList.groupBy(_._1).map { case (k, v) => (k, v.map(_._2).toSet) }
      bufferedSource.close

      allMappings
    }
  }

  private def suffixNotTooLong(suffixReplacement: (String, String)): Boolean = {
    suffixReplacement._1.length <= 5 && suffixReplacement._2.length <= 5
  }

  private def longestCommonPrefix(first: String, second: String): String = {
    first.zip(second).takeWhile { case (a, b) => a == b }.map(_._1).mkString
  }

  private def getWordData(strippedWords: Seq[String]): Seq[WordData] = {
    strippedWords.flatMap(
      wiseLookup(_)
        .map(_.clone())
        .toList)
    // idk why, but it's necessary
  }

  private def getCases(lookupResult: WordData): Set[String] = {
    splitIntoTokens(lookupResult).filter(CASES.contains).toSet
  }

  private def isPlural(lookupResult: WordData): Boolean = {
    splitIntoTokens(lookupResult).contains("pl")
  }

  private def isSingular(lookupResult: WordData): Boolean = {
    splitIntoTokens(lookupResult).contains("sg")
  }

  private def splitIntoTokens(lookupResult: WordData): Seq[String] = {
    lookupResult.getTag.toString.split("[:.+]")
  }

  def readAspellDict(): Map[String, Map[String, Set[String]]] = {
    val aspellDictionaryInputStream = readAspellDictionaryFromArchive
    val aspellDictReader = Source.fromInputStream(aspellDictionaryInputStream, "UTF-8")
    val genetivusReplacements = new mutable.HashMap[String, Set[String]]
    val locativusReplacements = new mutable.HashMap[String, Set[String]]
    aspellDictReader.getLines.foreach { line =>
      val wordVariants = line.split("\\s+").toSeq
      val strippedWordVariants = wordVariants.map(baseNameProducer.strippedForStemming)
      val results = getWordData(strippedWordVariants)

      getDifferenceInSuffixesOfInflectedWord(results, "gen", isSingular)
        .filter(suffixNotTooLong).foreach(addToMap(genetivusReplacements))
      getDifferenceInSuffixesOfInflectedWord(results, "gen", isPlural)
        .filter(suffixNotTooLong).foreach(addToMap(genetivusReplacements))
      getDifferenceInSuffixesOfInflectedWord(results, "loc", isSingular)
        .filter(suffixNotTooLong).foreach(addToMap(locativusReplacements))
      getDifferenceInSuffixesOfInflectedWord(results, "loc", isPlural)
        .filter(suffixNotTooLong).foreach(addToMap(locativusReplacements))
    }

    Map(
      "genetivus" -> mergeMaps(genetivusReplacements.toMap, supplementaryGenetivusSuffixes),
      "locativus" -> mergeMaps(locativusReplacements.toMap, supplementaryLocativusSuffixes)
    )
  }

  private def readAspellDictionaryFromArchive = {
    val zippedAspellDictionary = new File(getClass.getClassLoader.getResource("aspell_dictionary.zip").getFile)

    val zipFile = new ZipFile(zippedAspellDictionary)
    val entry = zipFile.getEntry("aspell_dictionary")

    zipFile.getInputStream(entry)
  }

  private def addToMap(map: mutable.HashMap[String, Set[String]]): ((String, String)) => Unit = {
    case (key, value) =>
      map.put(key, map.getOrElse(key, Set()) + value)
  }

  private def getDifferenceInSuffixesOfInflectedWord(results: Seq[WordData], caseName: String,
                                                     numberPredicate: WordData => Boolean): Option[(String, String)] = {
    val wordInNominativus = results.find(result => getCases(result).contains("nom") &&
      numberPredicate(result)).map(_.getWord.toString.toLowerCase)
    val inflectedName = results.find(result => getCases(result).contains(caseName) &&
      numberPredicate(result)).map(_.getWord.toString.toLowerCase)

    if (wordInNominativus.isDefined && inflectedName.isDefined) {
      val commonPrefix = longestCommonPrefix(inflectedName.get, wordInNominativus.get)
      Some((remainderOfString(wordInNominativus.get, commonPrefix),
        remainderOfString(inflectedName.get, commonPrefix)))
    } else {
      None
    }
  }

  private def remainderOfString(subject: String, prefixToRemove: String): String = {
    subject.substring(Math.max(0, prefixToRemove.length - 2))
  }

  private def wiseLookup(word: String): Seq[WordData] = {
    polishStemmer.lookup(word.toLowerCase).asScala.toList :::
      polishStemmer.lookup(word.toLowerCase.capitalize).asScala.toList
  }

  private val CASES = Set("nom", "gen", "acc", "dat", "inst", "loc", "voc")

  private val supplementaryGenetivusSuffixes = Map(
    "ski" -> Set("skiego"),
    "ska" -> Set("skiej"),
    "skie" -> Set("skiego"),
    "nia" -> Set("ni"),
    "ów" -> Set("owa"),
    "ór" -> Set("oru"),
    "wa" -> Set("wy"),
    "na" -> Set("ny"),
    "ko" -> Set("ka"),
    "no" -> Set("na"),
    "ik" -> Set("ika"),
    "ice" -> Set("ic"),
    "ła" -> Set("łej"),
    "cha" -> Set("chej"),
    "rze" -> Set("rza"),
    "arz" -> Set("arza"),
    "ica" -> Set("icy"),
    "cki" -> Set("ckiego"),
    "cka" -> Set("ckiej"),
    "ckie" -> Set("ckiego"),
    "icz" -> Set("icza"),
    "ia" -> Set("iej"),
    "cz" -> Set("cza"),
    "ka" -> Set("ki"),
    "cie" -> Set("cia"),
    "ta" -> Set("ty"),
    "prz" -> Set("prza"),
    "dziec" -> Set("dźca"),
    "iec" -> Set("ca"),
    "aw" -> Set("awia"),
    "la" -> Set("li"),
    "sk" -> Set("ska"),
    "zu" -> Set("zów"),
    "wo" -> Set("wa"),
    "in" -> Set("ina"),
    "yce" -> Set("yc"),
    "lce" -> Set("lc"),
    "sto" -> Set("sta"),
    "yśl" -> Set("yśla"),
    "iąż" -> Set("iąża"),
    "mża" -> Set("mży"),
    "słą" -> Set("słą"), // "X nad Wisłą"?
    "zyń" -> Set("zynia"),
    "tyń" -> Set("tynia"),
    "om" -> Set("omia")
  )

  private val supplementaryLocativusSuffixes = Map(
    "ski" -> Set("skim", "skiem"),
    "ska" -> Set("skiej"),
    "skie" -> Set("skim", "skiem"),
    "nia" -> Set("ni"),
    "ów" -> Set("owie"),
    "ór" -> Set("orze"),
    "wa" -> Set("wie", "wej"),
    "na" -> Set("nie"),
    "ko" -> Set("ku"),
    "no" -> Set("nie"),
    "ik" -> Set("iku"),
    "ice" -> Set("icach"),
    "ła" -> Set("łej"),
    "sła" -> Set("śle"),
    "cha" -> Set("sze"),
    "rze" -> Set("rzu"),
    "arz" -> Set("arzu"),
    "ica" -> Set("icy"),
    "cki" -> Set("ckim", "ckiem"),
    "cka" -> Set("ckiej"),
    "ckie" -> Set("ckim", "ckiem"),
    "icz" -> Set("iczu"),
    "ia" -> Set("i", "ii"),
    "cz" -> Set("czu"),
    "ka" -> Set("ce"),
    "cie" -> Set("ciu"),
    "ta" -> Set("cie"),
    "prz" -> Set("przu"),
    "dziec" -> Set("dźcu"),
    "iec" -> Set("cu"),
    "aw" -> Set("awiu"),
    "la" -> Set("li"),
    "sk" -> Set("sku"),
    "zu" -> Set("zie"),
    "wo" -> Set("wie"),
    "in" -> Set("inie"),
    "yce" -> Set("ycach"),
    "lce" -> Set("lcach"),
    "sto" -> Set("ście"),
    "yśl" -> Set("yślu"),
    "iąż" -> Set("iążu"),
    "mża" -> Set("mży"),
    "słą" -> Set("słą"), // "X nad Wisłą"?
    "zyń" -> Set("zyniu"),
    "tyń" -> Set("tyniu"),
    "lkie" -> Set("lkich"),
    "ród" -> Set("rodzie"),
    "rne" -> Set("rnym", "rnem"),
    "lne" -> Set("lnym", "lnem"),
    "ądz" -> Set("ądzu"),
    "leń" -> Set("leniu"),
    "uty" -> Set("utach"),
    "owe" -> Set("owem", "owych"),
    "om" -> Set("omiu")
  )

  private val supplementarySuffixes = Map(
    "gen" -> supplementaryGenetivusSuffixes,
    "loc" -> supplementaryLocativusSuffixes
  )
}

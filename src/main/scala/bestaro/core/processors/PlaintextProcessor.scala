package bestaro.core.processors

import bestaro.core.RawRecord
import morfologik.stemming.WordData
import morfologik.stemming.polish.PolishStemmer

import collection.JavaConverters._

class PlaintextProcessor {

  private val stemmer = new PolishStemmer

  def process(record: RawRecord): RawRecord = {
    val inputText = record.message
    val tokens = Option(inputText)
      // remove everything except letters, numbers and white spaces
      .map(_.replaceAll("[^.0-9a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ ]", " "))
      .map(_.replaceAll("\\.", ". "))
      .map(_.split("\\s+").toList)
      .getOrElse(List())

    val cleanedText = tokens.map { token =>
      val matchedStems = stemmer.lookup(token)
      if (matchedStems.isEmpty || isExcludedFromMorfologik(token)) {
        token
      } else {
        mostAccurateStem(matchedStems)
      }
    }.mkString(" ")

    println("########################")
    println(inputText)
    println(cleanedText)

    record.copy()
  }

  private def mostAccurateStem(results: java.util.List[WordData]): String = {
    val stems = results.asScala
      .map(_.getStem.toString)

    stems.find(stem => stem.endsWith("y")).getOrElse(stems.head)
  }

  private def isExcludedFromMorfologik(word: String): Boolean = {
    Set("w", "i", "m") contains word.toLowerCase
  }
}

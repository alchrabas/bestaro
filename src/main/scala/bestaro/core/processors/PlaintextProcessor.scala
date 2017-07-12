package bestaro.core.processors

import bestaro.core.RawRecord
import morfologik.stemming.polish.PolishStemmer

import collection.JavaConverters._

class PlaintextProcessor {

  private val stemmer = new PolishStemmer

  def process(record: RawRecord): RawRecord = {
    val inputText = record.message
    val tokens = Option(inputText)
      // remove everything except letters, numbers and white spaces
      .map(_.replaceAll("[^0-9a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ ]", ""))
      .map(_.split(" ").toList)
      .getOrElse(List())

    val cleanedText = tokens.map { token =>
      val results = stemmer.lookup(token)
      if (results.isEmpty) {
        token
      } else {
        results.get(0).getStem.toString
      }
    }.mkString(" ")

    println("########################")
    println(inputText)
    println(cleanedText)

    record.copy()
  }
}

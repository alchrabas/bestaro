package bestaro.core.processors

import java.util

import bestaro.core.RawRecord
import morfologik.stemming.WordData

import collection.JavaConverters._
import scala.collection.mutable.ListBuffer


object PlaintextProcessor {

  implicit class TokenListOps(private val tokens: List[Token]) extends AnyVal {
    def slidingPrefixedByEmptyTokens(size: Int): Iterator[List[Token]] = {
      (List.fill(size - 1)(EMPTY_TOKEN) ++ tokens).sliding(size)
    }
  }

  private val EMPTY_TOKEN = Token("", "", "", 0)
}

class PlaintextProcessor {

  private val gusData = new GusDataReader
  private val streets = gusData.streetsInKrakow
  private val streetsByFirstStemmedWord: Map[String, Seq[StreetEntry]] = streets.groupBy(_.stemmedName
    .split(" ")(0))
  private val streetsByFirstSimpleWord: Map[String, Seq[StreetEntry]] = streets.groupBy(_.strippedName
    .split(" ")(0))

  def process(record: RawRecord): RawRecord = {
    val inputText = record.message
    val tokens = Option(inputText)
      // remove everything except letters, numbers, dots, commas and white spaces
      .map(stripLinks)
      .map(_.replaceAll("[^.,!0-9a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ ]", " "))
      .map(_.replaceAll("([.,!])", "$1 "))
      .map(_.split("\\s+").toList)
      .getOrElse(List())

    var stemmedTokens = tokens.map { tokenText =>
      if (isLocationNameTrait(baseNameProducer.strippedForStemming(tokenText),
        baseNameProducer.getBestBaseName(tokenText))) {
        Token(tokenText,
          baseNameProducer.strippedForStemming(tokenText),
          baseNameProducer.getBestBaseName(tokenText), 5)
      } else {
        evaluateMostAccurateBaseName(tokenText)
      }
    }.map { a =>
      if (isCapitalized(a.original)) {
        a.copy(value = a.value + 5)
      } else {
        a
      }
    }

    println("########################")

    stemmedTokens = updateTokenEvaluationUsingContext(stemmedTokens)
    val mutableTokens = stemmedTokens.to[ListBuffer]
    for (idx <- mutableTokens.indices) {
      if (streetsByFirstSimpleWord.contains(mutableTokens(idx).stripped)) {
        streetsByFirstSimpleWord(mutableTokens(idx).stripped).find(streetEntry =>
          managedToMatchStrippedStreetName(mutableTokens, idx, streetEntry))
      } else if (streetsByFirstStemmedWord.contains(mutableTokens(idx).stem)) {
        streetsByFirstStemmedWord(mutableTokens(idx).stem).find(streetEntry =>
          managedToMatchStemmedStreetName(mutableTokens, idx, streetEntry))
      }
    }
    stemmedTokens = mutableTokens.toList

    println(inputText)
    println(" ====> ")
    println(stemmedTokens.mkString(" "))
    println("CANDIDATES: " + stemmedTokens.sortBy(_.value).reverse.slice(0, 3))

    record.copy()
  }

  private def managedToMatchStemmedStreetName(mutableTokens: ListBuffer[Token],
                                              idx: Int, streetEntry: StreetEntry): Boolean = {
    managedToExtract(mutableTokens, idx, streetEntry, _.stemmedName, _.stem)
  }

  private def managedToMatchStrippedStreetName(mutableTokens: ListBuffer[Token],
                                               idx: Int, streetEntry: StreetEntry): Boolean = {
    managedToExtract(mutableTokens, idx, streetEntry, _.strippedName, _.stripped)
  }

  private def managedToExtract(mutableTokens: ListBuffer[Token],
                               idx: Int, streetEntry: StreetEntry,
                               streetProperty: StreetEntry => String,
                               tokenProperty: Token => String): Boolean = {
    if (matching(mutableTokens, idx, streetEntry, streetProperty, tokenProperty)) {
      for (wordToReplace <- streetProperty(streetEntry).split(" ").indices) {
        increaseValue(mutableTokens, idx + wordToReplace)
        return true
      }
    }
    false
  }

  private def increaseValue(mutableTokens: ListBuffer[Token], idx: Int) = {
    mutableTokens(idx) = mutableTokens(idx).copy(value = mutableTokens(idx).value + 10)
  }

  private def matching(tokens: ListBuffer[Token],
                       firstTokenPos: Int,
                       street: StreetEntry,
                       streetProperty: StreetEntry => String,
                       tokenProperty: Token => String
                      ): Boolean = {
    val streetSimpleWords = streetProperty(street).split(" ")
    for (wordId <- streetSimpleWords.indices) {
      if (tokens.length > firstTokenPos + wordId) {
        if (tokenProperty(tokens(firstTokenPos + wordId)) != streetSimpleWords(wordId)) {
          return false
        }
      }
    }
    true
  }

  private def stripLinks(text: String): String = {
    text.replaceAll("http[^\\s+]+", "")
  }

  private def updateTokenEvaluationUsingContext(tokens: List[Token]): List[Token] = {
    import PlaintextProcessor._
    tokens.slidingPrefixedByEmptyTokens(2).map(a => {
      if (isLocationNameTrait(a.head)) {
        a.last.copy(value = a.last.value + 5)
      } else {
        a.last
      }
    }).toList.slidingPrefixedByEmptyTokens(2).map(a => {
      if (isLocationSpecificPreposition(a.head)) {
        a.last.copy(value = a.last.value + 5)
      } else {
        a.last
      }
    }).toList.slidingPrefixedByEmptyTokens(3).map(a => {
      val isPrepositionFollowedByKind = isLocationSpecificPreposition(a.head) && isLocationNameTrait(a(1))
      if (isPrepositionFollowedByKind) {
        a.last.copy(value = a.last.value + 5)
      } else {
        a.last
      }
    }).toList
  }

  private def isLocationNameTrait(token: Token): Boolean = {
    isLocationNameTrait(token.stripped, token.stem)
  }

  private def isLocationNameTrait(stripped: String, stem: String): Boolean = {
    (Set("ul.", "pl.", "os.", "al.") contains stripped) ||
      (Set("plac", "ulica", "osiedle", "aleja") contains stem)
  }

  private def isLocationSpecificPreposition(token: Token): Boolean = {
    (Set("w", "we", "nad", "na") contains token.stripped) ||
      (Set("okolica", "pobliże") contains token.stem)
  }

  private val baseNameProducer = new BaseNameProducer

  private def evaluateMostAccurateBaseName(original: String): Token = {
    val strippedText = baseNameProducer.strippedForStemming(original)
    baseNameProducer.maybeBestBaseName(original) match {
      case Some(stemmedText) => Token(original,
        strippedText,
        stemmedText, 1)
      case None => Token(original,
        strippedText,
        strippedText, 0)
    }
  }

  private def isLocativus(results: util.List[WordData]) = {
    results.asScala.exists(a => {
      a.getTag.toString.split("[:+]").contains("loc")
    })
  }

  private def isCapitalized(original: String): Boolean = {
    !original.isEmpty && original(0).isUpper
  }

}

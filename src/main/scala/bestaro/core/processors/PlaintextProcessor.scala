package bestaro.core.processors

import java.util

import bestaro.core.RawRecord
import morfologik.stemming.WordData
import collection.JavaConverters._


object PlaintextProcessor {

  implicit class TokenListOps(private val tokens: List[Token]) extends AnyVal {
    def slidingPrefixedByEmptyTokens(size: Int): Iterator[List[Token]] = {
      (List.fill(size - 1)(EMPTY_TOKEN) ++ tokens).sliding(size)
    }
  }

  private val EMPTY_TOKEN = Token("", "", 0)
}

class PlaintextProcessor {

  def process(record: RawRecord): RawRecord = {
    val inputText = record.message
    val tokens = Option(inputText)
      // remove everything except letters, numbers, dots, commas and white spaces
      .map(stripLinks)
      .map(_.replaceAll("[^.,!0-9a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ ]", " "))
      .map(_.replaceAll("([.,!])", "$1 "))
      .map(_.split("\\s+").toList)
      .getOrElse(List())

    var stemmedTokens = tokens.map { token =>
      if (isLocationNameTrait(Token(token, token.toLowerCase, 0))) {
        Token(token, token.toLowerCase, 5)
      } else {
        evaluateMostAccurateBaseName(token)
      }
    }.map { a =>
      if (isCapitalized(a.original)) {
        a.copy(value = a.value + 2)
      } else {
        a
      }
    }

    stemmedTokens = updateTokenEvaluationUsingContext(stemmedTokens)

    println("########################")
    println(inputText)
    println(" ====> ")
    println(stemmedTokens.mkString(" "))
    println("CANDIDATES: " + stemmedTokens.sortBy(_.value).reverse.slice(0, 3))

    record.copy()
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
      if (isLocationSpecificPreposition(a.head) && isLocationNameTrait(a(1))) {
        a.last.copy(value = a.last.value + 5)
      } else {
        a.last
      }
    }).toList
  }

  private def isLocationNameTrait(token: Token): Boolean = {
    (Set("ul.", "pl.", "os.", "al.") contains token.original.toLowerCase) ||
      (Set("plac", "ulica", "osiedle", "aleja") contains token.stem)
  }

  private def isLocationSpecificPreposition(token: Token): Boolean = {
    (Set("w", "we", "nad", "na") contains token.original.toLowerCase) ||
      (Set("okolica", "pobliże") contains token.stem)
  }

  private val baseNameProducer = new BaseNameProducer

  private def evaluateMostAccurateBaseName(original: String): Token = {
    baseNameProducer.getBestBaseName(original) match {
      case Some(value) => Token(original, value, 1)
      case None => Token(original, original.toLowerCase, 0)
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

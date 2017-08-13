package bestaro.extractors

import bestaro.core.processors.{Flag, Token}

case class MultiWordName(tokens: List[Token], startIndex: Int) {
  def pushWord(token: Token): MultiWordName = {
    copy(tokens = tokens :+ token)
  }

  def sumScore: Int = {
    tokens.map(_.placenessScore).sum
  }

  def wordsCount: Int = {
    tokens.size
  }

  def stripped: String = {
    tokens.map(_.stripped).mkString(" ")
  }

  def original: String = {
    tokens.map(_.original).mkString(" ")
  }

  def stemmed: String = {
    tokens.map(_.stem).mkString(" ")
  }
}

class MultiWordLocationNameExtractor {

  def mostSuitableMultiWordNames(tokens: List[Token]): List[MultiWordName] = {

    tokens.zipWithIndex
      .filter(_._1.placenessScore >= 5)
      .sortBy(_._1.placenessScore)
      .reverse.slice(0, 5).map {
      case (firstToken, startIndex) =>
        MultiWordName(List(firstToken), startIndex)
    }.map {
      firstWord =>
        val tokensToUse = tokens.slice(firstWord.startIndex, firstWord.startIndex + 4)

        if (tokensToUse.size == 1) {
          firstWord
        } else {
          tokensToUse.sliding(2).map(listToTuple).takeWhile {
            case (previousToken, currentToken) =>
              currentToken.placenessScore >= 5 &&
                !(previousToken.original.endsWith(",") ||
                  previousToken.isEndOfSentence)
          }.map(_._2).foldLeft(firstWord)(_.pushWord(_))
        }
    }.sortBy(_.sumScore).reverse
  }

  private def listToTuple(tokens: List[Token]): (Token, Token) = {
    assert(tokens.size == 2)
    (tokens.head, tokens.last)
  }
}

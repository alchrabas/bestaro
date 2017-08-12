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
    import scala.util.control.Breaks._

    val multiWordNames = tokens.zipWithIndex
      .filter(_._1.placenessScore >= 5)
      .sortBy(_._1.placenessScore)
      .reverse.slice(0, 5).map {
      case (firstToken, startIndex) =>
        MultiWordName(List(firstToken), startIndex)
    }.map {
      firstWord =>
        var multiWordName = firstWord
        breakable {
          for (i <- firstWord.startIndex + 1 to firstWord.startIndex + 3 if i < tokens.size) {
            val candidateToken = tokens(i)
            val previousToken = tokens(i - 1)
            if (candidateToken.placenessScore < 5 || previousToken.original.endsWith(",") ||
              (previousToken.original.endsWith(".") && !previousToken.flags.contains(Flag.PUNCTUATED_WORD))) {
              break
            }
            multiWordName = multiWordName.pushWord(tokens(i))
          }
        }
        multiWordName
    }.sortBy(_.sumScore).reverse
    //    println(multiWordNames.map(a => a.tokens.map(_.original).mkString(" ") + " = " + a.sumScore).mkString("\n"))

    multiWordNames
  }
}

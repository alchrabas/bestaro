package bestaro.core

class Tokenizer {

  def tokenize(inputText: String): List[String] = {
    Option(inputText)
      // remove everything except letters, numbers, dots, commas and white spaces
      .map(stripLinks)
      .map(_.replaceAll("[^.,!0-9a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ ]", " "))
      .map(_.replaceAll("([.,!])", "$1 "))
      .map(_.split("\\s+").toList)
      .getOrElse(List())
  }

  private def stripLinks(text: String): String = {
    text.replaceAll("http[^\\s+]+", "")
  }
}

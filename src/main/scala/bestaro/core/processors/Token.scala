package bestaro.core.processors

case class PartOfSpeech(name: String)

object PartOfSpeech {
  val ADJECTIVE = PartOfSpeech("adjective")
  val NOUN = PartOfSpeech("noun")
  val PREPOSITION = PartOfSpeech("preposition")
  val OTHER = PartOfSpeech("other")
}

case class Token(
                  original: String,
                  stripped: String,
                  stem: String,
                  partsOfSpeech: List[PartOfSpeech],
                  placenessScore: Int
                ) {
  override def toString: String = {
    original + " (" + stem + ")[" + placenessScore + "]"
  }
}

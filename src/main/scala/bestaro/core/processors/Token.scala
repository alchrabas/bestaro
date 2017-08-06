package bestaro.core.processors

case class PartOfSpeech(name: String)

object PartOfSpeech {
  val ADJECTIVE = PartOfSpeech("adjective")
  val NOUN = PartOfSpeech("noun")
  val PREPOSITION = PartOfSpeech("preposition")
  val VERB = PartOfSpeech("verb")
  val OTHER = PartOfSpeech("other")
}

case class Gender(symbol: String)

object Gender {
  val M = Gender("masculine")
  val F = Gender("feminine")
  val N = Gender("neuter")
}

case class Token(
                  original: String,
                  stripped: String,
                  stem: String,
                  partsOfSpeech: List[PartOfSpeech],
                  genders: List[Gender],
                  placenessScore: Int
                ) {
  override def toString: String = {
    original + " (" + stem + ")[" + placenessScore + "]"
  }
}

package bestaro.core.processors

case class Token(
                  original: String,
                  stripped: String,
                  stem: String,
                  placenessScore: Int
                ) {
  override def toString: String = {
    original + " (" + stem + ")[" + placenessScore + "]"
  }
}

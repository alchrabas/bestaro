package bestaro.core.processors

case class Token(
                  original: String,
                  stripped: String,
                  stem: String,
                  value: Int
                ) {
  override def toString: String = {
    original + " (" + stem + ")[" + value + "]"
  }
}

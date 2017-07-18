package bestaro.core.processors

case class Token(
                  original: String,
                  stem: String,
                  value: Int
                ) {
  override def toString: String = {
    stem + "[" + value + "]"
  }
}

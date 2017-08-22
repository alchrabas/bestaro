package bestaro.core.processors

import bestaro.service.Voivodeship

case class Location(stripped: String, original: String, kind: String, voivodeship: Option[Voivodeship] = None)

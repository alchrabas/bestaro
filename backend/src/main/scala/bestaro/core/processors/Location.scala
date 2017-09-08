package bestaro.core.processors

import bestaro.service.Voivodeship

case class Location(stripped: String, original: String, kind: LocationType,
                    voivodeship: Option[Voivodeship] = None, parent: Option[Location] = None)

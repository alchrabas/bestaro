package bestaro.core

import bestaro.core.processors.BaseNameProducer
import bestaro.helpers.TaggedRecordsManager.TaggedRecord
import bestaro.util.PolishCharactersAsciizer

import scala.collection.mutable.ListBuffer

case class VerificationResult(success: Int, all: Int, invalidPairs: Seq[InvalidPair]) {
  override def toString: String = {
    val successfulPercent = success * 100.0 / all
    invalidPairs.mkString("\n") + "\n\n" + f"Verification: $success / $all = $successfulPercent%1.2f%%"
  }
}

case class InvalidPair(record: RawRecord, expectedLocations: Seq[String], expectedCities: Seq[String]) {
  override def toString: String = {
    s"Expected $expectedLocations, in $expectedCities, but found ${record.location}"
  }
}

class LocationVerifier(recordTags: Map[RecordId, TaggedRecord]) {

  def verify(processed: Seq[RawRecord]): VerificationResult = {

    var successfulMatches = 0
    var allLocations = 0
    val invalidPairs = new ListBuffer[InvalidPair]
    processed.foreach { rawRecord =>
      val recordTag = recordTags.get(rawRecord.recordId)
      if (recordTag.exists(r => (r.locs ++ r.altLocs ++ r.cities).nonEmpty)) {
        allLocations += 1
        recordTag.map {
          taggedRecord =>
            val successfulMatch = rawRecord.location != null && anyLocMatches(taggedRecord, rawRecord.location)
            if (!successfulMatch) {
              invalidPairs.append(InvalidPair(rawRecord, taggedRecord.locs ++ taggedRecord.altLocs, taggedRecord.cities))
            }
            successfulMatch
        }.filter(_ == true).foreach(_ => successfulMatches += 1)
      }
    }

    VerificationResult(successfulMatches, allLocations, invalidPairs)
  }

  private def anyLocMatches(taggedRecord: TaggedRecord, location: String): Boolean = {
    val foundLocation = (taggedRecord.locs ++ taggedRecord.altLocs)
      .map(stripForVerification)
      .contains(
        stripForVerification(location)
      )
    val foundCityAndNoMoreAccurateLocation = (taggedRecord.locs.isEmpty && taggedRecord.altLocs.isEmpty) &&
      taggedRecord.cities
        .map(stripForVerification)
        .contains(
          stripForVerification(location))

    foundLocation || foundCityAndNoMoreAccurateLocation
  }

  private val baseNameProducer = new BaseNameProducer
  private val asciizer = new PolishCharactersAsciizer

  private def stripForVerification(name: String): String = {
    asciizer.convertToAscii(
      baseNameProducer.strippedForStemming(
        name.replaceAll("-", " ")))
  }
}

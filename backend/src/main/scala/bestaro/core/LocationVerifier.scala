package bestaro.core

import bestaro.common.util.PolishCharactersAsciizer
import bestaro.common.{FullLocation, RecordId}
import bestaro.core.processors.BaseNameProducer
import bestaro.helpers.TaggedRecordsManager.TaggedRecord

import scala.collection.mutable.ListBuffer

case class VerificationResult(success: Int, all: Int, resultPairs: Seq[ResultPair]) {
  override def toString: String = {
    val successfulPercent = success * 100.0 / all
    "Successful: \n" + resultPairs.filter(_.matched).mkString("\n") +
      "\n#################################\n" +
      "Failed: \n" + resultPairs.filter(!_.matched).mkString("\n") + "\n\n" +
      f"Verification: $success / $all = $successfulPercent%1.2f%%"
  }
}

case class ResultPair(record: RawRecord, expectedLocations: Seq[String], expectedCities: Seq[String], matched: Boolean) {
  override def toString: String = {
    s"Expected $expectedLocations, in $expectedCities, found ${record.fullLocation.primary.map(_.stripped)}, ${record.fullLocation.secondary.map(_.stripped)}"
  }
}

class LocationVerifier(recordTags: Map[RecordId, TaggedRecord]) {

  def verify(processed: Seq[RawRecord]): VerificationResult = {

    val resultPairs = new ListBuffer[ResultPair]
    processed.foreach { rawRecord =>
      val recordTag = recordTags.get(rawRecord.recordId)
      if (recordTag.exists(r => (r.locs ++ r.altLocs ++ r.cities).nonEmpty)) {
        recordTag.map {
          taggedRecord =>
            val successfulMatch = anyLocMatches(taggedRecord, rawRecord.fullLocation)
            ResultPair(rawRecord, taggedRecord.locs ++ taggedRecord.altLocs, taggedRecord.cities, successfulMatch)
        }.foreach(resultPairs.append(_))
      }
    }

    VerificationResult(resultPairs.count(_.matched), resultPairs.size, resultPairs)
  }

  private def anyLocMatches(taggedRecord: TaggedRecord, fullLocation: FullLocation): Boolean = {
    val locationMatches =
      (taggedRecord.locs.isEmpty && taggedRecord.altLocs.isEmpty) ||
        (fullLocation.primary.isDefined &&
          (taggedRecord.locs ++ taggedRecord.altLocs)
            .map(stripForVerification)
            .contains(
              stripForVerification(fullLocation.primary.get.stripped)))

    val townMatches =
      taggedRecord.cities.isEmpty ||
        (fullLocation.secondary.isDefined &&
          taggedRecord.cities
            .map(stripForVerification)
            .contains(
              stripForVerification(fullLocation.secondary.get.stripped)))

    locationMatches && townMatches
  }

  private val baseNameProducer = new BaseNameProducer
  private val asciizer = new PolishCharactersAsciizer

  private def stripForVerification(name: String): String = {
    asciizer.convertToAscii(
      baseNameProducer.strippedForStemming(name)
    )
  }
}

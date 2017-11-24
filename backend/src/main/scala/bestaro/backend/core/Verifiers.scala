package bestaro.backend.core

import bestaro.common.types.RecordId
import bestaro.backend.helpers.TaggedRecordsManager.TaggedRecord
import bestaro.backend.types.RawRecord
import bestaro.locator.types.FullLocation
import bestaro.locator.util.{BaseNameProducer, PolishCharactersAsciizer}

import scala.collection.mutable.ListBuffer

case class VerificationResult(resultPairs: Seq[ResultPair]) {
  val success: Int = resultPairs.count(_.matched)
  val all: Int = resultPairs.size

  def briefSummary: String = {
    val successfulPercent = success * 100.0 / all
    f"Verification: $success / $all = $successfulPercent%1.2f%%"
  }

  def detailedSummary: String = {
    "Successful: \n" + resultPairs.filter(_.matched).mkString("\n") +
      "\n#################################\n" +
      "Failed: \n" + resultPairs.filter(!_.matched).mkString("\n") + "\n\n" +
      briefSummary
  }

  override def toString: String = detailedSummary

}

sealed trait ResultPair {
  def matched: Boolean
}

case class LocationResultPair(record: RawRecord,
                              expectedLocations: Seq[String], expectedCities: Seq[String],
                              matched: Boolean) extends ResultPair {
  override def toString: String = {
    s"Expected $expectedLocations, in $expectedCities, found ${record.fullLocation.primary.map(_.stripped)}, ${record.fullLocation.secondary.map(_.stripped)}"
  }
}

case class EventTypeResultPair(record: RawRecord, expectedType: String, matched: Boolean) extends ResultPair {
  override def toString: String = {
    s"Expected $expectedType, found ${record.eventType}; ${(record.message + " " + record.secondaryMessage).slice(0, 30)}..."
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
            LocationResultPair(rawRecord, taggedRecord.locs ++ taggedRecord.altLocs, taggedRecord.cities, successfulMatch)
        }.foreach(resultPairs.append(_))
      }
    }

    VerificationResult(resultPairs)
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

class EventTypeVerifier(recordTags: Map[RecordId, TaggedRecord]) {
  def verify(processed: Seq[RawRecord]): VerificationResult = {
    val resultPairs = new ListBuffer[ResultPair]
    processed.foreach { rawRecord =>
      val recordTag = recordTags.get(rawRecord.recordId)
      recordTag.map(_.eventType).map(seenToFound).foreach { expectedType =>
        val correct = expectedType == rawRecord.eventType.entryName
        resultPairs.append(EventTypeResultPair(rawRecord, expectedType, correct))
      }
    }

    VerificationResult(resultPairs)
  }

  private def seenToFound(eventType: String): String = {
    if (eventType == "SEEN") {
      "FOUND"
    } else {
      eventType
    }
  }
}

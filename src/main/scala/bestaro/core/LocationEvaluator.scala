package bestaro.core

import bestaro.core.processors.BaseNameProducer
import bestaro.helpers.TaggedRecordsManager.TaggedRecord

import scala.collection.mutable.ListBuffer

case class EvaluationResult(success: Int, all: Int, invalidPairs: Seq[InvalidPair]) {
  override def toString: String = {
    val successfulPercent = success * 100.0 / all
    invalidPairs.mkString("\n") + "\n\n" + f"Evaluation: $success / $all = $successfulPercent%1.2f%%"
  }
}

case class InvalidPair(actual: String, expected: Seq[String]) {
  override def toString: String = {
    s"Expected $expected, but found $actual"
  }
}

class LocationEvaluator(recordTags: Map[RecordId, TaggedRecord]) {

  def evaluate(processed: Seq[RawRecord]): EvaluationResult = {

    var successfulMatches = 0
    var allLocations = 0
    val invalidPairs = new ListBuffer[InvalidPair]
    processed.foreach { rawRecord =>
      val recordTag = recordTags.get(rawRecord.recordId)
      if (recordTag.exists(_.locs.nonEmpty)) {
        allLocations += 1
        recordTag.map {
          taggedRecord =>
            val successfulMatch = rawRecord.location != null && anyLocMatches(taggedRecord, rawRecord.location)
            if (!successfulMatch) {
              invalidPairs.append(InvalidPair(rawRecord.location, taggedRecord.locs ++ taggedRecord.altLocs))
            }
            successfulMatch
        }.filter(_ == true).foreach(_ => successfulMatches += 1)
      }
    }

    EvaluationResult(successfulMatches, allLocations, invalidPairs)
  }

  protected val baseNameProducer = new BaseNameProducer

  private def anyLocMatches(taggedRecord: TaggedRecord, location: String): Boolean = {
    (taggedRecord.locs ++ taggedRecord.altLocs)
      .map(baseNameProducer.strippedForStemming)
      .contains(baseNameProducer.strippedForStemming(location))
  }
}

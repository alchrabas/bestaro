package bestaro.core

import bestaro.core.processors.MatchedStreet
import bestaro.helpers.TaggedRecordsManager.TaggedRecord

import scala.collection.mutable.ListBuffer

case class EvaluationResult(success: Int, all: Int, invalidPairs: Seq[InvalidPair]) {
  override def toString: String = {
    val successfulPercent = success * 100.0 / all
    f"Evaluation: $success / $all = $successfulPercent%1.2f%%"
  }
}

case class InvalidPair(actual: Seq[MatchedStreet], expected: Seq[String])

class LocationEvaluator(taggedRecords: Map[RecordId, TaggedRecord]) {

  def evaluate(processed: Seq[(RecordId, List[MatchedStreet])]): Unit = {

    var successfulMatches = 0
    var allLocations = 0
    val invalidPairs = new ListBuffer[InvalidPair]
    processed.foreach { case (id, matchedStreets) =>
      val record = taggedRecords.get(id)
      if (record.exists(_.locs.nonEmpty)) {
        allLocations += 1
        record.map {
          taggedRecord =>
            val successfulMatch = anyLocMatches(taggedRecord, matchedStreets)
            if (!successfulMatch) {
              invalidPairs.append(InvalidPair(matchedStreets, taggedRecord.locs ++ taggedRecord.altLocs))
            }
            successfulMatch
        }.filter(_ == true).foreach(_ => successfulMatches += 1)
      }
    }
  }

  private def anyLocMatches(taggedRecord: TaggedRecord, tokens: List[MatchedStreet]): Boolean = {
    taggedRecord.locs.map(_.toLowerCase).exists(expectedLoc => tokens.exists(_.street.strippedName == expectedLoc))
  }
}

package bestaro.core

import bestaro.DataSupplier
import bestaro.collectors.util.SlowHttpDownloader
import bestaro.collectors.{FacebookCollector, OlxCollector}
import bestaro.common.types.RecordId
import bestaro.core.processors.{LocationStringProcessor, PlaintextProcessor}
import bestaro.database.DatabaseWrapper
import bestaro.helpers.TaggedRecordsManager
import bestaro.helpers.TaggedRecordsManager.TaggedRecord

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object Main {

  val FB = "FB"
  val OLX = "OLX"
  val PROCESS = "PROCESS"

  val OPTION = PROCESS

  def main(args: Array[String]): Unit = {

    OPTION match {
      case FB =>
        val fb = new FacebookCollector(DatabaseWrapper.saveRawRecord, DatabaseWrapper.rawRecordAlreadyExists)
        fb.collect()
      case OLX =>
        val olx = new OlxCollector(DatabaseWrapper.saveRawRecord, new SlowHttpDownloader)
        olx.collect()
      case PROCESS =>
        val records = DatabaseWrapper.allRawRecords

        val locationStringProcessor = new LocationStringProcessor
        val plaintextProcessor = new PlaintextProcessor
        //                val recordsWithTags = records.filter(r => taggedRecords.contains(r.recordId))

        import scala.concurrent.ExecutionContext.Implicits.global

        val (allFutures, processedRecords) = records.toIterator
          .filterNot(rawRecord => DatabaseWrapper.processedLaterThanCollected(rawRecord.recordId))
          .map(locationStringProcessor.process)
          .map(plaintextProcessor.process)
          .map { processedRecord =>
            val future = DatabaseWrapper.saveProcessedRecord(processedRecord.buildRecord)
            (future, processedRecord)
          }.toSeq.unzip

        val locationVerifier = new LocationVerifier(getLocationTaggedRecords)
        println(locationVerifier.verify(processedRecords).detailedSummary)
        val eventTypeVerifier = new EventTypeVerifier(getEventTaggedRecords)
        println(eventTypeVerifier.verify(processedRecords).briefSummary)

        Await.result(Future.sequence(allFutures), Duration.Inf)

        val dataSupplier = new DataSupplier
              processedRecords
                .map(_.buildRecord)
                .filterNot(record => DatabaseWrapper.sentLaterThanProcessed(record.recordId))
                .foreach { record =>
                  dataSupplier.sendRecord(record)
                  DatabaseWrapper.markRecordAsSent(record)
                }
    }
  }

  private def getLocationTaggedRecords: Map[RecordId, TaggedRecord] = {
    TaggedRecordsManager.allLocationRecordsFromCsv().map(tr => tr.recordId -> tr).toMap
  }

  private def getEventTaggedRecords: Map[RecordId, TaggedRecord] = {
    TaggedRecordsManager.allEventTypeRecordsFromCsv().map(tr => tr.recordId -> tr).toMap
  }
}

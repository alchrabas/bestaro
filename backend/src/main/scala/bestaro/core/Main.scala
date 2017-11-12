package bestaro.core

import bestaro.{AppConfig, DataSupplier}
import bestaro.collectors.util.SlowHttpDownloader
import bestaro.collectors.{FacebookCollector, OlxCollector}
import bestaro.common.types.RecordId
import bestaro.core.processors.{LocationStringProcessor, PlaintextProcessor}
import bestaro.database.DatabaseWrapper
import bestaro.helpers.TaggedRecordsManager
import bestaro.helpers.TaggedRecordsManager.TaggedRecord
import bestaro.locator.LocatorDatabase

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object Main {

  val FB = "FB"
  val OLX = "OLX"
  val PROCESS = "PROCESS"
  val SEND = "SEND"

  val OPTION = SEND

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

        val locatorDatabase = new LocatorDatabase(AppConfig.getProperty("locatorDatabasePath"))
        val locationStringProcessor = new LocationStringProcessor(locatorDatabase)
        val plaintextProcessor = new PlaintextProcessor(locatorDatabase)
        //                val recordsWithTags = records.filter(r => taggedRecords.contains(r.recordId))

        import scala.concurrent.ExecutionContext.Implicits.global

        val forceProcessing = AppConfig.getProperty("forceProcessing") == "true"

        val (allFutures, processedRecords) = records.toIterator
          .filter(rawRecord => forceProcessing ||
            !DatabaseWrapper.processedLaterThanCollected(rawRecord.recordId))
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
      case SEND =>
        val dataSupplier = new DataSupplier
        DatabaseWrapper.allNotSentProcessedRecords
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

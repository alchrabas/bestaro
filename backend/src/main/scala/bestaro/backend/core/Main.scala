package bestaro.backend.core

import java.io.File
import java.util.Date

import bestaro.backend.collectors.util.SlowHttpDownloader
import bestaro.backend.collectors.{FacebookCollector, OlxCollector}
import bestaro.backend.core.processors.{LocationStringProcessor, PlaintextProcessor}
import bestaro.backend.database.DatabaseWrapper
import bestaro.backend.database.DatabaseWrapper.CacheEfficiencyRecord
import bestaro.backend.helpers.TaggedRecordsManager
import bestaro.backend.helpers.TaggedRecordsManager.TaggedRecord
import bestaro.backend.{AppConfig, DataSupplier}
import bestaro.common.types.{Record, RecordId}
import bestaro.locator.LocatorDatabase

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

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

        println(plaintextProcessor.cacheEfficiencyMetrics)
        val cacheEfficiency = plaintextProcessor.cacheEfficiencyMetrics
        DatabaseWrapper.saveDataEfficiencyRecord(
          CacheEfficiencyRecord(cacheEfficiency.cacheHits, cacheEfficiency.allQueries, new Date().getTime)
        )

      case SEND =>
        import scala.concurrent.ExecutionContext.Implicits.global

        val dataSupplier = new DataSupplier
        // synchronous
        DatabaseWrapper.allNotSentProcessedRecords
          .filter(allPicturesAreValid)
          .map { record =>
            Await.result(dataSupplier.sendRecord(record)
              .flatMap(_ => DatabaseWrapper.markRecordAsSent(record))
              , Duration.Inf)
          }
    }
  }

  private def allPicturesAreValid(record: Record): Boolean = {
    record.pictures.nonEmpty && record.pictures.forall(picture =>
      new File("pictures/" + picture).length() > 0)
  }

  private def getLocationTaggedRecords: Map[RecordId, TaggedRecord] = {
    TaggedRecordsManager.allLocationRecordsFromCsv().map(tr => tr.recordId -> tr).toMap
  }

  private def getEventTaggedRecords: Map[RecordId, TaggedRecord] = {
    TaggedRecordsManager.allEventTypeRecordsFromCsv().map(tr => tr.recordId -> tr).toMap
  }
}

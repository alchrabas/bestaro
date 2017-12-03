package bestaro.backend.core

import java.util.Date

import bestaro.backend.collectors.util.SlowHttpDownloader
import bestaro.backend.collectors.{FacebookCollector, OlxCollector}
import bestaro.backend.core.processors.{LocationStringProcessor, PlaintextProcessor}
import bestaro.backend.database.DatabaseWrapper
import bestaro.backend.database.DatabaseWrapper.CacheEfficiencyRecord
import bestaro.backend.types.RawRecord
import bestaro.backend.{AppConfig, DataSupplier}
import bestaro.locator.LocatorDatabase

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ScheduledProcessor {

  private val FB_INTERVAL_SEC = 7 * 60
  private val OLX_INTERVAL_SEC = 16 * 60

  private var lastFbRun = 0L
  private var lastOlxRun = 0L

  def now: Long = {
    new Date().getTime / 1000
  }

  def main(args: Array[String]): Unit = {

    while (true) {
      if (now - lastFbRun > FB_INTERVAL_SEC) {
        println(s"Collecting from FB - ${new Date()}")
        lastFbRun = now
        collectAndProcessFbTask()
      }
      if (now - lastOlxRun > OLX_INTERVAL_SEC) {
        println(s"Collecting from OLX - ${new Date()}")
        lastOlxRun = now
        collectAndProcessOlxTask()
      }

      Thread.sleep(60 * 1000)
    }
  }

  def collectAndProcessFbTask(): Unit = {
    val fb = new FacebookCollector(DatabaseWrapper.saveRawRecord, DatabaseWrapper.rawRecordAlreadyExists)
    fb.collectFromNextGroup()

    processDataTask()
    sendDataTask()
  }

  def collectAndProcessOlxTask(): Unit = {
    val olx = new OlxCollector(DatabaseWrapper.saveRawRecord, new SlowHttpDownloader)
    olx.collectFromNextVoivodeship()

    processDataTask()
    sendDataTask()
  }

  def processDataTask(): Unit = {

    val locatorDatabase = new LocatorDatabase(AppConfig.getProperty("locatorDatabasePath"))
    val locationStringProcessor = new LocationStringProcessor(locatorDatabase)
    val plaintextProcessor = new PlaintextProcessor(locatorDatabase)

    import scala.concurrent.ExecutionContext.Implicits.global

    val allFutures = allRecordsToProcess.toIterator
      .map(locationStringProcessor.process)
      .map(plaintextProcessor.process)
      .map(_.buildRecord)
      .map(DatabaseWrapper.saveProcessedRecord)
      .toSeq

    Await.result(Future.sequence(allFutures), Duration.Inf)

    println("Finished processing", plaintextProcessor.cacheEfficiencyMetrics)
    val cacheEfficiency = plaintextProcessor.cacheEfficiencyMetrics
    DatabaseWrapper.saveDataEfficiencyRecord(
      CacheEfficiencyRecord(cacheEfficiency.cacheHits, cacheEfficiency.allQueries, new Date().getTime)
    )
  }

  private def allRecordsToProcess: Seq[RawRecord] = {
    val forceProcessing = AppConfig.getProperty("forceProcessing") == "true"

    if (forceProcessing) {
      DatabaseWrapper.allRawRecords
    } else {
      DatabaseWrapper.allUnprocessedRawRecords
    }
  }

  def sendDataTask(): Unit = {
    val dataSupplier = new DataSupplier
    DatabaseWrapper.allNotSentProcessedRecords
      .foreach { record =>
        dataSupplier.sendRecord(record)
        DatabaseWrapper.markRecordAsSent(record)
      }
  }
}

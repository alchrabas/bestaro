package bestaro.core

import bestaro.DataSupplier
import bestaro.collectors.util.SlowHttpDownloader
import bestaro.collectors.{FacebookCollector, OlxCollector}
import bestaro.common.types.RecordId
import bestaro.core.processors.PlaintextProcessor
import bestaro.helpers.TaggedRecordsManager
import bestaro.helpers.TaggedRecordsManager.TaggedRecord


object Main {

  val FB = "FB"
  val OLX = "OLX"
  val PROCESS = "PROCESS"

  val OPTION = PROCESS

  def main(args: Array[String]): Unit = {
    val printResult = (record: RawRecord) => println(record)
    val jsonSerializer = new JsonSerializer

    OPTION match {
      case FB =>
        val fb = new FacebookCollector(jsonSerializer.saveInJson, jsonSerializer.recordAlreadyExists)
        fb.collect(jsonSerializer.saveInJson)
      case OLX =>
        val olx = new OlxCollector(new SlowHttpDownloader)
        olx.collect(jsonSerializer.saveInJson)
      case PROCESS =>
        val records = jsonSerializer.readRecordsFromFile
        val taggedRecords = getTaggedRecords
        val processor = new PlaintextProcessor
//        val recordsWithTags = records.filter(r => taggedRecords.contains(r.recordId))
        val processedRecords = records.map(processor.process)

        val verifier = new LocationVerifier(taggedRecords)
        println(verifier.verify(processedRecords))

        val dataSupplier = new DataSupplier
        processedRecords
          .map(_.buildRecord)
          .foreach(dataSupplier.sendRecord)
    }
  }

  private def getTaggedRecords: Map[RecordId, TaggedRecord] = {
    TaggedRecordsManager.readTaggedRecordsFromCsv().map(tr => tr.recordId -> tr).toMap
  }
}

package bestaro.core

import bestaro.collectors.util.SlowHttpDownloader
import bestaro.collectors.{FacebookCollector, OlxCollector}
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
        val processor = new PlaintextProcessor
        val processedRecords = records.map(record => (record.recordId, processor.process(record)))

        val evaluator = new LocationEvaluator(getTaggedRecords)
        println(evaluator.evaluate(processedRecords))
    }
  }

  private def getTaggedRecords: Map[RecordId, TaggedRecord] = {
    TaggedRecordsManager.readTaggedRecordsFromCsv().map(tr => tr.recordId -> tr).toMap
  }
}

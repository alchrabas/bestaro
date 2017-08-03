package bestaro.core

import bestaro.collectors.util.SlowHttpDownloader
import bestaro.collectors.{FacebookCollector, OlxCollector}
import bestaro.core.processors.PlaintextProcessor
import bestaro.helpers.TaggedRecordsManager
import bestaro.helpers.TaggedRecordsManager.TaggedRecord
import bestaro.service.CachedNominatimClient
import fr.dudie.nominatim.client.JsonNominatimClient
import morfologik.stemming.polish.PolishStemmer
import org.apache.http.impl.client.DefaultHttpClient


object Main {

  val FB = "FB"
  val OLX = "OLX"
  val PROCESS = "PROCESS"

  val OPTION = PROCESS

  def main(args: Array[String]): Unit = {
    val printResult = (record: RawRecord) => println(record)
    val jsonSerializer = new JsonSerializer

//    val la = new CachedNominatimClient(new JsonNominatimClient(new DefaultHttpClient(), "ala123@exeris.org"))
//    val a = la.search("Długa, Kraków, województwo małopolskie")
//
//    return
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
        val recordsWithTags = records.filter(r => taggedRecords.contains(r.recordId))
        val processedRecords = recordsWithTags.map(processor.process)

        val evaluator = new LocationEvaluator(taggedRecords)
        println(evaluator.evaluate(processedRecords))
    }
  }

  private def getTaggedRecords: Map[RecordId, TaggedRecord] = {
    TaggedRecordsManager.readTaggedRecordsFromCsv().map(tr => tr.recordId -> tr).toMap
  }
}

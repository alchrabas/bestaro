package bestaro.core

import java.io.{File, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import bestaro.collectors.{FacebookCollector, OlxCollector}
import bestaro.collectors.util.SlowHttpDownloader
import bestaro.core.processors.PlaintextProcessor
import upickle.default._

object Main {

  val FB = "FB"
  val OLX = "OLX"
  val PROCESS = "PROCESS"

  val OPTION = FB

  def main(args: Array[String]): Unit = {
    val printResult = (record: RawRecord) => println(record)

    OPTION match {
      case FB =>
        val fb = new FacebookCollector(saveInJson)
        fb.collect(printResult)
      case OLX =>
        val olx = new OlxCollector(new SlowHttpDownloader)
        olx.collect(saveInJson)
      case PROCESS =>
        val records = readRecordsFromFile
        val processor = new PlaintextProcessor
        for (record <- records) {
          processor.process(record)
        }
    }
  }

  def saveInJson(record: RawRecord): Unit = {
    var listOfRecords: Seq[RawRecord] = readRecordsFromFile
    listOfRecords = listOfRecords :+ record
    saveFile(write(listOfRecords))
  }

  private def readRecordsFromFile = {
    val fileContents = readFile()
    read[Seq[RawRecord]](fileContents).filter {
      a => a.link != null && a.link.contains("facebook")
    }
  }

  private def readFile(): String = {
    if (!Paths.get("rawData.json").toFile.exists()) {
      new File("rawData.json").createNewFile()
    }
    val encoded = Files.readAllBytes(Paths.get("rawData.json"))
    if (encoded.isEmpty) {
      new String("[]")
    } else {
      new String(encoded, StandardCharsets.UTF_8)
    }
  }

  private def saveFile(json: String): Unit = {
    val jsonWriter = new FileWriter("rawData.json")
    jsonWriter.write(json)
    jsonWriter.close()
  }
}

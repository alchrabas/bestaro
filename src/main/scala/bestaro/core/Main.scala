package bestaro.core

import java.io.{File, FileWriter}
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Paths}

import bestaro.collectors.{FacebookCollector, OlxCollector}
import bestaro.collectors.util.SlowHttpDownloader
import bestaro.core.processors.PlaintextProcessor
import upickle.default._

object Main {

  def main(args: Array[String]): Unit = {

    //    val fb = new FacebookCollector(saveInJson)
    //    fb.collect(saveInJson)
    //    val olx = new OlxCollector(new SlowHttpDownloader)
    //    olx.collect(saveInJson)


    val records = readRecordsFromFile
    val processor = new PlaintextProcessor
    for (record <- records) {
      processor.process(record)
    }
  }

  def saveInJson(record: RawRecord): Unit = {
    var listOfRecords: Seq[RawRecord] = readRecordsFromFile
    listOfRecords = listOfRecords :+ record
    saveFile(write(listOfRecords))
  }

  private def readRecordsFromFile = {
    val fileContents = readFile()
    var listOfRecords = read[Seq[RawRecord]](fileContents)
    listOfRecords
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

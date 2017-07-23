package bestaro.core

import java.io.{File, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import bestaro.collectors.util.SlowHttpDownloader
import bestaro.collectors.{FacebookCollector, OlxCollector}
import bestaro.core.processors.PlaintextProcessor


object Main {

  val FB = "FB"
  val OLX = "OLX"
  val PROCESS = "PROCESS"

  val OPTION = FB

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
        for (record <- records) {
          processor.process(record)
        }
    }
  }
}

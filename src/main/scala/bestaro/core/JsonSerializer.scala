package bestaro.core

import java.io.{File, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import upickle.default.{read, write}

class JsonSerializer {

  def readRecordsFromFile: Seq[RawRecord] = {
    val fileContents = readFile()
    read[Seq[RawRecord]](fileContents)
      .filter {
        record => record.link != null && record.link.contains("facebook")
      }
  }

  import RecordId._

  def saveInJson(record: RawRecord): Unit = {
    var listOfRecords: Seq[RawRecord] = readRecordsFromFile
    if (!listOfRecords.exists(_.recordId == record.recordId)) {
      listOfRecords = listOfRecords :+ record
    }
    saveFile(write(listOfRecords))
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

  def recordAlreadyExists(record: RawRecord): Boolean = {
    readRecordsFromFile.exists(_.recordId == record.recordId)
  }
}

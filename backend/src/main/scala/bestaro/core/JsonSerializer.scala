package bestaro.core

import bestaro.util.FileIO
import upickle.default.{read, write}

class JsonSerializer {

  def readRecordsFromFile: Seq[RawRecord] = {
    val fileContents = FileIO.readFile("rawData.json", "[]")
    read[Seq[RawRecord]](fileContents)
      .filter {
        record => record.link != null && record.link.contains("facebook")
      }
  }

  def saveInJson(record: RawRecord): Unit = {
    var listOfRecords: Seq[RawRecord] = readRecordsFromFile
    if (!listOfRecords.exists(_.recordId == record.recordId)) {
      listOfRecords = listOfRecords :+ record
    }
    FileIO.saveFile("rawData.json", write(listOfRecords))
  }


  def recordAlreadyExists(record: RawRecord): Boolean = {
    readRecordsFromFile.exists(_.recordId == record.recordId)
  }
}

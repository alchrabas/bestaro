package bestaro.core

import bestaro.common.util.FileIO
import play.api.libs.json.Json

class JsonSerializer {

  def readRecordsFromFile: Seq[RawRecord] = {
    val fileContents = FileIO.readFile("rawData.json", "[]")
    Json.parse(fileContents).as[Seq[RawRecord]]
      .filter {
        record => record.link != null && record.link.contains("facebook")
      }
  }

  def saveInJson(record: RawRecord): Unit = {
    var listOfRecords: Seq[RawRecord] = readRecordsFromFile
    if (!listOfRecords.exists(_.recordId == record.recordId)) {
      listOfRecords = listOfRecords :+ record
    }
    FileIO.saveFile("rawData.json", Json.stringify(Json.toJson(listOfRecords)))
  }


  def recordAlreadyExists(record: RawRecord): Boolean = {
    readRecordsFromFile.exists(_.recordId == record.recordId)
  }
}

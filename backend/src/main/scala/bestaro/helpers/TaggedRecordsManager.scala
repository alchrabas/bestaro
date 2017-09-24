package bestaro.helpers

import java.io.File

import bestaro.common.types.RecordId
import bestaro.core._
import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer

object TaggedRecordsManager {

  def main(args: Array[String]): Unit = {
    readTaggedRecordsFromCsv()
  }

  def readTaggedRecordsFromCsv(): List[TaggedRecord] = {
    val reader = CSVReader.open("records-for-tagging.csv")
    val listOfRows = reader.allWithHeaders()

    val taggedRecords = ListBuffer[TaggedRecord]()

    for ((row, index) <- listOfRows.view.zipWithIndex if index < 480) {
      val recordId = Json.parse(row("ID")).as[RecordId]
      val locs =
        stringToList(row("Location-1")) ::: stringToList(row("Location-2")) :::
          stringToList(row("Location-3"))
      val altLocs = stringToList(row("Location-1-opt")) ::: stringToList(row("Location-2-opt"))
      val cities = stringToList(row("City-1")) ::: stringToList(row("City-2")) :::
        stringToList(row("City-3"))
      val isKrakow = Option(row("Krakow")).contains("1")
      val date = row("Date")
      val animalType = row("Type")
      val eventType = row("Status")

      if ((locs ++ altLocs).nonEmpty || cities.nonEmpty /* && isKrakow */ ) {
        taggedRecords.append(TaggedRecord(recordId, locs, altLocs, cities, animalType, eventType))
      }
    }

    taggedRecords.toList
  }

  case class TaggedRecord(recordId: RecordId, locs: List[String], altLocs: List[String],
                          cities: List[String], animalType: String, eventType: String)

  private def stringToList(str: String): List[String] = {
    Option(str).filter(_.nonEmpty).toList
  }

  private def saveInCsv(): Unit = {
    if (new File("records-for-tagging.csv").exists()) {
      throw new Exception("Do not overwrite this csv file")
    }
    val writer = CSVWriter.open("records-for-tagging.csv")
    val jsonSerializer = new JsonSerializer
    for (record <- jsonSerializer.readRecordsFromFile) {
      writer.writeRow(List(Json.stringify(Json.toJson(record.recordId)), record.message))
    }
    writer.close()
  }
}

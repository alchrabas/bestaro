package bestaro.helpers

import java.io.File

import bestaro.core.{JsonSerializer, RecordId}
import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import upickle.default.{read, write}

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
      val recordId = read[RecordId](row("ID"))
      val locs =
        stringToList(row("Location-1")) ::: stringToList(row("Location-2")) :::
          stringToList(row("Location-3"))
      val altLocs = stringToList(row("Location-1-opt")) ::: stringToList(row("Location-2-opt"))
      val cities = stringToList(row("City-1")) ::: stringToList(row("City-2")) :::
        stringToList(row("City-3"))
      val isKrakow = Option(row("Krakow")).contains("1")
      val date = row("Date")
      val animalType = row("Type")
      val status = row("Status")

      if ((locs ++ altLocs).nonEmpty || cities.nonEmpty /* && isKrakow */ ) {
        taggedRecords.append(TaggedRecord(recordId, locs, altLocs, cities, animalType, status))
      }
    }

    taggedRecords.toList
  }

  case class TaggedRecord(recordId: RecordId, locs: List[String], altLocs: List[String],
                          cities: List[String], animalType: String, status: String)

  private def stringToList(str: String): List[String] = {
    Option(str).filter(_.nonEmpty).toList
  }

  private def saveInCsv(): Unit = {
    import RecordId._
    if (new File("records-for-tagging.csv").exists()) {
      throw new Exception("Do not overwrite this csv file")
    }
    val writer = CSVWriter.open("records-for-tagging.csv")
    val jsonSerializer = new JsonSerializer
    for (record <- jsonSerializer.readRecordsFromFile) {
      writer.writeRow(List(write(record.recordId), record.message))
    }
    writer.close()
  }
}

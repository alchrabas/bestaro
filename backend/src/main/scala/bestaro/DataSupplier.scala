package bestaro

import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.nio.file.{Files, Paths}

import bestaro.common.{NamedPicture, Record, RecordDTO}
import play.api.libs.json.Json

class DataSupplier {
  def sendRecord(record: Record) {

    val namedPictures = record.pictures
      .map { pictureName =>
        (pictureName, Files.readAllBytes(Paths.get("pictures/" + pictureName)))
      }
      .map(pictureAndBytes => NamedPicture(pictureAndBytes._1, pictureAndBytes._2))

    val recordDTO = RecordDTO(record, namedPictures)
    val encodedJson = Json.toBytes(Json.toJson(recordDTO))
    println("SENDING: " + recordDTO.record)

    uploadBytes(encodedJson)
  }

  private def uploadBytes(encodedJson: Array[Byte]): Unit = {
    val yahoo = new URL("http://localhost:8888/upload/")
    val connection = yahoo.openConnection().asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.setRequestMethod("POST")

    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
    connection.connect()
    val outputStream = connection.getOutputStream
    outputStream.write(encodedJson)
    outputStream.close()

    val reader = new BufferedReader(new InputStreamReader(connection.getInputStream))

    println(reader.readLine())
  }
}

package bestaro

import java.io._
import java.net.{HttpURLConnection, URL}
import java.nio.file.{Files, Paths}
import java.util.{Base64, Properties}
import javax.imageio.ImageIO

import bestaro.common.types.{NamedPicture, Record, RecordDTO}
import bestaro.common.util.ImageResizer
import play.api.libs.json.Json

class DataSupplier {

  def sendRecord(record: Record) {
    val namedPictures = record.pictures
      .map { pictureName =>
        (pictureName, imageBytes(pictureName), minifiedImageBytes(pictureName))
      }
      .map(pictureAndBytes => NamedPicture(pictureAndBytes._1, pictureAndBytes._2, pictureAndBytes._3))

    val recordDTO = RecordDTO(record, namedPictures)
    val encodedJson = Json.toBytes(Json.toJson(recordDTO))
    println("SENDING: " + recordDTO.record)

    uploadBytes(encodedJson)
  }

  private def imageBytes(pictureName: String): Array[Byte] = {
    Files.readAllBytes(Paths.get("pictures/" + pictureName))
  }

  private def minifiedImageBytes(pictureName: String): Array[Byte] = {
    val bufferedImage = ImageIO.read(new File("pictures/" + pictureName))
    val resizedImage = ImageResizer.createResizedCopy(bufferedImage, 100, 100, preserveAlpha = true)
    val byteOutputStream = new ByteArrayOutputStream()
    ImageIO.write(resizedImage, "png", byteOutputStream)
    byteOutputStream.toByteArray
  }

  private def uploadBytes(encodedJson: Array[Byte]): Unit = {
    val connectionToFrontend = new URL(AppConfig.getProperty("frontendURL"))
    val connection = connectionToFrontend.openConnection().asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.setRequestMethod("POST")

    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
    val userCredentials = AppConfig.getProperty("frontendAuthCredentials").getBytes
    val basicAuth = "Basic " + new String(Base64.getEncoder.encode(userCredentials))
    connection.setRequestProperty("Authorization", basicAuth)
    connection.connect()
    val outputStream = connection.getOutputStream
    outputStream.write(encodedJson)
    outputStream.close()

    val reader = new BufferedReader(new InputStreamReader(connection.getInputStream))

    println(reader.readLine())
  }
}

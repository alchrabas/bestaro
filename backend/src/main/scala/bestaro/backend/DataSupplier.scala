package bestaro.backend


import java.io._
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO

import bestaro.backend.util.ImageUtil
import bestaro.common.types.{NamedPicture, Record, RecordDTO}
import dispatch.Defaults._
import dispatch._
import play.api.libs.json.Json

class DataSupplier {

  def sendRecord(record: Record): Future[String] = {
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

    val resizedImage = ImageUtil.shrinkToDimension(bufferedImage, 200)
    val byteOutputStream = new ByteArrayOutputStream()
    ImageIO.write(resizedImage, "png", byteOutputStream)
    byteOutputStream.toByteArray
  }

  private def uploadBytes(encodedJson: Array[Byte]): Future[String] = {
    val Array(username, password) = AppConfig.getProperty("frontendAuthCredentials").split(":")
    val r = url(AppConfig.getProperty("frontendURL"))
      .as_!(username, password)
      .POST
      .setContentType("application/json", StandardCharsets.UTF_8)
      .setBody(encodedJson)

    val future = Http.default(r OK as.String)
    future.onComplete(a => println(a.get))
    future
  }
}

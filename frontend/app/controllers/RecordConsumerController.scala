package controllers

import java.io.{File, FileOutputStream}
import javax.inject.Inject

import bestaro.common.types.{NamedPicture, Record, RecordDTO}
import bestaro.common.util.FileIO
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

class RecordConsumerController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def saveRecord() = Action { implicit request: Request[AnyContent] =>
    val recordDTO = request.body.asJson.get.as[RecordDTO]
    recordDTO.pictures.foreach(saveNamedPicture)

    val record = recordDTO.record
    // will safe it in db or something
    val jsonDataMap = Json.parse(FileIO.readFile("frontend/allData.json", "{}")).as[Map[String, Record]]
    val newJsonData = jsonDataMap.updated(record.recordId.toString, record)
    FileIO.saveFile("frontend/allData.json", Json.stringify(Json.toJson(newJsonData)))

    Ok("Thanks")
  }

  private def saveNamedPicture(picture: NamedPicture): Unit = {
    saveImage(picture.bytes, new File("frontend/public/pictures/" + picture.name))
    saveImage(picture.minifiedBytes, new File("frontend/public/pictures_min/" + picture.name))
  }

  private def saveImage(bytes: Array[Byte], picturePath: File): Unit = {
    val fileWriter = new FileOutputStream(picturePath)
    fileWriter.write(bytes)
    fileWriter.close()
  }
}


package controllers

import java.io.{File, FileOutputStream}
import java.util.Base64
import javax.inject.Inject

import bestaro.common.types.{NamedPicture, Record, RecordDTO}
import bestaro.common.util.FileIO
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


class AuthenticateAction @Inject()(parser: BodyParsers.Default,
                                   configuration: play.api.Configuration)
                                  (implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {

  private val authUsername = configuration.underlying.getString("bestaro.upload.auth.username")
  private val authPassword = configuration.underlying.getString("bestaro.upload.auth.password")

  private val UNAUTHORIZED =
    Results.Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=Unauthorized")

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    val authSuccessful = request.headers.get("Authorization") exists { authHeader =>
      val (user, pass) = decodeBasicAuth(authHeader)
      user == authUsername && pass == authPassword
    }

    if (authSuccessful) {
      block(request)
    } else {
      Future(UNAUTHORIZED)
    }
  }

  private def decodeBasicAuth(authHeader: String): (String, String) = {
    val baStr = authHeader.replaceFirst("Basic ", "")

    val decoded = Base64.getDecoder.decode(baStr)
    val Array(user, password) = new String(decoded).split(":")
    (user, password)
  }
}

class RecordConsumerController @Inject()(cc: ControllerComponents,
                                         configuration: play.api.Configuration,
                                         authenticationAction: AuthenticateAction,
                                        ) extends AbstractController(cc) {

  def saveRecord() = authenticationAction { implicit request: Request[AnyContent] =>
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
    val picturesDir = configuration.underlying.getString("bestaro.picturesDir")
    val minPicturesDir = configuration.underlying.getString("bestaro.minPicturesDir")

    saveImage(picture.bytes, new File(picturesDir + "/" + picture.name))
    saveImage(picture.minifiedBytes, new File(minPicturesDir + "/" + picture.name))
  }

  private def saveImage(bytes: Array[Byte], picturePath: File): Unit = {
    val fileWriter = new FileOutputStream(picturePath)
    fileWriter.write(bytes)
    fileWriter.close()
  }
}


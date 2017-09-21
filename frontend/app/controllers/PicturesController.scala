package controllers

import java.io.File
import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

@Singleton
class PicturesController @Inject()(cc: ControllerComponents,
                                   configuration: play.api.Configuration,
                                   implicit val ec: ExecutionContext) extends AbstractController(cc) {

  def minPicture(file: String) = Action { implicit request: Request[AnyContent] =>
    val picturesDir = configuration.underlying.getString("bestaro.frontend.minPicturesDir")
    Ok.sendFile(new File(picturesDir + "/" + file))
  }

  def picture(file: String) = Action { implicit request: Request[AnyContent] =>
    val minPicturesDir = configuration.underlying.getString("bestaro.frontend.picturesDir")
    Ok.sendFile(new File(minPicturesDir + "/" + file))
  }
}

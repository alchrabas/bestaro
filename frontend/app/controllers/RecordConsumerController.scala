package controllers

import java.util.Base64

import bestaro.common.types.Record
import data.DatabaseTypes
import javax.inject.Inject
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
                                         protected val database: DatabaseTypes
                                        )(
                                          implicit executionContext: ExecutionContext
                                        ) extends AbstractController(cc) {

  def saveRecord(): Action[AnyContent] = authenticationAction.async { implicit request: Request[AnyContent] =>
    val record = request.body.asJson.get.as[Record]

    database.saveRecord(record)
      .map(_ => Ok("Thanks"))
  }
}

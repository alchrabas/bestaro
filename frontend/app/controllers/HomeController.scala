package controllers

import javax.inject._

import jsmessages.JsMessagesFactory
import play.api.Configuration
import play.api.i18n._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(view: views.html.index,
                               cc: ControllerComponents,
                               messagesApi: MessagesApi,
                               jsMessagesFactory: JsMessagesFactory,
                               implicit val configuration: Configuration)
  extends AbstractController(cc)
    with I18nSupport {
  
  val messages = Action { implicit request =>
    val jsMessages = jsMessagesFactory.all
    Ok(jsMessages(Some("window.Messages")))
  }

  def index() = Action { implicit request =>
    Ok(view())
  }
}

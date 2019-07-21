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
class HomeController @Inject()(cc: ControllerComponents,
                               langs: Langs,
                               messagesApi: MessagesApi,
                               implicit val configuration: Configuration)
  extends AbstractController(cc) {

  private def messagesObject(language: String): Messages = {
    messagesApi.preferred(langs.availables.find(_.code == language).toSeq ++ langs.availables)
  }

  def messages(language: String) = Action { implicit request =>
    val jsMessagesFactory = new JsMessagesFactory(messagesApi)
    val jsMessages = jsMessagesFactory.all

    Ok(jsMessages(Some("window.Messages"))(messagesObject(language)))
  }
}

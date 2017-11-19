package controllers

import javax.inject._

import play.api.Configuration
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(view: views.html.index,
                               cc: ControllerComponents,
                               implicit val configuration: Configuration) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(view())
  }
}

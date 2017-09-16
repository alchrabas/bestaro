package bestaro

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{ContextHandler, HandlerList, ResourceHandler}

object App {
  def main(args: Array[String]): Unit = {
    import org.eclipse.jetty.servlet.ServletContextHandler
    val server: Server = new Server(8888)

    val resourceHandler = new ResourceHandler
    resourceHandler.setDirectoriesListed(true)
    resourceHandler.setWelcomeFiles(Array[String]("index.html"))
    resourceHandler.setResourceBase("src/main/resources")

    val contextHandler = new ContextHandler
    contextHandler.setContextPath("/")
//    context0.setBaseResource()
    contextHandler.setHandler(resourceHandler)

    val dynamicHandler: ServletContextHandler = new ServletContextHandler(server, "/rest")
    dynamicHandler.addServlet(classOf[ExampleServlet], "/")

    val ingestHandler: ServletContextHandler = new ServletContextHandler(server, "/ingest")
    ingestHandler.addServlet(classOf[ExampleServlet], "/")

    server.setHandler(new HandlerList(dynamicHandler, contextHandler))

    server.start()
  }
}

class ExampleServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    resp.setStatus(HttpStatus.OK_200)
    resp.getWriter.println("EmbeddedJetty")
  }
}
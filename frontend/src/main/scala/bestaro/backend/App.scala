package bestaro.backend

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.{NCSARequestLog, Server}
import org.eclipse.jetty.server.handler.{ContextHandler, HandlerList, ResourceHandler}

object App {
  def main(args: Array[String]): Unit = {
    import org.eclipse.jetty.servlet.ServletContextHandler
    val server: Server = new Server(8888)

    server.setRequestLog(createRequestLogger())

    val resourceHandler = new ResourceHandler
    resourceHandler.setDirectoriesListed(true)
    resourceHandler.setWelcomeFiles(Array[String]("index.html"))
    resourceHandler.setResourceBase("src/main/resources")

    val contextHandler = new ContextHandler
    contextHandler.setContextPath("/")
    //    context0.setBaseResource()
    contextHandler.setHandler(resourceHandler)
    contextHandler.setAllowNullPathInfo(true)

    val dynamicHandler: ServletContextHandler = new ServletContextHandler(server, "/rest")
    dynamicHandler.addServlet(classOf[ExampleServlet], "/")

    val dataConsumer: ServletContextHandler = new ServletContextHandler(server, "/upload")
    dataConsumer.addServlet(classOf[DataConsumer], "/")

    server.setHandler(new HandlerList(dynamicHandler, contextHandler, dataConsumer))

    server.start()
  }

  private def createRequestLogger(): NCSARequestLog = {
    import org.eclipse.jetty.server.NCSARequestLog
    val requestLog = new NCSARequestLog("jetty-yyyy_mm_dd.request.log")
    requestLog.setAppend(true)
    requestLog.setExtended(false)
    requestLog.setLogTimeZone("GMT")
    requestLog.setLogLatency(true)
    requestLog
  }
}

class ExampleServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    resp.setStatus(HttpStatus.OK_200)
    resp.getWriter.println("EmbeddedJetty")
  }
}
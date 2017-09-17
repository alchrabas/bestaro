package bestaro.frontend

import org.eclipse.jetty.server.handler.{ContextHandler, HandlerList, ResourceHandler}
import org.eclipse.jetty.server.{NCSARequestLog, Server}

object App {
  def main(args: Array[String]): Unit = {
    import org.eclipse.jetty.servlet.ServletContextHandler
    val server: Server = new Server(8888)
    server.setRequestLog(createRequestLogger())
    val resourceHandler = new ResourceHandler
    resourceHandler.setDirectoriesListed(true)
    resourceHandler.setWelcomeFiles(Array[String]("index.html"))
    resourceHandler.setResourceBase("src/main/resources")

    val pictureHandler = new ResourceHandler
    pictureHandler.setResourceBase("pictures_min")

    val contextHandler = new ContextHandler
    contextHandler.setContextPath("/pictures")
    contextHandler.setHandler(pictureHandler)
    contextHandler.setAllowNullPathInfo(true)

    val dynamicHandler: ServletContextHandler = new ServletContextHandler(server, "/rest")
    dynamicHandler.addServlet(classOf[PetRestServlet], "/")

    val dataConsumer: ServletContextHandler = new ServletContextHandler(server, "/upload")
    dataConsumer.addServlet(classOf[DataConsumer], "/")

    server.setHandler(new HandlerList(dynamicHandler, resourceHandler, contextHandler, dataConsumer))

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

package bestaro.frontend

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import bestaro.common.types.Record
import bestaro.common.util.FileIO
import org.eclipse.jetty.http.HttpStatus
import play.api.libs.json.Json

class PetRestServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val jsonData = Json.parse(FileIO.readFile("allData.json", "{}")).as[Map[String, Record]]

    val markers = jsonData
      .values
      .filter(_.pictures.nonEmpty)
      .filter(_.fullLocation.coordinate.isDefined)
      .map {
        v =>
          Map(
            "eventDate" -> String.valueOf(v.eventDate),
            "publishDate" -> String.valueOf(v.publishDate),
            "picture" -> v.pictures.head,
            "status" -> v.status.value,
            "lat" -> String.valueOf(v.fullLocation.coordinate.get.lat),
            "lon" -> String.valueOf(v.fullLocation.coordinate.get.lon)
          )
      }.toSeq

    val markersJson = Json.stringify(Json.toJson(markers))

    resp.setStatus(HttpStatus.OK_200)
    resp.getWriter.println(markersJson)
  }
}

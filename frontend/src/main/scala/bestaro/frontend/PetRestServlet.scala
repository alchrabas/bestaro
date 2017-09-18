package bestaro.frontend

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import bestaro.common.types.Record
import bestaro.common.util.FileIO
import org.eclipse.jetty.http.HttpStatus
import play.api.libs.json.Json

class PetRestServlet extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    try {
      val minLat = req.getParameter("minlat").toDouble
      val minLon = req.getParameter("minlon").toDouble
      val maxLon = req.getParameter("maxlon").toDouble
      val maxLat = req.getParameter("maxlat").toDouble

      resp.getWriter.println(getJsonWithMarkers(minLat, minLon, maxLon, maxLat))
      resp.setStatus(HttpStatus.OK_200)
    } catch {
      case _: Throwable => resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
    }
  }

  private def getJsonWithMarkers(minLat: Double, minLon: Double, maxLon: Double, maxLat: Double) = {
    val jsonData = Json.parse(FileIO.readFile("allData.json", "{}")).as[Map[String, Record]]
    val markers = getAllMarkers(jsonData)
      .filter(onlyInViewport(minLat, minLon, maxLat, maxLon))
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
      }

    println(s"Found ${markers.size} markers for $minLat $minLon $maxLat $maxLon")
    Json.stringify(Json.toJson(markers))
  }

  private def onlyInViewport(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Record => Boolean = {
    val latRange = maxLat - minLat
    val lonRange = maxLon - minLon
    record => {
      val coords = record.fullLocation.coordinate.get

      (coords.lat >= minLat - latRange) && (coords.lat <= maxLat + latRange) &&
        (coords.lon >= minLon - lonRange) && (coords.lon <= maxLon + lonRange)
    }
  }

  private def getAllMarkers(jsonData: Map[String, Record]): Seq[Record] = {
    jsonData
      .values
      .toSeq
      .filter(_.pictures.nonEmpty)
      .filter(_.fullLocation.coordinate.isDefined)
  }
}

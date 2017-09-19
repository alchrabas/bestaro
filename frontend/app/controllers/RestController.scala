package controllers

import java.io.File
import javax.inject.{Inject, Singleton}

import bestaro.common.types.Record
import bestaro.common.util.FileIO
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

@Singleton
class RestController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getMarkers(minlat: Double, minlon: Double, maxlat: Double, maxlon: Double) = Action {
    implicit request: Request[AnyContent] =>
      Ok(getJsonWithMarkers(minlat, minlon, maxlat, maxlon))
  }

  private def getJsonWithMarkers(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): JsValue = {
    val jsonData = Json.parse(FileIO.readFile("frontend/allData.json", "{}")).as[Map[String, Record]]
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
    Json.toJson(markers)
  }

  private def onlyInViewport(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Record => Boolean = {
    record => {
      val coords = record.fullLocation.coordinate.get

      val latRange = maxLat - minLat
      val lonRange = maxLon - minLon

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

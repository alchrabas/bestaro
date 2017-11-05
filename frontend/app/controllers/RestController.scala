package controllers

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}

import bestaro.common.types.Record
import bestaro.common.util.FileIO
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

case class FilterCriteria(dateFrom: String, dateTo: String, eventType: String)

@Singleton
class RestController @Inject()(cc: ControllerComponents
                              ) extends AbstractController(cc) {

  def getMarkers(minlat: Double, minlon: Double, maxlat: Double, maxlon: Double, dateFrom: String, dateTo: String, eventType: String) = Action {
    implicit request: Request[AnyContent] =>
      Ok(getJsonWithMarkers(minlat, minlon, maxlat, maxlon, FilterCriteria(dateFrom, dateTo, eventType)))
  }

  def strToDate(dateString: String): Long = {
    val f = new SimpleDateFormat("yyyy-M-dd")
    f.parse(dateString).getTime
  }

  private def applyFilters(filterCriteria: FilterCriteria): (Record) => Boolean = {
    record => {
      ((record.eventDate >= strToDate(filterCriteria.dateFrom) &&
        record.eventDate <= strToDate(filterCriteria.dateTo)) ||
        (record.postDate >= strToDate(filterCriteria.dateFrom) &&
          record.postDate <= strToDate(filterCriteria.dateTo))
        ) &&
        (filterCriteria.eventType == "ANY" || filterCriteria.eventType == record.eventType.value)
    }
  }

  private def getJsonWithMarkers(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double, filterCriteria: FilterCriteria): JsValue = {
    val jsonData = Json.parse(FileIO.readFile("frontend/allData.json", "{}")).as[Map[String, Record]]
    val markers = getAllMarkers(jsonData)
      .filter(onlyInViewport(minLat, minLon, maxLat, maxLon))
      .filter(applyFilters(filterCriteria))
      .map {
        v =>
          Map(
            "id" -> v.recordId.toString,
            "eventDate" -> String.valueOf(v.eventDate),
            "publishDate" -> String.valueOf(v.postDate),
            "picture" -> v.pictures.head,
            "eventType" -> v.eventType.value,
            "lat" -> String.valueOf(v.fullLocation.coordinate.get.lat),
            "lon" -> String.valueOf(v.fullLocation.coordinate.get.lon),
            "link" -> v.link
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

package controllers

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}

import bestaro.common.types.Record
import data.DatabaseTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class FilterCriteria(dateFrom: String, dateTo: String, eventType: String)

@Singleton
class RestController @Inject()(cc: ControllerComponents,
                               protected val database: DatabaseTypes
                              )(
                                implicit executionContext: ExecutionContext
                              ) extends AbstractController(cc) {

  private val PICTURES_IN_VIEWPORT_LIMIT = 500

  def getMarkers(minlat: Double, minlon: Double, maxlat: Double, maxlon: Double,
                 dateFrom: String, dateTo: String, eventType: String) = Action.async {
    implicit request: Request[AnyContent] =>
      getJsonWithMarkers(minlat, minlon, maxlat, maxlon,
        FilterCriteria(dateFrom, dateTo, eventType))
        .map(Ok(_))
  }

  private val simpleDateFormat = new SimpleDateFormat("yyyy-M-dd")

  def strToDate(dateString: String): Long = {
    simpleDateFormat.parse(dateString).getTime
  }

  private def applyFilters(filterCriteria: FilterCriteria): (Record) => Boolean = {
    record => {
      ((record.eventDate >= strToDate(filterCriteria.dateFrom) &&
        record.eventDate <= strToDate(filterCriteria.dateTo)) ||
        (record.postDate >= strToDate(filterCriteria.dateFrom) &&
          record.postDate <= strToDate(filterCriteria.dateTo))
        ) &&
        (filterCriteria.eventType == "ANY" || filterCriteria.eventType == record.eventType.entryName)
    }
  }

  private def getJsonWithMarkers(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double,
                                 filterCriteria: FilterCriteria): Future[JsValue] = {
    database
      .allRecordsInRange(minLat, minLon, maxLat, maxLon)
      .map(_.filter(_.pictures.nonEmpty))
      .map(_.filter(applyFilters(filterCriteria)))
      .map(_.slice(0, PICTURES_IN_VIEWPORT_LIMIT))
      .map(_.map(recordToMarker))
      .map { markers =>
        println(s"Found ${markers.size} markers for $minLat $minLon $maxLat $maxLon")
        Json.toJson(markers)
      }
  }

  private def recordToMarker(r: Record) = {
    Map(
      "id" -> r.recordId.toString,
      "eventDate" -> String.valueOf(r.eventDate),
      "publishDate" -> String.valueOf(r.postDate),
      "picture" -> r.pictures.head,
      "eventType" -> r.eventType.entryName,
      "lat" -> String.valueOf(r.fullLocation.coordinate.get.lat),
      "lon" -> String.valueOf(r.fullLocation.coordinate.get.lon),
      "link" -> r.link
    )
  }
}

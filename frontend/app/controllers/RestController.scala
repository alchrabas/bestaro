package controllers

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}

import bestaro.common.types.{EventType, Record}
import data.DatabaseTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class FilterCriteria(dateFrom: Long, dateTo: Long, eventType: Set[EventType])

@Singleton
class RestController @Inject()(cc: ControllerComponents,
                               protected val database: DatabaseTypes
                              )(
                                implicit executionContext: ExecutionContext
                              ) extends AbstractController(cc) {

  private val RECORDS_IN_VIEWPORT_LIMIT = 1000

  def getMarkers(centerlat: Double, centerlon: Double,
                 dateFrom: String, dateTo: String, eventType: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      getJsonWithMarkers(centerlat, centerlon,
        FilterCriteria(strToDate(dateFrom), strToDate(dateTo), strToEventType(eventType))
      ).map(Ok(_))
  }

  private def strToEventType(typeStr: String): Set[EventType] = {
    typeStr match {
      case "ANY" => Set(EventType.LOST, EventType.FOUND)
      case "FOUND" => Set(EventType.FOUND)
      case "LOST" => Set(EventType.LOST)
      case "NONE" => Set()
      case _ => throw new IllegalArgumentException(s"invalid value of eventType string: $typeStr")
    }
  }

  private val simpleDateFormat = new SimpleDateFormat("yyyy-M-dd")

  def strToDate(dateString: String): Long = {
    simpleDateFormat.parse(dateString).getTime
  }

  private def getJsonWithMarkers(centerLat: Double, centerLon: Double,
                                 filterCriteria: FilterCriteria): Future[JsValue] = {
    database
      .allRecordsInRange(centerLat, centerLon, filterCriteria,
        RECORDS_IN_VIEWPORT_LIMIT)
      .map(_.map(recordToMarker))
      .map { markers =>
        println(s"Found ${markers.size} markers for $centerLat $centerLon")
        Json.toJson(markers)
      }
  }

  private def recordToMarker(recordWithDistance: (Record, Option[Float])) = {
    val r = recordWithDistance._1
    Map(
      "id" -> r.recordId.toString,
      "eventDate" -> String.valueOf(r.eventDate),
      "publishDate" -> String.valueOf(r.postDate),
      "picture" -> r.pictures.head,
      "eventType" -> r.eventType.entryName,
      "lat" -> String.valueOf(r.fullLocation.coordinate.get.lat),
      "lng" -> String.valueOf(r.fullLocation.coordinate.get.lon),
      "link" -> r.link,
      "distance" -> String.valueOf(recordWithDistance._2.getOrElse(0f)),
    )
  }
}

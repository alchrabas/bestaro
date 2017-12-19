package data

import javax.inject.Inject

import bestaro.common.types.{AnimalType, EventType, Record, RecordId}
import bestaro.locator.types.FullLocation
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import controllers.FilterCriteria
import data.ExtendedPostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.{JsValue, Json}
import slick.ast.BaseTypedType
import slick.jdbc.meta.MTable
import slick.jdbc.{JdbcProfile, JdbcType}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class DatabaseTypes @Inject()(
                               protected val dbConfigProvider: DatabaseConfigProvider
                             )(
                               implicit executionContext: ExecutionContext
                             ) extends HasDatabaseConfigProvider[JdbcProfile] {

  private val geometryFactory = new GeometryFactory()

  def saveRecord(record: Record): Future[Int] = {
    db.run(records.insertOrUpdate(record))
  }

  def allRecordsInRange(centerLat: Double, centerLon: Double,
                        filterCriteria: FilterCriteria,
                        limit: Int): Future[Seq[(Record, Option[Float])]] = {

    val centerPoint = geometryFactory.createPoint(
      new Coordinate(centerLat, centerLon))

    db.run(records
      .filter(_.pictures.arrayLength > 0)
      .filter(r =>
        (r.eventDate >= filterCriteria.dateFrom && r.eventDate <= filterCriteria.dateTo) ||
          (r.postDate >= filterCriteria.dateFrom && r.postDate <= filterCriteria.dateTo))
      .filter(_.eventType === filterCriteria.eventType || filterCriteria.eventType.isEmpty)
      .filter(_.coordinates.nonEmpty)
      .map(r => (r, r.coordinates.distanceSphere(centerPoint)))
      .sortBy(_._2)
      .take(limit)
      .result)
  }

  private implicit val recordIdColumn: JdbcType[RecordId] with BaseTypedType[RecordId] = MappedColumnType.base[RecordId, String](
    _.toString,
    RecordId.fromString
  )

  private implicit val eventTypeColumn: JdbcType[EventType] with BaseTypedType[EventType] = MappedColumnType.base[EventType, String](
    _.entryName,
    EventType.withName
  )

  private implicit val animalTypeColumn: JdbcType[AnimalType] with BaseTypedType[AnimalType] = MappedColumnType.base[AnimalType, String](
    _.entryName,
    AnimalType.withName
  )

  private implicit val fullLocationColumn: JdbcType[FullLocation] with BaseTypedType[FullLocation] = MappedColumnType.base[FullLocation, String](
    fullLocation => Json.stringify(Json.toJson(fullLocation)),
    Json.parse(_).as[FullLocation]
  )

  private type recordColumns = (RecordId, EventType, AnimalType,
    JsValue, String, Long, Long, FullLocation, Option[Point])

  class Records(tag: Tag) extends Table[Record](tag, "records") {

    def recordId = column[RecordId]("record_id", O.PrimaryKey)

    def eventType = column[EventType]("event_type")

    def animalType = column[AnimalType]("animal_type")

    def pictures = column[JsValue]("pictures")

    def link = column[String]("link")

    def eventDate = column[Long]("event_date")

    def postDate = column[Long]("post_date")

    def fullLocation = column[FullLocation]("full_location")

    def coordinates = column[Option[Point]]("coordinates")

    def postDateIndex = index("post_date_idx", Tuple1(postDate))

    def eventDateIndex = index("event_date_idx", Tuple1(eventDate))

    def * = (
      recordId, eventType, animalType, pictures, link,
      eventDate, postDate, fullLocation, coordinates
    ) <> (toModel, fromModel)

    private def toModel(a: recordColumns): Record = {
      Record(a._1, a._2, a._3, a._4.as[List[String]], a._5, a._6, a._7, a._8)
    }

    private def fromModel(r: Record): Option[recordColumns] = {
      val coords = r.fullLocation.coordinate
        .map(a => geometryFactory.createPoint(new Coordinate(a.lat, a.lon)))
      Some((r.recordId, r.eventType, r.animalType, Json.toJson(r.pictures),
        r.link, r.eventDate, r.postDate, r.fullLocation, coords))
    }
  }

  val records = TableQuery[Records]

  createSchemaIfNotExists()

  /**
    * Function used to perform additional table initialization.
    * It produces queries that need to be executed for a specific table when it's created.
    */
  private def initializeTable(table: TableQuery[_]): DBIO[Int] = table.baseTableRow match {
    /* UNCOMMENT ON PG 9.5+
    case _: Records =>
      sqlu"CREATE INDEX record_coordinates ON records USING GIST (coordinates);"*/
    case _ => DBIO.successful(0)
  }

  private def createSchemaIfNotExists(): Unit = {
    val toCreate = List(records)
    val existingTables = db.run(MTable.getTables)
    val createSchemaAction = existingTables.flatMap(v => {
      val names = v.map(mTable => mTable.name.name)
      val createIfNotExist = toCreate.filter(table =>
        !names.contains(table.baseTableRow.tableName))
        .flatMap(table => List(
          table.schema.create,
          initializeTable(table)
        ))
      db.run(DBIO.sequence(createIfNotExist))
    })
    Await.result(createSchemaAction, Duration.Inf)
  }

}

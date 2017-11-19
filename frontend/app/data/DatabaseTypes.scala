package data

import javax.inject.Inject

import bestaro.common.types.{AnimalType, EventType, Record, RecordId}
import bestaro.locator.types.FullLocation
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import data.ExtendedPostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
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

  def allRecordsInRange(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Future[Seq[Record]] = {
    val poly = geometryFactory.createPolygon(
      Array(
        new Coordinate(minLat, minLon),
        new Coordinate(maxLat, minLon),
        new Coordinate(maxLat, maxLon),
        new Coordinate(minLat, maxLon),
        new Coordinate(minLat, minLon)
      ))
    db.run(records
      .filter(_.coordinates.within(poly))
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

  private implicit val picturesList: JdbcType[List[String]] with BaseTypedType[List[String]] = MappedColumnType.base[List[String], String](
    pictures => Json.stringify(Json.toJson(pictures)),
    Json.parse(_).as[List[String]]
  )

  private implicit val fullLocationColumn: JdbcType[FullLocation] with BaseTypedType[FullLocation] = MappedColumnType.base[FullLocation, String](
    fullLocation => Json.stringify(Json.toJson(fullLocation)),
    Json.parse(_).as[FullLocation]
  )

  private type recordColumns = (RecordId, EventType, AnimalType,
    List[String], String, Long, Long, FullLocation, Option[Point])

  class Records(tag: Tag) extends Table[Record](tag, "records") {

    def recordId = column[RecordId]("record_id", O.PrimaryKey)

    def eventType = column[EventType]("event_type")

    def animalType = column[AnimalType]("animal_type")

    def pictures = column[List[String]]("pictures")

    def link = column[String]("link")

    def postDate = column[Long]("post_date")

    def eventDate = column[Long]("event_date")

    def fullLocation = column[FullLocation]("full_location")

    def coordinates = column[Option[Point]]("coordinates")

    def * = (
      recordId, eventType, animalType, pictures, link,
      postDate, eventDate, fullLocation, coordinates
    ) <> (toModel, fromModel)

    private def toModel(a: recordColumns): Record = {
      Record(a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8)
    }

    private def fromModel(r: Record): Option[recordColumns] = {
      val coords = r.fullLocation.coordinate
        .map(a => geometryFactory.createPoint(new Coordinate(a.lat, a.lon)))
      Some((r.recordId, r.eventType, r.animalType, r.pictures,
        r.link, r.postDate, r.eventDate, r.fullLocation, coords))
    }
  }

  val records = TableQuery[Records]

  createSchemaIfNotExists()

  /**
    * Function used to perform additional table initialization.
    * It produces queries that need to be executed for a specific table when it's created.
    */
  private def initializeTable(table: TableQuery[_]): DBIO[Int] = table.baseTableRow match {
    case _: Records =>
      sqlu"CREATE INDEX record_coordinates ON records USING GIST (coordinates);"
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

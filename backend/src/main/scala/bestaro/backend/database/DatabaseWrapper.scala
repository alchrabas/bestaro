package bestaro.backend.database

import java.util.Date

import bestaro.backend.AppConfig
import bestaro.backend.types.RawRecord
import bestaro.common.types._
import bestaro.locator.types.FullLocation
import org.sqlite.SQLiteConfig
import play.api.libs.json.Json
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.SQLiteProfile.api._
import slick.jdbc.SQLiteProfile.backend.DatabaseDef
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object DatabaseWrapper {

  lazy val db: DatabaseDef = {
    val sqliteConfig = new SQLiteConfig
    sqliteConfig.setJournalMode(SQLiteConfig.JournalMode.WAL)
    sqliteConfig.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL)

    val dbURL = "jdbc:sqlite:" + AppConfig.getProperty("databasePath")

    val dbHandle = Database.forURL(dbURL, driver = "org.sqlite.JDBC",
      executor = AsyncExecutor("test1", minThreads = 1, queueSize = 1000,
        maxThreads = 1, maxConnections = 1),
      prop = sqliteConfig.toProperties
    )
    createSchemaIfNotExists(dbHandle)
    dbHandle
  }

  private def createSchemaIfNotExists(db: DatabaseDef): Unit = {
    val toCreate = List(rawRecords, processedRecords, recordsMetadata, cacheEfficiencyRecords)
    val existingTables = db.run(MTable.getTables)
    val createSchemaAction = existingTables.flatMap(v => {
      val names = v.map(mTable => mTable.name.name)
      val createIfNotExist = toCreate.filter(table =>
        !names.contains(table.baseTableRow.tableName)).map(_.schema.create)
      db.run(DBIO.sequence(createIfNotExist))
    })
    Await.result(createSchemaAction, Duration.Inf)
  }

  def saveProcessedRecord(recordToSave: Record): Future[Unit] = {
    db.run(
      processedRecords
        .filter(_.recordId === recordToSave.recordId)
        .join(recordsMetadata).on(_.recordId === _.recordId)
        .result.headOption
    ).map { recordAndMetadata =>
      if (recordAndMetadata.isEmpty || recordAndMetadata.get._1 != recordToSave || neverProcessed(recordAndMetadata)) {
        db.run(
          DBIO.seq(
            recordsMetadata
              .filter(_.recordId === recordToSave.recordId)
              .map(_.processedTimestamp)
              .update(new Date().getTime),
            processedRecords.insertOrUpdate(recordToSave)
          ).transactionally
        )
      } else {
        Future.successful(Unit)
      }
    }
  }

  private def neverProcessed(recordMetadata: Option[(Record, RecordMetadata)]): Boolean = {
    recordMetadata.get._2.processedTimestamp == 0
  }

  def saveRawRecord(rawRecord: RawRecord): Future[Seq[Int]] = {
    saveRawRecords(Seq(rawRecord))
  }

  def saveRawRecords(rawRecordsSeq: Seq[RawRecord]): Future[Seq[Int]] = {
    db.run(
      DBIO.sequence(
        rawRecordsSeq.flatMap(rawRecord =>
          Seq(
            rawRecords.insertOrUpdate(rawRecord),
            recordsMetadata.insertOrUpdate(
              RecordMetadata(rawRecord.recordId, new Date().getTime, 0, 0))
          )
        )
      ).transactionally
    )
  }

  def rawRecordAlreadyExists(rawRecord: RawRecord): Boolean = {
    Await.result(db.run( // todo make async
      recordsMetadata.filter(_.recordId === rawRecord.recordId)
        .exists.result
    ), Duration.Inf)
  }

  def allRawRecords: Seq[RawRecord] = {
    Await.result(db.run( // todo make async
      rawRecords.result
    ), Duration.Inf)
  }

  def allUnprocessedRawRecords: Seq[RawRecord] = {
    Await.result(db.run( // todo make async
      rawRecords.filter(
        _.recordId in {
          recordsMetadata
            .filter(row => row.processedTimestamp <= row.collectedTimestamp)
            .map(_.recordId)
        })
        .result
    ), Duration.Inf)
  }

  def allProcessedRecords: Seq[Record] = {
    Await.result(db.run( // todo make async
      processedRecords.result
    ), Duration.Inf)
  }

  def allNotSentProcessedRecords: Seq[Record] = {
    Await.result(db.run( // todo make async
      processedRecords.filter(
        _.recordId in {
          recordsMetadata
            .filter(row => row.sentTimestamp <= row.processedTimestamp)
            .map(_.recordId)
        })
        .result
    ), Duration.Inf)
  }

  def markRecordAsSent(record: Record): Unit = {
    db.run(
      recordsMetadata
        .filter(_.recordId === record.recordId)
        .map(_.sentTimestamp)
        .update(new Date().getTime)
    )
  }

  def processedLaterThanCollected(recordId: RecordId): Boolean = {
    Await.result(db.run(
      recordsMetadata.filter(_.recordId === recordId)
        .filter(row => row.processedTimestamp >= row.collectedTimestamp)
        .exists.result
    ), Duration.Inf)
  }

  // RawRecords

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

  class RawRecords(tag: Tag) extends Table[RawRecord](tag, "raw_records") {

    def recordId = column[RecordId]("record_id", O.PrimaryKey)

    def eventType = column[EventType]("event_type")

    def animalType = column[AnimalType]("animal_type")

    def message = column[String]("message")

    def postDate = column[Long]("post_date")

    def dataSource = column[String]("data_source")

    def pictures = column[List[String]]("pictures")

    def link = column[String]("link")

    def location = column[String]("location")

    def eventDate = column[Long]("event_date")

    def title = column[String]("title")

    def fullLocation = column[FullLocation]("full_location")

    def secondaryMessage = column[String]("secondary_message")

    def * = (recordId, eventType, animalType, message, postDate,
      dataSource, pictures, link, location, eventDate,
      title, fullLocation, secondaryMessage) <> ((RawRecord.apply _).tupled, RawRecord.unapply)
  }

  val rawRecords = TableQuery[RawRecords]

  // processed records

  case class RecordMetadata(recordId: RecordId,
                            collectedTimestamp: Long,
                            processedTimestamp: Long,
                            sentTimestamp: Long)

  class RecordsMetadata(tag: Tag) extends Table[RecordMetadata](tag, "records_metadata") {

    def recordId = column[RecordId]("record_id", O.PrimaryKey)

    def record = foreignKey("record_id_fk", recordId, rawRecords)(
      _.recordId,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def collectedTimestamp = column[Long]("collected_timestamp")

    def processedTimestamp = column[Long]("processed_timestamp")

    def sentTimestamp = column[Long]("sent_timestamp")

    def * = (recordId, collectedTimestamp, processedTimestamp, sentTimestamp) <> (RecordMetadata.tupled, RecordMetadata.unapply)
  }

  val recordsMetadata = TableQuery[RecordsMetadata]

  case class CacheEfficiencyRecord(cacheHits: Long, allQueries: Long, dateTime: Long)

  class CacheEfficiencyRecords(tag: Tag) extends Table[CacheEfficiencyRecord](tag, "cache_efficiency_records") {

    def cacheHits = column[Long]("cache_hits")

    def allQueries = column[Long]("all_queries")

    def dateTime = column[Long]("date_time", O.PrimaryKey)

    def * = (cacheHits, allQueries, dateTime) <> (CacheEfficiencyRecord.tupled, CacheEfficiencyRecord.unapply)
  }

  val cacheEfficiencyRecords = TableQuery[CacheEfficiencyRecords]

  def saveDataEfficiencyRecord(cacheEfficiencyRecord: CacheEfficiencyRecord): Unit = {
    Await.result(db.run(cacheEfficiencyRecords += cacheEfficiencyRecord), Duration.Inf)
  }

  class ProcessedRecords(tag: Tag) extends Table[Record](tag, "processed_records") {

    def recordId = column[RecordId]("record_id", O.PrimaryKey)

    def eventType = column[EventType]("event_type")

    def animalType = column[AnimalType]("animal_type")

    def pictures = column[List[String]]("pictures")

    def link = column[String]("link")

    def postDate = column[Long]("post_date")

    def eventDate = column[Long]("event_date")

    def fullLocation = column[FullLocation]("full_location")

    def record = foreignKey("record_id_fk", recordId, rawRecords)(
      _.recordId,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def * = (
      recordId, eventType, animalType, pictures, link, postDate, eventDate, fullLocation
    ) <> ((Record.apply _).tupled, Record.unapply)
  }

  val processedRecords = TableQuery[ProcessedRecords]
}

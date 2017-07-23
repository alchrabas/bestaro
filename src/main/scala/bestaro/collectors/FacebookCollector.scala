package bestaro.collectors

import java.nio.file.Path
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import bestaro.core.ProgressStatus.LOST
import bestaro.core.{FbId, RawRecord}
import bestaro.util.ImageUtil
import facebook4j._

import scala.collection.JavaConverters._
import scala.collection.mutable

class FacebookCollector(recordConsumer: RawRecord => Unit, isAlreadyStored: RawRecord => Boolean) {

  private val THREE_MONTHS = TimeUnit.DAYS.toSeconds(3 * 30)

  def collect(recordConsumer: RawRecord => Unit): Unit = {
    val facebook = new FacebookFactory().getInstance

    val foundGroups = facebook.search().searchGroups("ZaginioneKrakow")
    val foundGroup = foundGroups.get(0)
    val eater = new FacebookEater(facebook, foundGroup.getId)

    var continueFetching = true
    while (continueFetching) {
      val result = eater.fetch()
      println(s"Found ${result.size()} fb posts")

      val records = result
        .asScala
        .map(postToRecord)

      continueFetching = thereIsAtLeastOneRecordToSave(records)
      records.foreach(recordConsumer)
    }
  }

  private def thereIsAtLeastOneRecordToSave(records: mutable.Buffer[RawRecord]) = {
    records.map {
      record =>
        isWithinTimeBox(record.postDate) && !isAlreadyStored(record)
    }.exists(validToSave => validToSave)
  }

  private def isWithinTimeBox(postDate: Long): Boolean = {
    val threeMonthsAgo = Instant.now().minusSeconds(THREE_MONTHS).toEpochMilli
    postDate >= threeMonthsAgo
  }

  class FacebookEater(facebook: Facebook, groupId: String) {
    private var lastPage: Option[Paging[Post]] = None
    private val READING = new Reading().limit(10)
      .fields("message", "link", "id", "permalink_url", "created_time", "attachments")

    def fetch(): ResponseList[Post] = {
      Thread.sleep(1000)
      val responseList = if (lastPage.isEmpty) {
        facebook.groups().getGroupFeed(groupId, READING)
      } else {
        facebook.fetchNext(lastPage.get)
      }
      lastPage = Some(responseList.getPaging)

      responseList
    }
  }

  def postToRecord(post: Post): RawRecord = {
    println(s"[link: ${post.getPermalinkUrl}, ${post.getCreatedTime}] ${post.getMessage}")

    val id = FbId(post.getId)
    var picturePath: Option[Path] = Option.empty
    if (post.getPicture != null) {
      picturePath = Some(ImageUtil.saveImage(id, post.getPicture.openStream()))
    }

    RawRecord(id, LOST, post.getMessage, post.getCreatedTime.getTime,
      picturePath.map(_.toString).toList, Option(post.getPermalinkUrl).map(_.toString).orNull)
  }
}

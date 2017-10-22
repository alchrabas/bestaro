package bestaro.collectors

import java.nio.file.{Path, Paths}
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

import bestaro.common.types._
import bestaro.core.RawRecord
import bestaro.util.ImageUtil
import facebook4j._

import scala.collection.JavaConverters._
import scala.collection.mutable

class FacebookCollector(recordConsumer: RawRecord => Unit, isAlreadyStored: RawRecord => Boolean) {

  private val OLDEST_DATE_TO_COLLECT = TimeUnit.DAYS.toSeconds(3 * 30)
  private val POSTS_FETCHED_PER_PAGE = 20

  def collect(recordConsumer: RawRecord => Unit): Unit = {
    val facebook = new FacebookFactory().getInstance
    var allFoundPosts = 0

    val foundGroups = facebook.search().searchGroups("ZaginioneKrakow")
    val foundGroup = foundGroups.get(0)
    val eater = new FacebookEater(facebook, foundGroup.getId)

    var continueFetching = true
    while (continueFetching) {
      val result = eater.fetch()
      println(s"Found ${result.size()} fb posts")
      allFoundPosts += result.size()

      val records = result
        .asScala
        .map(postToRecord)

      continueFetching = thereIsAtLeastOneRecordToSave(records)
      records.foreach(recordConsumer)
    }
    println(s"Collected $allFoundPosts from FB")
  }

  private def thereIsAtLeastOneRecordToSave(records: mutable.Buffer[RawRecord]) = {
    records.map {
      record =>
        isWithinTimeBox(record.postDate) && !isAlreadyStored(record)
    }.exists(validToSave => validToSave)
  }

  private def isWithinTimeBox(postDate: Long): Boolean = {
    val threeMonthsAgo = Instant.now().minusSeconds(OLDEST_DATE_TO_COLLECT).toEpochMilli
    postDate >= threeMonthsAgo
  }

  class FacebookEater(facebook: Facebook, groupId: String) {
    private var lastPage: Option[Paging[Post]] = None
    private val READING = new Reading().until(new Date()).limit(POSTS_FETCHED_PER_PAGE)
      .fields("message", "link", "id", "permalink_url", "created_time",
        "attachments", "full_picture", "description")

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
    if (post.getFullPicture != null) {
      try {
        val potentialPicturePath = ImageUtil.pathToPicture(ImageUtil.pictureName(id, 1))
        if (potentialPicturePath.toFile.exists()) { // avoid fetching image when it's already there
          picturePath = Some(Paths.get(ImageUtil.pictureName(id, 1)))
        } else {
          picturePath = Some(ImageUtil.saveImage(id, 1, post.getFullPicture.openStream()))
        }
      } catch {
        case _: Exception => println("Unable to save the picture for ID " + id)
      }
    }

    val message = Option(post.getMessage)
    val sharedPostMessage = Option(post.getDescription)

    RawRecord(id, EventType.UNKNOWN, AnimalType.UNKNOWN, message.getOrElse(""), post.getCreatedTime.getTime,
      picturePath.map(_.toString).toList,
      Option(post.getPermalinkUrl).map(_.toString).orNull, secondaryMessage = sharedPostMessage.getOrElse(""),
      fullLocation = FullLocation(None, None, Some(Voivodeship.MALOPOLSKIE), None))
  }
}

package bestaro.collectors

import java.nio.file.Path
import java.util.UUID

import bestaro.core.ProgressStatus.LOST
import bestaro.core.RawRecord
import bestaro.util.ImageUtil
import facebook4j._
import scala.collection.JavaConverters._

class FacebookCollector(recordConsumer: RawRecord => Unit) {

  def collect(recordConsumer: RawRecord => Unit): Unit = {
    val facebook = new FacebookFactory().getInstance

    val foundGroups = facebook.search().searchGroups("ZaginioneKrakow")
    val foundGroup = foundGroups.get(0)
    val eater = new FacebookEater(facebook, foundGroup.getId)

    val result = eater.fetch()
    println(s"Found ${result.size()} posts")

    result
      .iterator
      .asScala
      .map(postToRecord)
      .foreach(recordConsumer)
  }

  class FacebookEater(facebook: Facebook, groupId: String) {
    private var lastPage: Option[Paging[Post]] = None
    private val READING = new Reading().limit(25)
      .fields("message", "link", "id", "permalink_url", "created_time", "attachments")

    def fetch(): ResponseList[Post] = {
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
    val uuid = UUID.randomUUID()
    var picturePath: Option[Path] = Option.empty
    if (post.getPicture != null) {
      picturePath = Some(ImageUtil.saveImage(uuid, post.getPicture.openStream()))
    }

    RawRecord(uuid.toString, LOST, post.getMessage, post.getCreatedTime.getTime,
      picturePath.map(_.toString).toList, Option(post.getPermalinkUrl).map(_.toString).orNull)
  }
}

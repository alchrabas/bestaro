package bestaro.collectors

import java.nio.file.Path
import java.util.UUID

import bestaro.core.ProgressStatus.LOST
import bestaro.core.RawRecord
import bestaro.util.ImageUtil
import facebook4j.{FacebookFactory, Post}

class FacebookCollector(recordConsumer: RawRecord => Unit) {

  def collect(recordConsumer: RawRecord => Unit): Unit = {
    val facebook = new FacebookFactory().getInstance

    val foundGroups = facebook.search().searchGroups("ZaginioneKrakow")
    val foundGroup = foundGroups.get(0)
    val result = facebook.groups().getGroupFeed(String.valueOf(foundGroup.getId))
    println(s"Found ${result.size()} posts")

    val indexes = 0 until result.size()

    indexes
      .iterator
      .map(result.get)
      .map(postToRecord)
      .foreach(recordConsumer)
  }

  def postToRecord(post: Post): RawRecord = {
    println(s"[link: ${post.getLink}, ${post.getUpdatedTime}] ${post.getMessage}")
    val uuid = UUID.randomUUID()
    var picturePath: Option[Path] = Option.empty
    if (post.getPicture != null) {
      picturePath = Some(ImageUtil.saveImage(uuid, post.getPicture.openStream()))
    }

    RawRecord(uuid.toString, LOST, post.getMessage, post.getUpdatedTime.getTime,
      picturePath.map(_.toString).toList, Option(post.getLink).map(_.toString).orNull)
  }
}

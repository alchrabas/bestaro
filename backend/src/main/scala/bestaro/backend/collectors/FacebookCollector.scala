package bestaro.backend.collectors

import java.nio.file.{Path, Paths}
import java.time.Instant
import java.util.Random
import java.util.concurrent.TimeUnit

import bestaro.backend.types.RawRecord
import bestaro.common.types._
import bestaro.locator.types.{FullLocation, Voivodeship}
import bestaro.backend.util.ImageUtil
import facebook4j._

import scala.collection.JavaConverters._
import scala.collection.mutable

case class FacebookGroup(name: String, id: String,
                         voivodeshipRestriction: Option[Voivodeship])

class FacebookCollector(recordConsumer: RawRecord => Unit, isAlreadyStored: RawRecord => Boolean) {

  private val OLDEST_DATE_TO_COLLECT = TimeUnit.DAYS.toSeconds(1 * 30)
  private val POSTS_FETCHED_PER_PAGE = 30

  private val GROUPS_TO_SEARCH = Seq(
    FacebookGroup("ZaginioneKrakow", "396743770370604", Some(Voivodeship.MALOPOLSKIE)),
    FacebookGroup("ZaginioneNowyTarg", "635559053174109", Some(Voivodeship.MALOPOLSKIE)),
    FacebookGroup("ZaginioneMalopolska", "1064279656952087", Some(Voivodeship.MALOPOLSKIE)),
    FacebookGroup("ZaginioneWroclaw", "147269725398198", Some(Voivodeship.DOLNOSLASKIE)),
    FacebookGroup("ZaginioneWarszawa", "211708982280762", Some(Voivodeship.MAZOWIECKIE)),
    FacebookGroup("ZaginionePodkarpacie", "1714486045490643", Some(Voivodeship.PODKARPACKIE)),
    FacebookGroup("ZaginioneLodz", "450184494994903", Some(Voivodeship.LODZKIE)),
    FacebookGroup("ZaginioneKujawskoPomorskie", "1027349197280520", Some(Voivodeship.KUJAWSKO_POMORSKIE)),
    FacebookGroup("ZaginioneLublinLubelskie", "416563518550942", Some(Voivodeship.LUBELSKIE)),
    FacebookGroup("ZaginioneLubuskie", "1521330538114927", Some(Voivodeship.LUBUSKIE)),
    FacebookGroup("ZaginioneLubuskieZielonaGoraGorzow", "655496414515533", Some(Voivodeship.LUBUSKIE)),
    FacebookGroup("ZaginionePoznan", "307623212630596", Some(Voivodeship.WIELKOPOLSKIE)),
    FacebookGroup("ZaginioneZachodniopomorskie", "374107929376049", Some(Voivodeship.ZACHODNIOPOMORSKIE)),
    FacebookGroup("ZaginioneTrojmiasto", "209768362473608", Some(Voivodeship.POMORSKIE)),
    FacebookGroup("PoszukiwaneOpolskie", "580836405327224", Some(Voivodeship.OPOLSKIE)),
    FacebookGroup("ZaginioneBialystokGizyckoSuwalki", "306054069477023", Some(Voivodeship.PODLASKIE)),
    FacebookGroup("ZaginioneSlaskIOkolice", "586787474694575", Some(Voivodeship.SLASKIE)),
    FacebookGroup("GornySlaskDlaZwierzat", "377426005706051", Some(Voivodeship.SLASKIE)),
    FacebookGroup("ZaginoneSwietokrzyskie", "1405798499654655", Some(Voivodeship.SWIETOKRZYSKIE)),
    FacebookGroup("ZaginioneZnalezioneOlsztynOkolice", "1833070163614017", Some(Voivodeship.WARMINSKO_MAZURSKIE)),
  )

  private val facebook = new FacebookFactory().getInstance

  def collect(): Unit = {
    GROUPS_TO_SEARCH.foreach(searchInGroup(_, facebook))
  }

  // start with a random group
  private var groupIndex = new Random().nextInt(GROUPS_TO_SEARCH.size)

  def collectFromNextGroup(): Unit = {
    groupIndex += 1
    groupIndex %= GROUPS_TO_SEARCH.size

    searchInGroup(GROUPS_TO_SEARCH(groupIndex), facebook)
  }

  private def searchInGroup(group: FacebookGroup, facebook: Facebook) {
    var allFoundPosts = 0
    val eater = new FacebookEater(facebook, group.id)

    var continueFetching = true
    while (continueFetching) {
      val result = eater.fetch()
      println(s"Found ${result.size()} fb posts")
      allFoundPosts += result.size()

      val records = result
        .asScala
        .map(postToRecord(_, group))

      continueFetching = thereIsAtLeastOneRecordToSave(records)
      records.foreach(recordConsumer)
    }
    println(s"Collected $allFoundPosts from group ${group.name}")
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
    private val READING = new Reading().limit(POSTS_FETCHED_PER_PAGE)
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

  def postToRecord(post: Post, group: FacebookGroup): RawRecord = {
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
      "FB-" + group.id,
      picturePath.map(_.toString).toList,
      Option(post.getPermalinkUrl).map(_.toString).orNull,
      secondaryMessage = sharedPostMessage.getOrElse(""),
      fullLocation = FullLocation(None, None, group.voivodeshipRestriction, None))
  }
}

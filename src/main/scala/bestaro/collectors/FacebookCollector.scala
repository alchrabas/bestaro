package bestaro.collectors

import java.nio.file.{Files, Paths, StandardCopyOption}

import facebook4j.FacebookFactory

import scala.util.Random

class FacebookCollector {

  val random = new Random()

  def collect(): Unit = {
    val facebook = new FacebookFactory().getInstance

    val foundGroups = facebook.search().searchGroups("ZaginioneKrakow")
    val foundGroup = foundGroups.get(0)
    val result = facebook.groups().getGroupFeed(String.valueOf(foundGroup.getId))
    println(s"Found ${result.size()} posts")
    for (i <- 0 to 3) {
      val a = result.get(i)
      println(s"[link: ${a.getLink}, ${a.getUpdatedTime}] ${a.getMessage}")
      if (a.getPicture != null) {
        Files.copy(a.getPicture.openStream(),
          Paths.get("pictures", random.nextString(10)),
          StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }
}

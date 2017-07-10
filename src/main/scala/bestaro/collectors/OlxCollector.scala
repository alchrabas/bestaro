package bestaro.collectors

import java.net.URL
import java.util.UUID

import bestaro.core.ProgressStatus.LOST
import bestaro.core.RawRecord
import bestaro.extractors.PolishDateExtractor
import bestaro.util.ImageUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.language.postfixOps

class OlxCollector {

  def collect(recordConsumer: RawRecord => Unit): Unit = {
    collectLostPets(recordConsumer)
    collectFoundPets(recordConsumer)
  }

  def collectLostPets(recordConsumer: (RawRecord) => Unit): Unit = {
    collectPetsFromListOnUrl(recordConsumer, 1, "https://www.olx.pl/zwierzeta/zaginione-i-znalezione/krakow/?search%5Bfilter_enum_lostfound%5D%5B0%5D=lost")
  }

  def collectFoundPets(recordConsumer: (RawRecord) => Unit): Unit = {
    collectPetsFromListOnUrl(recordConsumer, 1, "https://www.olx.pl/zwierzeta/zaginione-i-znalezione/krakow/?search%5Bfilter_enum_lostfound%5D%5B0%5D=found")
  }

  def collectPetsFromListOnUrl(recordConsumer: RawRecord => Unit, page: Int, url: String): Unit = {
    val doc = requestSlowly(url)
    val offerTables = doc.select("#offers_table").first().children()

    val allTables = offerTables.select("table.fixed.breakword").subList(0, 1)

    (0 until allTables.size)
      .map(allTables.get)
      .map(_.select("a").first().attr("href"))
      .map(requestSlowly)
      .map(collectAdvertisementDetails(_, (id, pictureUrl) => requestImageSlowly(id, pictureUrl)))
      .foreach(recordConsumer)

    enterAnotherPage(recordConsumer, page, url, doc)
  }

  def enterAnotherPage(recordConsumer: (RawRecord) => Unit, page: Int, url: String, doc: Document): Unit = {
    if (nextPageExists(page, url, doc)) {
      collectPetsFromListOnUrl(recordConsumer, page + 1, url)
    }
  }

  def nextPageExists(page: Int, url: String, doc: Document): Boolean = {
    val nextPageString = String.valueOf(page + 1)
    val nextPageLink = "a[href=\"" + url + "&page=" + nextPageString + "\"]"
    val nextPageLinkExists = doc.select(nextPageLink).size() > 0
    nextPageLinkExists
  }

  // todo it would be better to avoid this callback
  def collectAdvertisementDetails(adDocument: Document,
                                  picturesLoader: (UUID, String) => Unit
                                 ): RawRecord = {
    val uuid = UUID.randomUUID()
    val locationString = adDocument.select(".show-map-link > strong").text()
    val messageContent = adDocument.select("#textContent").text()
    val title = adDocument.select("title").text()
    val dateString = adDocument.select(".offer-titlebox__details > em").text()

    val pictures = adDocument.select(".img-item img").eachAttr("src").toList
    val url = adDocument.select("link[rel=canonical]").attr("href")

    val polishDateExtractor = new PolishDateExtractor()
    val extractedDate = polishDateExtractor.parse(dateString)
    val extractedEventDate = polishDateExtractor.parse(messageContent)

    if (pictures.nonEmpty) {
      picturesLoader(uuid, pictures.get(0))
    }

    RawRecord(uuid.toString, LOST, messageContent,
      postDate = extractedDate.map(_.toEpochMilli).getOrElse(1L),
      location = locationString, title = title,
      pictures = pictures,
      eventDate = extractedEventDate.map(_.toEpochMilli).getOrElse(2L),
      link = url
    )
  }

  def requestImageSlowly(id: UUID, url: String): Unit = {
    Thread.sleep(10 * 1000)
    println("Requesting image: " + url)
    ImageUtil.saveImage(id, new URL(url).openStream())
  }

  def requestSlowly(url: String): Document = {
    Thread.sleep(10 * 1000)

    println("Requesting: " + url)
    Jsoup.connect(url).get()
  }
}

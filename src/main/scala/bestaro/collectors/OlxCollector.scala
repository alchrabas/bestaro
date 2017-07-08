package bestaro.collectors

import java.util.UUID

import bestaro.ProgressStatus.LOST
import bestaro.RawRecord
import bestaro.extractors.PolishDateExtractor
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

    val allTables = offerTables.select("table.fixed.breakword")

    (0 until allTables.size)
      .map(allTables.get)
      .map(_.select("a").first().attr("href"))
      .map(requestSlowly)
      .map(collectAdvertisementDetails)
      .foreach(recordConsumer)

    val nextPageString = String.valueOf(page + 1)
    val nextPageLinkExists = doc.select("a[href='" + url + "&page=" + nextPageString + "'").size() > 0
    if (nextPageLinkExists) {
      collectPetsFromListOnUrl(recordConsumer, page + 1, url)
    }
  }

  def collectAdvertisementDetails(adDocument: Document): RawRecord = {
    val uuid = UUID.randomUUID()
    val locationString = adDocument.select(".show-map-link > strong").text()
    val messageContent = adDocument.select("#textContent").text()
    val title = adDocument.select("title").text()
    val dateString = adDocument.select(".offer-titlebox__details > em").text()

    val pictures = adDocument.select(".img-item img").eachAttr("src").toList
    val url = adDocument.select("link[rel=canonical]").attr("href")

    val extractedDate = new PolishDateExtractor().parse(dateString)
    val extractedEventDate = new PolishDateExtractor().parse(messageContent)

    RawRecord(uuid.toString, LOST, messageContent,
      postDate = extractedDate.map(_.toEpochMilli).getOrElse(1L),
      location = locationString, title = title,
      pictures = pictures,
      eventDate = extractedEventDate.map(_.toEpochMilli).getOrElse(2L),
      link = url
    )
  }

  def requestSlowly(url: String): Document = {
    Thread.sleep(10 * 1000)

    Jsoup.connect(url).get()
  }
}

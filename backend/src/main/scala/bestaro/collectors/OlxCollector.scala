package bestaro.collectors

import java.net.URL

import bestaro.collectors.util.HttpDownloader
import bestaro.common.types._
import bestaro.core.RawRecord
import bestaro.extractors.PolishDateExtractor
import bestaro.util.ImageUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.language.postfixOps

class OlxCollector(recordConsumer: RawRecord => Unit, httpDownloader: HttpDownloader) {

  def collect(): Unit = {
    collectLostPets()
    collectFoundPets()
  }

  def collectLostPets(): Unit = {
    collectPetsFromListOnUrl(1, EventType.LOST, "https://www.olx.pl/zwierzeta/zaginione-i-znalezione/krakow/?search%5Bfilter_enum_lostfound%5D%5B0%5D=lost")
  }

  def collectFoundPets(): Unit = {
    collectPetsFromListOnUrl(1, EventType.FOUND, "https://www.olx.pl/zwierzeta/zaginione-i-znalezione/krakow/?search%5Bfilter_enum_lostfound%5D%5B0%5D=found")
  }

  def collectPetsFromListOnUrl(page: Int, eventType: EventType, url: String): Unit = {
    val doc = requestSlowly(url)
    val offerTables = doc.select("#offers_table").first().children()

    val allTables = offerTables.select("table.fixed.breakword")

    (0 until allTables.size)
      .iterator
      .map(allTables.get)
      .map(_.select("a").first().attr("href"))
      .map(requestSlowly)
      .map(collectAdvertisementDetails(_, eventType))
      .foreach(recordConsumer)

    enterAnotherPage(page, eventType, url, doc)
  }

  def enterAnotherPage(page: Int,
                       eventType: EventType, url: String, doc: Document): Unit = {
    if (nextPageExists(page, url, doc)) {
      collectPetsFromListOnUrl(page + 1, eventType, url)
    }
  }

  def nextPageExists(page: Int, url: String, doc: Document): Boolean = {
    val nextPageString = String.valueOf(page + 1)
    val nextPageLink = "a[href=\"" + url + "&page=" + nextPageString + "\"]"
    val nextPageLinkExists = doc.select(nextPageLink).size() > 0
    nextPageLinkExists
  }

  def collectAdvertisementDetails(adDocument: Document, eventType: EventType): RawRecord = {
    val locationString = adDocument.select(".show-map-link > strong").text()
    val messageContent = adDocument.select("#textContent").text()
    val title = adDocument.select("title").text()
    val dateString = adDocument.select(".offer-titlebox__details > em").text()
    val idString = adDocument.select(".offer-titlebox__details > em > small").text()

    val pictures = adDocument.select(".img-item img").eachAttr("src").asScala.toList
    val url = adDocument.select("link[rel=canonical]").attr("href")

    val polishDateExtractor = new PolishDateExtractor()
    val extractedDate = polishDateExtractor.parse(dateString)
    val extractedEventDate = polishDateExtractor.parse(messageContent)

    val id = parseId(idString)
    if (pictures.nonEmpty) {
      requestImageSlowly(id, pictures.head, 1)
    }

    RawRecord(id, EventType.LOST, AnimalType.UNKNOWN,
      messageContent,
      extractedDate.map(_.toEpochMilli).getOrElse(1L),
      dataSource = "OLX-0",
      location = locationString, title = title,
      pictures = pictures,
      eventDate = extractedEventDate.map(_.toEpochMilli).getOrElse(2L),
      link = url
    )
  }

  private def parseId(idString: String): RecordId = {
    val idRegex = "ID ogłoszenia: (\\d+)".r
    idString match {
      case idRegex(identifier) => OlxId(identifier)
    }
  }

  def requestImageSlowly(id: RecordId, url: String, serialId: Int): Unit = {
    ImageUtil.saveImage(id, serialId, httpDownloader.downloadResource(new URL(url)))
  }

  def requestSlowly(url: String): Document = {
    Jsoup.parse(httpDownloader.downloadResource(new URL(url)), "UTF-8", url)
  }
}

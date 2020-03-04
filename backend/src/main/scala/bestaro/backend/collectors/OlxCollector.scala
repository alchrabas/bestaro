package bestaro.backend.collectors

import java.net.URL
import java.nio.file.{Path, Paths}
import java.util.Random

import bestaro.backend.collectors.util.HttpDownloader
import bestaro.backend.database.DatabaseWrapper
import bestaro.backend.extractors.PolishDateExtractor
import bestaro.backend.types._
import bestaro.backend.util.ImageUtil
import bestaro.locator.types.{FullLocation, Voivodeship}
import bestaro.locator.util.PolishCharactersAsciizer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.language.postfixOps

class OlxCollector(recordConsumer: RawRecord => Unit, httpDownloader: HttpDownloader) {

  private val asciizer = new PolishCharactersAsciizer

  def collect(): Unit = {
    Voivodeship.values.foreach(collectForVoivideship)
  }

  // start with a random voivodeship
  private var voivodeshipIndex = new Random().nextInt(Voivodeship.values.size)

  def collectFromNextVoivodeship(): Unit = {
    voivodeshipIndex += 1
    voivodeshipIndex %= Voivodeship.values.size

    collectForVoivideship(Voivodeship.values(voivodeshipIndex))
  }

  def collectForVoivideship(voivodeship: Voivodeship): Unit = {
    collectLostPets(voivodeship)
    collectFoundPets(voivodeship)
  }

  private def olxAcceptableName(voivodeship: Voivodeship): String = {
    asciizer.convertToAscii(voivodeship.entryName).toLowerCase
  }

  def collectLostPets(voivodeship: Voivodeship): Unit = {
    collectPetsFromListOnUrl(1, EventType.LOST, voivodeship, s"https://www.olx.pl/zwierzeta/zaginione-i-znalezione/${olxAcceptableName(voivodeship)}/?search%5Bfilter_enum_lostfound%5D%5B0%5D=lost")
  }

  def collectFoundPets(voivodeship: Voivodeship): Unit = {
    collectPetsFromListOnUrl(1, EventType.FOUND, voivodeship, s"https://www.olx.pl/zwierzeta/zaginione-i-znalezione/${olxAcceptableName(voivodeship)}/?search%5Bfilter_enum_lostfound%5D%5B0%5D=found")
  }

  def collectPetsFromListOnUrl(page: Int, eventType: EventType, voivodeship: Voivodeship, url: String): Unit = {
    val doc = requestSlowly(url)
    val offerTables = doc.select("#offers_table").first().children()

    val allTables = offerTables.select("table.fixed.breakword")

    val existingRecordsCount = (0 until allTables.size)
      .iterator
      .map(allTables.get)
      .filterNot(row =>
        DatabaseWrapper.recordIdAlreadyExists(
          OlxId(row.attr("data-id"))
        ))
      .map(_.select("a").first().attr("href"))
      .map(requestSlowly)
      .map(collectAdvertisementDetails(_, eventType, voivodeship))
      .map { a => recordConsumer(a); println("SAVE: " + a.message); a }.size

    if (atLeastHalfOfRecordsAreNew(existingRecordsCount, allTables.size())) {
      enterAnotherPage(page, eventType, voivodeship, url, doc)
    }
  }

  def atLeastHalfOfRecordsAreNew(existing: Int, all: Int): Boolean = {
    existing * 0.5 < all
  }

  def enterAnotherPage(page: Int,
                       eventType: EventType, voivodeship: Voivodeship, url: String, doc: Document): Unit = {
    if (nextPageExists(page, url, doc)) {
      collectPetsFromListOnUrl(page + 1, eventType, voivodeship, url)
    }
  }

  def nextPageExists(page: Int, url: String, doc: Document): Boolean = {
    val nextPageString = String.valueOf(page + 1)
    val nextPageLink = "a[href=\"" + url + "&page=" + nextPageString + "\"]"
    val nextPageLinkExists = doc.select(nextPageLink).size() > 0
    nextPageLinkExists
  }

  def collectAdvertisementDetails(adDocument: Document, eventType: EventType, voivodeship: Voivodeship): RawRecord = {
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

    var picturePath: Option[Path] = Option.empty
    if (pictures.nonEmpty) { // todo extract common code from FbCollector
      try {
        val potentialPicturePath = ImageUtil.generatePicturePath(ImageUtil.pictureName(id, 1))
        if (potentialPicturePath.toFile.exists()) { // avoid fetching image when it's already there
          picturePath = Some(Paths.get(ImageUtil.pictureName(id, 1)))
        } else {
          picturePath = Some(requestImageSlowly(id, pictures.head, 1))
        }
      } catch {
        case _: Exception => println("Unable to save the picture for ID " + id)
      }
    }

    RawRecord(id, eventType, AnimalType.UNKNOWN,
      messageContent,
      extractedDate.map(_.toEpochMilli).getOrElse(1L),
      dataSource = "OLX-" + voivodeship.entryName,
      location = locationString,
      title = title,
      pictures = picturePath.map(_.toString).toList,
      eventDate = extractedEventDate.map(_.toEpochMilli).getOrElse(2L),
      link = url,
      fullLocation = FullLocation(None, None, Some(voivodeship), None)
    )
  }

  private def parseId(idString: String): RecordId = {
    val idRegex = "ID ogłoszenia: (\\d+)".r
    idString match {
      case idRegex(identifier) => OlxId(identifier)
    }
  }

  def requestImageSlowly(id: RecordId, url: String, serialId: Int): Path = {
    ImageUtil.saveImageForRecord(id, serialId, httpDownloader.downloadResource(new URL(url)))
  }

  def requestSlowly(url: String): Document = {
    Jsoup.parse(httpDownloader.downloadResource(new URL(url)), "UTF-8", url)
  }
}

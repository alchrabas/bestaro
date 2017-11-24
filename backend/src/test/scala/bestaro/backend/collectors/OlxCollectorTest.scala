package bestaro.backend.collectors

import java.net.URL
import java.time.{Instant, LocalDate, Month, ZoneOffset}

import bestaro.backend.collectors.util.HttpDownloader
import bestaro.backend.types.RawRecord
import bestaro.common.types.EventType
import org.jsoup.Jsoup
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSpec

class OlxCollectorTest extends FunSpec with MockFactory {

  describe("Collector") {
    val saveMock = (_: RawRecord) => {}

    it("should get the data from olxSamplePage") {
      val httpDownloaderMock = mock[HttpDownloader]
      (httpDownloaderMock.downloadResource _).expects(
        new URL("https://olxpl-ring09.akamaized.net/images_tablicapl/103525969_4_644x461_bura-pregowana-starsza-kotka-zwierzeta.jpg"))
        .returning(this.getClass.getResourceAsStream("/paw.png")).once

      val collector = new OlxCollector(saveMock, httpDownloaderMock)
      val doc = Jsoup.parse(getClass.getResourceAsStream("/olxSamplePage.html"), "UTF-8",
        "https://www.olx.pl/oferta/bura-pregowana-starsza-kotka-CID103-ID40upd.html#9537ecc7fe")
      val record = collector.collectAdvertisementDetails(doc, EventType.UNKNOWN)

      assert(record.eventType == EventType.LOST)
      assert(record.title == "Bura, pręgowana, starsza KOTKA ! Kraków Nowa Huta • OLX.pl")
      assert(record.postDate == toInstant(2017, Month.JUNE, 26).toEpochMilli)
      assert(record.eventDate == toInstant(2013, Month.OCTOBER, 25).toEpochMilli)
      assert(record.location == "Kraków, Małopolskie, Nowa Huta")
      assert(record.pictures.sorted == List(
        "https://olxpl-ring09.akamaized.net/images_tablicapl/103525969_4_644x461_bura-pregowana-starsza-kotka-zwierzeta.jpg",
        "https://olxpl-ring09.akamaized.net/images_tablicapl/103525969_1_644x461_bura-pregowana-starsza-kotka-krakow.jpg",
        "https://olxpl-ring09.akamaized.net/images_tablicapl/103525969_2_644x461_bura-pregowana-starsza-kotka-dodaj-zdjecia.jpg",
        "https://olxpl-ring09.akamaized.net/images_tablicapl/103525969_3_644x461_bura-pregowana-starsza-kotka-zaginione-i-znalezione.jpg",
        "https://olxpl-ring09.akamaized.net/images_tablicapl/103525969_5_644x461_bura-pregowana-starsza-kotka-malopolskie.jpg"
      ).sorted)
      assert(record.link == "https://www.olx.pl/oferta/bura-pregowana-starsza-kotka-CID103-ID40upd.html")
    }

    it("should know if a next page exists") {
      val httpDownloaderMock = mock[HttpDownloader]
      val collector = new OlxCollector(saveMock, httpDownloaderMock)
      val PAGE_URL = "https://www.olx.pl/zwierzeta/zaginione-i-znalezione/malopolskie/?search%5Bfilter_enum_lostfound%5D%5B0%5D=lost"
      val doc = Jsoup.parse(getClass.getResourceAsStream("/olxListPage.html"), "UTF-8", PAGE_URL)
      assert(collector.nextPageExists(2, PAGE_URL, doc) === true)
      assert(collector.nextPageExists(4, PAGE_URL, doc) === false)
    }
  }

  def toInstant(year: Int, month: Month, day: Int): Instant = {
    LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.UTC)
  }
}

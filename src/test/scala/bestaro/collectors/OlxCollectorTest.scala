package bestaro.collectors

import java.time.{Instant, LocalDate, Month, ZoneOffset}

import bestaro.ProgressStatus.LOST
import org.jsoup.Jsoup
import org.scalatest.FunSpec

class OlxCollectorTest extends FunSpec {

  describe("Collector") {
    it("should get the data from olxSamplePage") {
      val collector = new OlxCollector()
      val doc = Jsoup.parse(getClass.getResourceAsStream("/olxSamplePage.html"), "UTF-8",
        "https://www.olx.pl/oferta/bura-pregowana-starsza-kotka-CID103-ID40upd.html#9537ecc7fe")
      val record = collector.collectAdvertisementDetails(doc)

      assert(record.status == LOST)
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
  }

  def toInstant(year: Int, month: Month, day: Int): Instant = {
    LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.UTC)
  }
}

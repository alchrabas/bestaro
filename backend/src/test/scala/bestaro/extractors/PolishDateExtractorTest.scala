package bestaro.extractors

import java.time._

import org.scalatest.FunSpec

class PolishDateExtractorTest extends FunSpec {

  describe("Date extractor") {
    val dateExtractor = new PolishDateExtractor()
    describe("should extract date") {
      it("from a simple word string") {
        assert(dateExtractor.parse("11 stycznia 2015").contains(toInstant(2015, Month.JANUARY, 11)))
        assert(dateExtractor.parse("25 lutego").contains(toInstant(LocalDate.now().getYear, Month.FEBRUARY, 25)))
      }

      it("from an abbreviated month string") {
        assert(dateExtractor.parse("17 paź").contains(toInstant(LocalDate.now().getYear, Month.OCTOBER, 17)))
        assert(dateExtractor.parse("17 paź 2021").contains(toInstant(2021, Month.OCTOBER, 17)))
      }

      it("from a simple number string") {
        assert(dateExtractor.parse("11.10.2015").contains(toInstant(2015, Month.OCTOBER, 11)))
        assert(dateExtractor.parse("25.11").contains(toInstant(LocalDate.now().getYear, Month.NOVEMBER, 25)))
        assert(dateExtractor.parse("13-11-1999").contains(toInstant(1999, Month.NOVEMBER, 13)))
        assert(dateExtractor.parse("17/11/2020").contains(toInstant(2020, Month.NOVEMBER, 17)))
      }

      it("from olx-style post date") {
        assert(dateExtractor.parse("Dodane o 22:42, 26 czerwca 2017").contains(toInstant(2017, Month.JUNE, 26)))
      }
    }

    describe("should fail to extract date") {
      it("from invalid text string") {
        assert(dateExtractor.parse("ala123").isEmpty)
        assert(dateExtractor.parse("21 stycz1 44").isEmpty)
        assert(dateExtractor.parse("2 2 2").isEmpty)
      }

      it("from invalid number string") {
        assert(dateExtractor.parse("ala123").isEmpty)
        assert(dateExtractor.parse("21 23 44").isEmpty)
        assert(dateExtractor.parse("2 2 2").isEmpty)
      }
    }
  }

  def toInstant(year: Int, month: Month, day: Int): Instant = {
    LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.UTC)
  }
}

package bestaro.extractors

import java.time.{Instant, LocalDate, ZoneOffset}

import bestaro.util.PolishCharactersAsciizer

import scala.util.Try


class PolishDateExtractor {

  val MONTHS = Map(
    "sty" -> 1,
    "lut" -> 2,
    "mar" -> 3,
    "kwi" -> 4,
    "maj" -> 5,
    "cze" -> 6,
    "lip" -> 7,
    "sie" -> 8,
    "wrz" -> 9,
    "paz" -> 10,
    "lis" -> 11,
    "gru" -> 12
  )

  private val asciizer = new PolishCharactersAsciizer

  def parse(dateString: String): Option[Instant] = {
    val letters = "[a-ząćęłńóśżźA-ZĄĆĘŁŃÓŚŻŹ]"
    val stringifiedMonthAndYear = ("(?:.*[^\\d])?(\\d{1,2})\\s+(" + letters + "+)\\s+(\\d{1,4})(?:[^\\d].*)?").r
    val stringifiedMonth = ("(?:.*[^\\d])?(\\d{1,2})\\s+(" + letters + "+)(?:\\s.*)?").r
    val numericMonthAndYear = "(?:.*[^\\d])?(\\d{1,2})[./-](\\d{1,2})[./-](\\d{1,4})(?:[^\\d].*)?".r
    val numericMonth = "(?:.*[^\\d])?(\\d{1,2})[./-](\\d{1,2})(?:\\s.*)?".r

    val normalizedString = asciizer.convertToAscii(dateString).toLowerCase
    normalizedString match {
      case stringifiedMonthAndYear(day, month, year) =>
        Try {
          Instant.from(LocalDate.of(
            Integer.valueOf(year),
            MONTHS.getOrElse(month.substring(0, 3), -1),
            Integer.valueOf(day))
            .atStartOfDay().toInstant(ZoneOffset.UTC))
        }.toOption
      case stringifiedMonth(day, month) =>
        Try {
          Instant.from(LocalDate.of(
            LocalDate.now().getYear,
            MONTHS.getOrElse(month.substring(0, 3), -1),
            Integer.valueOf(day))
            .atStartOfDay().toInstant(ZoneOffset.UTC))
        }.toOption
      case numericMonthAndYear(day, month, year) =>
        Try {
          Instant.from(LocalDate.of(
            Integer.valueOf(year),
            Integer.valueOf(month),
            Integer.valueOf(day))
            .atStartOfDay().toInstant(ZoneOffset.UTC))
        }.toOption
      case numericMonth(day, month) =>
        Try {
          Instant.from(LocalDate.of(
            LocalDate.now().getYear,
            Integer.valueOf(month),
            Integer.valueOf(day))
            .atStartOfDay().toInstant(ZoneOffset.UTC))
        }.toOption
      case _ => {
        None
      }
    }
  }
}

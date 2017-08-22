package bestaro.core.processors


import bestaro.core.ProgressStatus.LOST
import bestaro.core.{FbId, RawRecord, RecordId, Tokenizer}
import bestaro.extractors.{GusLocationExtractor, MatchedLocation}
import bestaro.helpers.TaggedRecordsManager.TaggedRecord
import bestaro.service.Voivodeship
import org.scalatest.FunSpec

class GusLocationExtractorTest extends FunSpec {

  describe("Should extract") {
    it("not inflected two-word street name") {
      val record = RawRecord(FbId("123"), LOST, "Dziś na ulicy Monte Cassino", 1, Voivodeship.MALOPOLSKIE, List())
      val taggedRecords: Map[RecordId, TaggedRecord] = Map(FbId("123") ->
        TaggedRecord(FbId("123"), List("Monte Cassino"), List(), List(), "", "")
      )

      val locationExtractor = new GusLocationExtractor()
      val plaintextProcessor = new PlaintextProcessor
      val tokenizedText = new Tokenizer().tokenize(record.message)
      val (tokens, result) = locationExtractor.extractLocationName(tokenizedText, Voivodeship.MALOPOLSKIE)
      assert(result == List(
        MatchedLocation(Location("monte cassino", "Monte Cassino", "ulica"), 3, 2)
      ))
    }
  }
}

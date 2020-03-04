package unused_code

import bestaro.backend.core._
import bestaro.backend.helpers.TaggedRecordsManager.TaggedRecord
import bestaro.backend.types.{EventType, FbId, RecordId}
import bestaro.locator.extractors.MatchedFullLocation
import bestaro.locator.types.{Location, LocationType, Voivodeship}

class GusLocationExtractorTest extends FunSpec {

  describe("Should extract") {
    it("not inflected two-word street name") {
      val record = RawRecord(FbId("123"), EventType.LOST, "Dziś na ulicy Monte Cassino", 1, Voivodeship.MALOPOLSKIE, List())
      val taggedRecords: Map[RecordId, TaggedRecord] = Map(FbId("123") ->
        TaggedRecord(FbId("123"), List("Monte Cassino"), List(), List(), "", "")
      )

      val locationExtractor = new GusLocationExtractor()
      val plaintextProcessor = new PlaintextProcessor
      val tokenizedText = new Tokenizer().tokenize(record.message)
      val (tokens, result) = locationExtractor.extractLocation(tokenizedText, Voivodeship.MALOPOLSKIE)
      assert(result == List(
        MatchedFullLocation(FullLocation(
          Some(Location("monte cassino", "Monte Cassino", LocationType.STREET)),
          None, None),
          3, 2)
      ))
    }
  }
}

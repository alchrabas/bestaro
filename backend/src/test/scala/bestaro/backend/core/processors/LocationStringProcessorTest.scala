package bestaro.backend.core.processors

import bestaro.common.types._
import bestaro.core.RawRecord
import bestaro.core.processors.LocationStringProcessor
import bestaro.locator.LocatorDatabase
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSpec

class LocationStringProcessorTest extends FunSpec with MockFactory {

  describe("Location String Processor") {

    val locStringProcessor = new LocationStringProcessor(new LocatorDatabase("testDb.sqlite"))

    it("should extract voivodeship info from location string") {
      val updatedRecord = locStringProcessor.process(someRecord("QAZWER, małopolska"))
      assert(updatedRecord.fullLocation == FullLocation(None, None, Some(Voivodeship.MALOPOLSKIE), None))
    }

    it("should not overwrite already known voivodeship") {
      val a = someRecord("QAZWER, małopolska",
        FullLocation(None, None, Some(Voivodeship.PODKARPACKIE), None))
      val updatedRecord = locStringProcessor.process(a)
      assert(updatedRecord.fullLocation == FullLocation(None, None, Some(Voivodeship.PODKARPACKIE), None))
    }

    it("should extract secondary location info") {
      val updatedRecord = locStringProcessor.process(someRecord("nowy sącz, województwo małopolskie"))
      assert(updatedRecord.fullLocation == FullLocation(None,
        Some(Location("nowy sącz", "Nowy Sącz", LocationType.CITY)),
        Some(Voivodeship.MALOPOLSKIE), None))
    }
  }

  def someRecord(recordLocation: String,
                 fullLocation: FullLocation = FullLocation(None, None, None, None)
                ): RawRecord = {
    RawRecord(FbId("abc"),
      EventType.LOST, AnimalType.UNKNOWN,
      "", 1L, "DS",
      location = recordLocation,
      fullLocation = fullLocation)
  }
}

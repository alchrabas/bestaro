package bestaro.service

import java.lang.reflect.Type

import com.google.gson.reflect.TypeToken
import fr.dudie.nominatim.client.NominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import fr.dudie.nominatim.client.request.paramhelper.PolygonFormat
import fr.dudie.nominatim.model.Address

import scala.collection.JavaConverters._
import scala.util.Random

class CachedNominatimClient(nominatimClient: NominatimClient,
                            requestLogger: String => Unit = _ => Unit)
  extends GeocodingCacheTrait[java.util.List[Address]] {

  private type listOfAddresses = java.util.List[Address]
  private type mapOfAddresses = java.util.Map[String, listOfAddresses]

  def search(queryString: String): List[Address] = {
    (if (existsInCache(queryString)) {
      loadFromCache(queryString)
    } else {
      val searchRequest = new NominatimSearchRequest()
      searchRequest.addCountryCode("pl")
      searchRequest.setAcceptLanguage("pl,en")
      searchRequest.setAddress(true)
      searchRequest.setPolygonFormat(PolygonFormat.NONE)

      searchRequest.setQuery(queryString)
      Thread.sleep(1100 + Random.nextInt(100))
      val nominatimResults = nominatimClient.search(searchRequest)
      saveInCache(queryString, nominatimResults)

      requestLogger(queryString)

      nominatimResults
    }).asScala.toList
  }

  override def cacheFileName(): String = {
    "nominatimCache.json"
  }

  override def cacheType(): Type = {
    new TypeToken[mapOfAddresses]() {}.getType
  }
}

package bestaro.service

import bestaro.util.FileIO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.dudie.nominatim.client.NominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import fr.dudie.nominatim.client.request.paramhelper.PolygonFormat
import fr.dudie.nominatim.model.Address

import scala.collection.JavaConverters._

class CachedNominatimClient(nominatimClient: NominatimClient) {

  private val gson = new Gson()

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
      val nominatimResults = nominatimClient.search(searchRequest)
      saveInCache(queryString, nominatimResults)

      nominatimResults
    }).asScala.toList
  }

  private type mapOfAddresses = java.util.Map[String, java.util.List[Address]]

  private def saveInCache(queryString: String, listToSave: java.util.List[Address]) = {
    val cacheEntries = loadMapOfAddresses()
    cacheEntries.put(queryString, listToSave)
    FileIO.saveFile("nominatimCache.json", gson.toJson(cacheEntries))
  }

  def existsInCache(queryString: String): Boolean = {
    loadMapOfAddresses().containsKey(queryString)
  }

  def loadFromCache(queryString: String): java.util.List[Address] = {
    loadMapOfAddresses().get(queryString)
  }

  private def loadMapOfAddresses(): mapOfAddresses = {
    val gsonType = new TypeToken[mapOfAddresses]() {}.getType
    gson.fromJson(FileIO.readFile("nominatimCache.json", "{}"), gsonType)
  }

}

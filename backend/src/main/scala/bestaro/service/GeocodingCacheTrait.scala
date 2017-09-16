package bestaro.service

import java.lang.reflect.Type

import bestaro.common.util.FileIO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

trait GeocodingCacheTrait[results] {

  private type mapOfResults = java.util.Map[String, results]

  def cacheFileName(): String

  def cacheType(): Type

  private val gson = new Gson()
  private var memoryCache: Option[mapOfResults] = None


  def resetCacheCache(): Unit = {
    memoryCache = None
  }

  def saveInCache(queryString: String, listToSave: results): Unit = {
    resetCacheCache()
    val cacheEntries = loadMapOfAddresses()
    cacheEntries.put(queryString, listToSave)
    FileIO.saveFile(cacheFileName(), gson.toJson(cacheEntries))
  }

  def existsInCache(queryString: String): Boolean = {
    loadMapOfAddresses().containsKey(queryString)
  }

  def loadFromCache(queryString: String): results = {
    loadMapOfAddresses().get(queryString)
  }

  def loadMapOfAddresses(): mapOfResults = {
    if (memoryCache.isEmpty) {
      val gsonType = cacheType()
      memoryCache = Some(gson.fromJson(FileIO.readFile(cacheFileName(), "{}"), gsonType))
    }
    memoryCache.get
  }
}

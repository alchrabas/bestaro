package bestaro.service

import java.io.FileInputStream
import java.lang.reflect.Type
import java.util.Properties

import com.google.gson.reflect.TypeToken
import com.google.maps.model.GeocodingResult
import com.google.maps.{GeoApiContext, GeocodingApi}

import scala.collection.JavaConverters._

class CachedGoogleApiClient(requestLogger: String => Unit = _ => Unit)
  extends GeocodingCacheTrait[java.util.List[GeocodingResult]] {

  private type listOfResults = java.util.List[GeocodingResult]
  private type mapOfResults = java.util.Map[String, listOfResults]

  private val propertyFileResource = getClass.getClassLoader.getResource("google-api.properties")
  private val googleApiPropertiesFIS = new FileInputStream(propertyFileResource.getFile)
  private val properties = new Properties()
  properties.load(googleApiPropertiesFIS)

  def search(queryString: String): List[GeocodingResult] = {
    (if (existsInCache(queryString)) {
      loadFromCache(queryString)
    } else {
      val context = new GeoApiContext.Builder().apiKey(properties.getProperty("apiKey")).build
      Thread.sleep(1000)
      val results: java.util.List[GeocodingResult] = GeocodingApi.geocode(context, queryString).await.toList.asJava
      saveInCache(queryString, results)

      requestLogger(queryString)

      results
    }).asScala.toList
  }

  override def cacheFileName(): String = {
    "googleGeocodingCache.json"
  }

  override def cacheType(): Type = {
    new TypeToken[mapOfResults]() {}.getType
  }
}

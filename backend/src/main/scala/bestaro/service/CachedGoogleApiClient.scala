package bestaro.service

import java.io.FileInputStream
import java.util.{Date, Properties}

import bestaro.locator.LocatorDatabase
import com.google.maps.model.GeocodingResult
import com.google.maps.{GeoApiContext, GeocodingApi}

import scala.collection.JavaConverters._

class CachedGoogleApiClient(locatorDatabase: LocatorDatabase,
                            requestLogger: String => Unit = _ => Unit
                           ) {

  private type listOfResults = java.util.List[GeocodingResult]

  private val propertyFileResource = getClass.getClassLoader.getResource("google-api.properties")
  private val googleApiPropertiesFIS = new FileInputStream(propertyFileResource.getFile)
  private val properties = new Properties()
  properties.load(googleApiPropertiesFIS)

  def search(queryString: String): List[GeocodingResult] = {
    locatorDatabase.retrieveFromCache(queryString).getOrElse {
      val context = new GeoApiContext.Builder()
        .apiKey(properties.getProperty("apiKey"))
        .queryRateLimit(40)
        .build
      val results = GeocodingApi.geocode(context, queryString)
        .language("pl").await.toList.asJava
      locatorDatabase.saveInCache(locatorDatabase.GoogleCacheEntry(queryString, results, new Date().getTime))

      requestLogger(queryString)

      results
    }.asScala.toList
  }
}

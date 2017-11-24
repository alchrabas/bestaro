package bestaro.backend

import java.io.FileInputStream
import java.util.Properties

object AppConfig {
  private val appPropertiesFIS = new FileInputStream("app.properties")
  private val properties = new Properties()
  properties.load(appPropertiesFIS)

  def getProperty(propertyName: String): String = properties.getProperty(propertyName)
}

package bestaro

import java.io.FileInputStream
import java.util.Properties

object AppConfig {
  private val propertyFileResource = getClass.getClassLoader.getResource("app.properties")
  private val appPropertiesFIS = new FileInputStream(propertyFileResource.getFile)
  private val properties = new Properties()
  properties.load(appPropertiesFIS)

  def getProperty(propertyName: String): String = properties.getProperty(propertyName)
}

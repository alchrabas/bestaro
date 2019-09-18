package data

import java.io.FileInputStream
import java.util.Properties

object S3Client {
  private val s3credentialsFIS = new FileInputStream("s3frontend.properties")
  private val properties = new Properties()
  properties.load(s3credentialsFIS)

  def getPictureUrl(path: String): String = {
    properties.get("imagesBucketDomain") + "/pictures/" + path
  }

  def getMinPictureUrl(path: String): String = {
    properties.get("imagesBucketDomain") + "/pictures_min/" + path
  }
}

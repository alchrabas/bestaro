package bestaro.backend

import java.io._
import java.util.Properties

import bestaro.backend.util.ImageUtil
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import javax.imageio.ImageIO

object S3Client {
  private val s3credentialsFIS = new FileInputStream("s3credentials.properties")
  private val properties = new Properties()
  properties.load(s3credentialsFIS)
  private val awsCredentials = new BasicAWSCredentials(
    properties.getProperty("accessKeyId"),
    properties.getProperty("secretAccessKey"))
  private val bucketName = properties.getProperty("imagesBucketName")
  private val instance = AmazonS3Client
    .builder()
    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
    .build()

  def uploadImage(name: String, content: File): Unit = {
    instance.putObject(bucketName, "pictures/" + name, content)
    val minifiedImageMetadata = new ObjectMetadata()
    minifiedImageMetadata.setContentType("image/png")
    val minifiedIS = minifiedImageBytes(content)
    instance.putObject(bucketName, "pictures_min/" + name,
      minifiedIS, minifiedImageMetadata)
    minifiedIS.close()
  }

  private def minifiedImageBytes(file: File): InputStream = {
    val bufferedImage = ImageIO.read(file)

    val resizedImage = ImageUtil.shrinkToDimension(bufferedImage, 200)
    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(resizedImage, "png", outputStream)
    new ByteArrayInputStream(outputStream.toByteArray)
  }
}

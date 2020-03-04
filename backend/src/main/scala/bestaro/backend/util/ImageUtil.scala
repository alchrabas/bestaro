package bestaro.backend.util

import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.file.{Path, Paths}
import java.security.MessageDigest

import bestaro.backend.types.RecordId
import javax.imageio.ImageIO

object ImageUtil {

  val MAX_ALLOWED_DIMENSION = 1600

  def saveImageForRecord(id: RecordId, serialId: Int, stream: InputStream): Path = {
    val _pictureName = pictureName(id, serialId)
    val _pathToPicture = generatePicturePath(_pictureName)
    val bufferedImage = ImageIO.read(stream)
    val resizedImage = shrinkToDimension(bufferedImage, MAX_ALLOWED_DIMENSION)
    _pathToPicture.toFile.getParentFile.mkdirs()
    ImageIO.write(resizedImage, "png", _pathToPicture.toFile)

    generatePathWithHash(_pictureName)
  }

  def generatePicturePath(pictureName: String): Path = {
    Paths.get("pictures", generatePathWithHash(pictureName).toString)
  }

  def generatePathWithHash(pictureName: String): Path = {
    Paths.get(nameBasedDirectoryHash(pictureName), pictureName)
  }

  def nameBasedDirectoryHash(name: String): String = {
    val messageDigest = MessageDigest.getInstance("MD5")
    val hash = messageDigest.digest(name.getBytes)
    val firstTwoCharsOfHash = f"${hash(0)}%02x"
    firstTwoCharsOfHash
  }

  def pictureName(id: RecordId, serialId: Int): String = {
    id.toString + "_" + serialId + ".png"
  }

  def shrinkToDimension(bufferedImage: BufferedImage, maxAllowedDimension: Int): BufferedImage = {
    val width = bufferedImage.getWidth
    val height = bufferedImage.getHeight()

    if (width < maxAllowedDimension && height < maxAllowedDimension) {
      return bufferedImage
    }

    val rescale = (value: Int) => (value / (Math.max(width, height) * (1.0 / maxAllowedDimension))).intValue()
    ImageResizer.createResizedCopy(
      bufferedImage,
      rescale(width),
      rescale(height),
      preserveAlpha = true)
  }
}

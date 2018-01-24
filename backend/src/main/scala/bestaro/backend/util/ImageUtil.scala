package bestaro.backend.util

import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.file.{Path, Paths}
import javax.imageio.ImageIO

import bestaro.common.types.RecordId
import bestaro.common.util.ImageResizer

object ImageUtil {

  val MAX_ALLOWED_DIMENSION = 1600

  def saveImageForRecord(id: RecordId, serialId: Int, stream: InputStream): Path = {
    val _pictureName = pictureName(id, serialId)
    val _pathToPicture = pathToPicture(_pictureName)
    val bufferedImage = ImageIO.read(stream)
    val resizedImage = shrinkToDimension(bufferedImage, MAX_ALLOWED_DIMENSION)
    ImageIO.write(resizedImage, "png", _pathToPicture.toFile)

    Paths.get(_pictureName)
  }

  def pathToPicture(pictureName: String): Path = {
    Paths.get("pictures", pictureName)
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

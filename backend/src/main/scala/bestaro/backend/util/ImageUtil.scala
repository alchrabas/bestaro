package bestaro.backend.util

import java.io.InputStream
import java.nio.file.{Path, Paths}
import javax.imageio.ImageIO

import bestaro.common.types.RecordId

object ImageUtil {

  def saveImage(id: RecordId, serialId: Int, stream: InputStream): Path = {
    val _pictureName = pictureName(id, serialId)
    val _pathToPicture = pathToPicture(_pictureName)
    val bufferedImage = ImageIO.read(stream)
    ImageIO.write(bufferedImage, "png", _pathToPicture.toFile)

    Paths.get(_pictureName)
  }

  def pathToPicture(pictureName: String): Path = {
    Paths.get("pictures", pictureName)
  }

  def pictureName(id: RecordId, serialId: Int): String = {
    id.toString + "_" + serialId + ".png"
  }
}

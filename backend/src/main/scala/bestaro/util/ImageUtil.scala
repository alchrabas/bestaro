package bestaro.util

import java.io.InputStream
import java.nio.file.{Path, Paths}
import javax.imageio.ImageIO

import bestaro.common.types.RecordId

object ImageUtil {

  def saveImage(id: RecordId, serialId: Int, stream: InputStream): Path = {
    val pictureName = id.toString + "_" + serialId + ".png"
    val pathToPicture = Paths.get("pictures", pictureName)
    val bufferedImage = ImageIO.read(stream)
    ImageIO.write(bufferedImage, "png", pathToPicture.toFile)

    Paths.get(pictureName)
  }
}

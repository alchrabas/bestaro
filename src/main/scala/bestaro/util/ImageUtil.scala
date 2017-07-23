package bestaro.util

import java.io.InputStream
import java.nio.file.{Path, Paths}
import javax.imageio.ImageIO

import bestaro.core.RecordId

object ImageUtil {

  def saveImage(id: RecordId, stream: InputStream): Path = {
    val pathToPicture = Paths.get("pictures", id.toString + ".png")
    val bufferedImage = ImageIO.read(stream)
    ImageIO.write(bufferedImage, "png", pathToPicture.toFile)

    pathToPicture
  }
}

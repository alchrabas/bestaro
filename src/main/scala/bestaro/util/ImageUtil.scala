package bestaro.util

import java.io.InputStream
import java.nio.file.{Path, Paths}
import java.util.UUID
import javax.imageio.ImageIO

object ImageUtil {

  def saveImage(id: UUID, stream: InputStream): Path = {
    val pathToPicture = Paths.get("pictures", id.toString + ".png")
    val bufferedImage = ImageIO.read(stream)
    ImageIO.write(bufferedImage, "png", pathToPicture.toFile)

    pathToPicture
  }
}

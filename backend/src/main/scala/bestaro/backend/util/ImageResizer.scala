package bestaro.backend.util

import java.awt.image.BufferedImage
import java.awt.{AlphaComposite, Image, RenderingHints}

object ImageResizer {

  def createResizedCopy(originalImage: Image, scaledWidth: Int, scaledHeight: Int, preserveAlpha: Boolean): BufferedImage = {
    val imageType = if (preserveAlpha) {
      BufferedImage.TYPE_INT_RGB
    } else {
      BufferedImage.TYPE_INT_ARGB
    }
    val scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType)

    val g = scaledBI.createGraphics
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    if (preserveAlpha) g.setComposite(AlphaComposite.Src)
    g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null)
    g.dispose()
    scaledBI
  }
}

package bestaro.backend.collectors.util

import java.io.InputStream
import java.net.URL

trait HttpDownloader {
  def downloadResource(url: URL): InputStream
}

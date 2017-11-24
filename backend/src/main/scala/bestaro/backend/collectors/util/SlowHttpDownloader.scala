package bestaro.backend.collectors.util

import java.io.InputStream
import java.net.URL

class SlowHttpDownloader extends HttpDownloader {
  override def downloadResource(url: URL): InputStream = {
    Thread.sleep(10 * 1000)
    println("Requesting " + url.toString)
    url.openStream()
  }
}

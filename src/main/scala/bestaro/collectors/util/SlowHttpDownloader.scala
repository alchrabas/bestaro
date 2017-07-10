package bestaro.collectors.util

import java.io.InputStream
import java.net.URL

class SlowHttpDownloader extends HttpDownloader {
  override def downloadResource(url: URL): InputStream = {
    Thread.sleep(10 * 1000)
    url.openStream()
  }
}

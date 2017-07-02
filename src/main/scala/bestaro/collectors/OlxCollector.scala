package bestaro.collectors

import com.gaocegege.scrala.core.common.response.impl.HttpResponse
import com.gaocegege.scrala.core.spider.impl.DefaultSpider

import scala.language.postfixOps

class OlxCollector {
  def collect(): Unit = {
    val testSpider = new TestSpider()
    testSpider.begin()
  }

  class TestSpider extends DefaultSpider {
    def startUrl = List[String]("https://www.olx.pl/zwierzeta/zaginione-i-znalezione/krakow/")

    def parse(response: HttpResponse): Unit = {
      val links = (response getContentParser).select("a")
      for (i <- 0 until links.size()) {
        println(links.get(i).attr("href"))
      }
    }

    def printIt(response: HttpResponse): Unit = {
      println((response getContentParser) title)
    }
  }

}

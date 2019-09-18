package bestaro.backend


import java.nio.charset.StandardCharsets

import bestaro.common.types.Record
import dispatch.Defaults._
import dispatch._
import play.api.libs.json.Json

class DataSupplier {

  def sendRecord(record: Record): Future[String] = {
    val encodedJson = Json.toBytes(Json.toJson(record))
    println("SENDING: " + record)

    uploadBytes(encodedJson)
  }

  private def uploadBytes(encodedJson: Array[Byte]): Future[String] = {
    val Array(username, password) = AppConfig.getProperty("frontendAuthCredentials").split(":")
    val r = url(AppConfig.getProperty("frontendURL"))
      .as_!(username, password)
      .POST
      .setContentType("application/json", StandardCharsets.UTF_8)
      .setBody(encodedJson)

    val future = Http.default(r OK as.String)
    future.onComplete(a => println(a.get))
    future
  }
}

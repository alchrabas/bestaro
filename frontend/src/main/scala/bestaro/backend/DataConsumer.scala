package bestaro.backend

import java.io.FileOutputStream
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import bestaro.common.util.FileIO
import bestaro.common.{NamedPicture, Record, RecordDTO}
import org.eclipse.jetty.http.HttpStatus
import play.api.libs.json.Json

class DataConsumer extends HttpServlet {

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val inputData = new Array[Char](10 * 1000 * 1000)

    val charsRead = req.getReader.read(inputData)
    if (charsRead == -1 || charsRead == inputData.length) {
      resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
      resp.getWriter.println("Invalid input data")
    } else {
      val json = new String(inputData, 0, charsRead)
      println(json)

      val recordDTO = Json.parse(json).as[RecordDTO]
      recordDTO.pictures.foreach(savePicture)

      val record = recordDTO.record
      // will safe it in db or something
      val jsonData = Json.parse(FileIO.readFile("allData.json", "{}")).as[Map[String, Record]]
      val newJsonData = jsonData.updated(record.recordId.toString, record)
      FileIO.saveFile("allData.json", Json.stringify(Json.toJson(newJsonData)))

      resp.setStatus(HttpStatus.OK_200)
      resp.getWriter.println("Thanks!")
    }
  }

  private def savePicture(picture: NamedPicture): Unit = {
    val fileWriter = new FileOutputStream("pictures/" + picture.name)
    fileWriter.write(picture.bytes)
    fileWriter.close()
  }
}

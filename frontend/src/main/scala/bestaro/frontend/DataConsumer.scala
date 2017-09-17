package bestaro.frontend

import java.io.{File, FileOutputStream}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import bestaro.common.types.{NamedPicture, Record, RecordDTO}
import bestaro.common.util.FileIO
import com.google.common.io.CharStreams
import org.eclipse.jetty.http.HttpStatus
import play.api.libs.json.Json

class DataConsumer extends HttpServlet {

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {

    val json = CharStreams.toString(req.getReader)
    req.getReader.close()
    if (json.length == -1) {
      resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
      resp.getWriter.println("Invalid input data")
    } else {

      val recordDTO = Json.parse(json).as[RecordDTO]
      recordDTO.pictures.foreach(saveNamedPicture)

      val record = recordDTO.record
      // will safe it in db or something
      val jsonData = Json.parse(FileIO.readFile("allData.json", "{}")).as[Map[String, Record]]
      val newJsonData = jsonData.updated(record.recordId.toString, record)
      FileIO.saveFile("allData.json", Json.stringify(Json.toJson(newJsonData)))

      resp.setStatus(HttpStatus.OK_200)
      resp.getWriter.println("Thanks!")
    }
  }

  private def saveNamedPicture(picture: NamedPicture): Unit = {
    saveImage(picture.bytes, new File("pictures/" + picture.name))
    saveImage(picture.minifiedBytes, new File("pictures_min/" + picture.name))
  }

  private def saveImage(bytes: Array[Byte], picturePath: File): Unit = {
    val fileWriter = new FileOutputStream(picturePath)
    fileWriter.write(bytes)
    fileWriter.close()
  }
}

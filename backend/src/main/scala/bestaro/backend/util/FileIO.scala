package bestaro.backend.util

import java.io.{File, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object FileIO {

  def readFile(inputFile: String, whenEmpty: String): String = {
    if (!Paths.get(inputFile).toFile.exists()) {
      new File(inputFile).createNewFile()
    }
    val encoded = Files.readAllBytes(Paths.get(inputFile))
    if (encoded.isEmpty) {
      new String(whenEmpty)
    } else {
      new String(encoded, StandardCharsets.UTF_8)
    }
  }

  def saveFile(outputFile: String, json: String): Unit = {
    val jsonWriter = new FileWriter(outputFile)
    jsonWriter.write(json)
    jsonWriter.close()
  }
}

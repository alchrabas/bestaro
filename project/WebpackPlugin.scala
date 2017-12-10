package com.typesafe.sbt.webpack

import sbt._
import com.typesafe.sbt.web.{PathMapping, SbtWeb}
import com.typesafe.sbt.web.js.JS
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys._
import sbt.Task

object Import {

  // For development only, could execute before "run"
  // eg: run in Compile <<= (run in Compile) dependsOn webpack
  val webpack = TaskKey[Seq[File]]("webpack", "Invoke the webpack module bundler in dev mode.")
  // For production
  val webpackStage = TaskKey[Pipeline.Stage]("webpack-stage", "Invoke the webpack module bundler.")

  object WebpackKeys {
    val command = SettingKey[String]("webpack-command", "The webpack command in dev mode.")
    val outputPath = SettingKey[String]("webpack-output-path", "Path to the generated asset file in dev mode.")

    val stageCommand = SettingKey[String]("webpack-stage-command", "The webpack command.")
    val stageOutputPath = SettingKey[String]("webpack-stage-output-path", "Path to the generated asset file.")
  }

}

object SbtWebpack extends AutoPlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport._
  import WebpackKeys._

  override def projectSettings = Seq(
    command := "npm run build",
    outputPath := "web/main/public",
    webpack := webpackDevelopTask.value,

    stageCommand := "npm run build release",
    stageOutputPath := "webpack",
    webpackStage := webpackStageTask.value
  )

  def webpackDevelopTask: Def.Initialize[Task[Seq[File]]] = Def.task {
    exec(command.value)
    Seq()
  }

  def webpackStageTask: Def.Initialize[Task[Pipeline.Stage]] = Def.task { mappings =>
    exec(stageCommand.value)

    val outputDir = target.value / stageOutputPath.value
    val outputFiles = outputDir ** "*.*"
    val newMappings = outputFiles pair relativeTo(outputDir)

    // Replace existed ones
    val newNames = newMappings map (_._2)
    val (existed, other) = mappings partition (newNames contains _._2)

    newMappings ++ other
  }

  // Execute NPM command
  def exec(cmd: String) = {
    try {
      val rc = Process(cmd, file(".")).!
      if (rc != 0) {
        sys.error(s"NPM generated non-zero return code: $rc")
      }
    } catch {
      case e: java.io.IOException => {
        // For windows
        val rc = Process("cmd /c " + cmd, file(".")).!
        if (rc != 0) {
          sys.error(s"NPM generated non-zero return code: $rc")
        }
      }
    }
  }

}

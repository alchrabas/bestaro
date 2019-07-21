lazy val root = (project in file("."))
  .aggregate(backend, frontend)

lazy val commonSettings = Seq(
  organization := "bestaro",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.2",
  resolvers += "jitpack" at "https://jitpack.io"
)


import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.slidingautonomy.sbt.filter.Import._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.sbt.Play.autoImport._
import sbt._
import sbt.Keys._


lazy val backend = project
  .dependsOn(common)
  .settings(
    commonSettings,
    name := "bestaro-backend",
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.10.3",
      "org.facebook4j" % "facebook4j-core" % "2.4.13",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.carrot2" % "morfologik-polish" % "2.1.3",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test,
      "com.github.tototoshi" %% "scala-csv" % "1.3.4",
      "com.google.maps" % "google-maps-services" % "0.2.1",
      "com.typesafe.play" %% "play-json" % "2.6.3",
      "com.google.guava" % "guava" % "23.0",
      "cc.mallet" % "mallet" % "2.0.8",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "org.xerial" % "sqlite-jdbc" % "3.20.1",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.beachape" %% "enumeratum-play-json" % "1.5.12-2.6.0-M7",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "net.databinder.dispatch" %% "dispatch-core" % "0.13.2"
    )
  )

lazy val frontend = project
  .dependsOn(common)
  .enablePlugins(PlayScala, SbtWeb)
  .settings(
    commonSettings,
    name := "bestaro-frontend",
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "23.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.beachape" %% "enumeratum-play-json" % "1.5.12-2.6.0-M7",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "com.typesafe.play" %% "play-slick" % "3.0.1",
      "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
      "org.julienrf" %% "play-jsmessages" % "3.0.0",
      "org.postgresql" % "postgresql" % "42.1.4",
      "com.github.tminglei" %% "slick-pg" % "0.15.4",
      "com.github.tminglei" %% "slick-pg_jts" % "0.15.4",
      "com.github.tminglei" %% "slick-pg_play-json" % "0.15.4",
      guice,
      ehcache, // play cache external module
      ws
    ),
    includeFilter in filter := "*.scss" || "*.jsx",
    pipelineStages := Seq(filter, digest, gzip),
    WebKeys.exportedMappings in Assets := Seq(),
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    PlayKeys.devSettings := Seq("play.server.http.port" -> "8888")
  )

lazy val common = project
  .settings(
    commonSettings,
    name := "bestaro-common",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.6.3",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.beachape" %% "enumeratum-play-json" % "1.5.12-2.6.0-M7",
      "com.github.alchrabas" %% "bestaro-locator" % "0.9.1"
    )
  )

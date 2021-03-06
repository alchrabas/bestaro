import sbt._
import sbt.Keys._


lazy val root = (project in file("."))
  .settings(
    organization := "bestaro",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.2",
    resolvers += "jitpack" at "https://jitpack.io",
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
      "net.databinder.dispatch" %% "dispatch-core" % "0.13.2",
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.630",
      "com.github.alchrabas" %% "bestaro-locator" % "0.9.1"
    )
  )

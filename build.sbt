
lazy val root = (project in file("."))
  .aggregate(backend, frontend)

lazy val commonSettings = Seq(
  organization := "pl.alchrabas",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.0",
  resolvers += "jitpack" at "https://jitpack.io"
)

lazy val backend = project
  .settings(
    commonSettings,
    name := "bestaro-backend",
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.10.3",
      "org.facebook4j" % "facebook4j-core" % "2.4.9",
      "com.lihaoyi" %% "upickle" % "0.4.3",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.carrot2" % "morfologik-polish" % "2.1.3",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test,
      "com.github.tototoshi" %% "scala-csv" % "1.3.4",
      "fr.dudie" % "nominatim-api" % "3.3",
      "com.google.maps" % "google-maps-services" % "0.2.1",
      "org.slf4j" % "slf4j-nop" % "1.7.25"
    )
  )

lazy val frontend = project
  .settings(
    commonSettings,
    name := "bestaro-frontend"
  )

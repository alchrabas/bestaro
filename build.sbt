lazy val root = (project in file("."))
  .aggregate(backend, frontend)

lazy val commonSettings = Seq(
  organization := "bestaro",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.2",
  resolvers += "jitpack" at "https://jitpack.io"
)

lazy val locator = project
  .settings(
    commonSettings,
    name := "bestaro-locator",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.6.3",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.beachape" %% "enumeratum-play-json" % "1.5.12-2.6.0-M7",
      "org.carrot2" % "morfologik-polish" % "2.1.3",
      "com.google.guava" % "guava" % "23.0",
      "com.google.maps" % "google-maps-services" % "0.2.1",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "com.github.tototoshi" %% "scala-csv" % "1.3.4",
      "org.xerial" % "sqlite-jdbc" % "3.20.1",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test
    )
  )

lazy val backend = project
  .dependsOn(common)
  .settings(
    commonSettings,
    name := "bestaro-backend",
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.10.3",
      "org.facebook4j" % "facebook4j-core" % "2.4.9",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.carrot2" % "morfologik-polish" % "2.1.3",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test,
      "com.github.tototoshi" %% "scala-csv" % "1.3.4",
      "com.google.maps" % "google-maps-services" % "0.2.1",
      "org.slf4j" % "slf4j-nop" % "1.7.25",
      "com.typesafe.play" %% "play-json" % "2.6.3",
      "com.google.guava" % "guava" % "23.0",
      "cc.mallet" % "mallet" % "2.0.8",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "org.xerial" % "sqlite-jdbc" % "3.20.1",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.beachape" %% "enumeratum-play-json" % "1.5.12-2.6.0-M7"
    )
  )

lazy val frontend = project
  .dependsOn(common)
  .enablePlugins(PlayScala)
  .settings(
    commonSettings,
    name := "bestaro-frontend",
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "23.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.webjars" % "leaflet" % "1.2.0",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.beachape" %% "enumeratum-play-json" % "1.5.12-2.6.0-M7",
      guice
    ),
    PlayKeys.devSettings := Seq("play.server.http.port" -> "8888")
  )

lazy val common = project
  .dependsOn(locator)
  .settings(
    commonSettings,
    name := "bestaro-common",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.6.3",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.beachape" %% "enumeratum-play-json" % "1.5.12-2.6.0-M7"
    )
  )

name := "bestaro"

version := "1.0"

scalaVersion := "2.11.0"

resolvers +=
  "jitpack" at "https://jitpack.io"

libraryDependencies +=
  "org.jsoup" % "jsoup" % "1.10.3"

libraryDependencies +=
  "org.facebook4j" % "facebook4j-core" % "2.4.9"

libraryDependencies +=
  "com.lihaoyi" %% "upickle" % "0.4.3"

libraryDependencies +=
  "org.scalactic" %% "scalactic" % "3.0.1"

libraryDependencies +=
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies +=
  "org.carrot2" % "morfologik-polish" % "2.1.3"

libraryDependencies +=
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test

libraryDependencies +=
  "com.github.tototoshi" %% "scala-csv" % "1.3.4"

libraryDependencies +=
  "fr.dudie" % "nominatim-api" % "3.3"

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
  "org.carrot2" % "morfologik-stemming" % "1.2.2"

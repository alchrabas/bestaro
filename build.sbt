import com.lihaoyi.workbench.Plugin._
import UdashBuild._
import Dependencies._

name := "bestaro"

version in ThisBuild := "0.1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.2"
organization in ThisBuild := "bestaro"
crossPaths in ThisBuild := false
scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:existentials",
  "-language:dynamics",
  "-Xfuture",
  "-Xfatal-warnings",
  "-Xlint:-unused,_"
)

def crossLibs(configuration: Configuration) =
  libraryDependencies ++= crossDeps.value.map(_ % configuration)

lazy val bestaro = project.in(file("."))
  .aggregate(frontend_commonJS, frontend_commonJVM, frontend_client, frontend_server, backend)
  .dependsOn(frontend_server)
  .settings(
    publishArtifact := false,
    mainClass in Compile := Some("bestaro.Launcher")
  )


lazy val backend = project.in(file("backend"))
  .settings(
    name := "backend",
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
      "com.typesafe.play" %% "play-json" % "2.6.3"
    )
  )



lazy val frontend_common = crossProject.crossType(CrossType.Pure).in(file("frontend_common"))
  .settings(
    crossLibs(Provided)
  )

lazy val frontend_commonJVM = frontend_common.jvm
lazy val frontend_commonJS = frontend_common.js

lazy val frontend_server = project.in(file("frontend_server"))
  .dependsOn(frontend_commonJVM)
  .settings(
    libraryDependencies ++= backendDeps.value,
    crossLibs(Compile),

    compile := (compile in Compile).value,
    (compile in Compile) := (compile in Compile).dependsOn(copyStatics).value,
    copyStatics := IO.copyDirectory((crossTarget in frontend_client).value / StaticFilesDir, (target in Compile).value / StaticFilesDir),
    copyStatics := copyStatics.dependsOn(compileStatics in frontend_client).value,

    mappings in(Compile, packageBin) ++= {
      copyStatics.value
      ((target in Compile).value / StaticFilesDir).***.get map { file =>
        file -> file.getAbsolutePath.stripPrefix((target in Compile).value.getAbsolutePath)
      }
    },

    watchSources ++= (sourceDirectory in frontend_client).value.***.get
  )

lazy val frontend_client = project.in(file("frontend_client")).enablePlugins(ScalaJSPlugin)
  .dependsOn(frontend_commonJS)
  .settings(
    libraryDependencies ++= frontendDeps.value,
    crossLibs(Compile),
    jsDependencies ++= frontendJSDeps.value,
    scalaJSUseMainModuleInitializer in Compile := true,

    compile := (compile in Compile).dependsOn(compileStatics).value,
    compileStatics := {
      IO.copyDirectory(sourceDirectory.value / "main/assets/fonts", crossTarget.value / StaticFilesDir / WebContent / "assets/fonts")
      IO.copyDirectory(sourceDirectory.value / "main/assets/images", crossTarget.value / StaticFilesDir / WebContent / "assets/images")
      val statics = compileStaticsForRelease.value
      (crossTarget.value / StaticFilesDir).***.get
    },

    artifactPath in(Compile, fastOptJS) :=
      (crossTarget in(Compile, fastOptJS)).value / StaticFilesDir / WebContent / "scripts" / "frontend-impl-fast.js",
    artifactPath in(Compile, fullOptJS) :=
      (crossTarget in(Compile, fullOptJS)).value / StaticFilesDir / WebContent / "scripts" / "frontend-impl.js",
    artifactPath in(Compile, packageJSDependencies) :=
      (crossTarget in(Compile, packageJSDependencies)).value / StaticFilesDir / WebContent / "scripts" / "frontend-deps-fast.js",
    artifactPath in(Compile, packageMinifiedJSDependencies) :=
      (crossTarget in(Compile, packageMinifiedJSDependencies)).value / StaticFilesDir / WebContent / "scripts" / "frontend-deps.js"
  ).settings(workbenchSettings: _*)
  .settings(
    bootSnippet := "bestaro.Init().main();",
    updatedJS := {
      var files: List[String] = Nil
      ((crossTarget in Compile).value / StaticFilesDir ** "*.js").get.foreach {
        (x: File) =>
          streams.value.log.info("workbench: Checking " + x.getName)
          FileFunction.cached(streams.value.cacheDirectory / x.getName, FilesInfo.lastModified, FilesInfo.lastModified) {
            (f: Set[File]) =>
              val fsPath = f.head.getAbsolutePath.drop(new File("").getAbsolutePath.length)
              files = "http://localhost:12345" + fsPath :: files
              f
          }(Set(x))
      }
      files
    },
    //// use either refreshBrowsers OR updateBrowsers
    // refreshBrowsers := (refreshBrowsers triggeredBy (compileStatics in Compile)).value
    updateBrowsers := (updateBrowsers triggeredBy (compileStatics in Compile)).value
  )


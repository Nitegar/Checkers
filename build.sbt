
val scala3Version = "3.3.7"

lazy val root = project
  .in(file("."))
  .settings(
    name := "project",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
      "org.mockito" % "mockito-core" % "5.21.0" % Test,
      "net.codingwell" %% "scala-guice" % "7.0.0",
      "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
      "com.lihaoyi" %% "upickle" % "4.4.2"
    ),
    libraryDependencies ++= {
      val osName = System.getProperty("os.name").toLowerCase
      val osArch = System.getProperty("os.arch").toLowerCase

      val classifier = osName match {
        case n if n.contains("win") => "win"
        case n if n.contains("mac") =>
          if (osArch.contains("aarch64") || osArch.contains("arm64")) "mac-aarch64"
          else "mac"
        case n if n.contains("nux") => "linux"
        case _ => "linux"
      }

      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "22.0.2" classifier classifier)
    },
    javaOptions ++= Seq(
      "--module-path",
      (Compile / dependencyClasspath).value.files
        .filter(_.getName.contains("javafx"))
        .map(_.getAbsolutePath)
        .mkString(java.io.File.pathSeparator),
      "--add-modules",
      "javafx.controls,javafx.fxml,javafx.graphics"
    ),
    fork := true,
    Test / fork := true,
    run / connectInput := true,
    coverageExcludedFiles +=
      """.*/de/htwg/view/gui/.*|.*/de/htwg/file/.*|.*/de/htwg/CheckersModule""",
    Compile / mainClass := Some("de.htwg.CheckersApp")
  )


//ThisBuild / organization := "de.htwg"
//ThisBuild / name         := "checkers"
//ThisBuild / version      := "0.1.0-SNAPSHOT"
//ThisBuild / scalaVersion := "3.3.7"
//
//Compile / mainClass := Some("de.htwg.CheckersApp.main")
//
//enablePlugins(JavaAppPackaging)
//
//libraryDependencies ++= Seq(
//  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
//  "org.scalafx" %% "scalafx" % "21.0.0-R32",
//  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
//  "org.mockito" % "mockito-core" % "5.21.0" % Test,
//  "net.codingwell" %% "scala-guice" % "7.0.0",
//  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
//  "com.lihaoyi" %% "upickle" % "4.4.2"
//)

//libraryDependencies ++= {
//  val osName = System.getProperty("os.name").toLowerCase
//  val osArch = System.getProperty("os.arch").toLowerCase
//
//  val classifier = osName match {
//    case n if n.contains("win") => "win"
//    case n if n.contains("mac") =>
//      if (osArch.contains("aarch64") || osArch.contains("arm64")) "mac-aarch64"
//      else "mac"
//    case n if n.contains("nux") => "linux"
//    case _ => "linux"
//  }
//
//  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
//    .map(m => "org.openjfx" % s"javafx-$m" % "22.0.2" classifier classifier)
//}
//
//javaOptions ++= Seq(
//  "--module-path",
//  (Compile / dependencyClasspath).value.files
//    .filter(_.getName.contains("javafx"))
//    .map(_.getAbsolutePath)
//    .mkString(java.io.File.pathSeparator),
//  "--add-modules",
//  "javafx.controls,javafx.fxml,javafx.graphics"
//)
//
//fork := true
//Test / fork := true
//
//run / connectInput := true
//
//coverageEnabled := false
//coverageExcludedFiles +=
//  """.*/de/htwg/view/gui/.*|.*/de/htwg/file/.*|.*/de/htwg/CheckersModule"""
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
  "org.mockito" % "mockito-core" % "5.21.0" % Test,
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
  "com.lihaoyi" %% "upickle" % "4.4.2",
)

libraryDependencies ++= {
  val osName = System.getProperty("os.name").toLowerCase
  val osArch = System.getProperty("os.arch").toLowerCase

  val classifier = osName match {
    case n if n.contains("win") => "win"
    case n if n.contains("mac") =>
      // "mac-aarch64" is required for M1/M2/M3 chips
      if (osArch.contains("aarch64") || osArch.contains("arm64")) "mac-aarch64" else "mac"
    case n if n.contains("nux") => "linux"
    case _ => "linux" // Default fallback
  }

  // Consistent versioning (21.0.2 is very stable for MacOS)
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map(m => "org.openjfx" % s"javafx-$m" % "21.0.2" classifier classifier)
}

javaOptions ++= Seq(
  "--module-path",
  (Compile / dependencyClasspath).value.files.filter(_.getName.contains("javafx")).map(_.getAbsolutePath).mkString(java.io.File.pathSeparator),
  "--add-modules",
  "javafx.controls,javafx.fxml,javafx.graphics" // Add others if needed
)

// CRITICAL FOR MAC: This allows JavaFX to hook into the MacOS AppKit thread correctly
fork := true
Test / fork := true

coverageEnabled := true
run / connectInput := true

coverageExcludedFiles += ".*\\/de\\/htwg\\/view\\/gui\\/.*|.*\\/de\\/htwg\\/file\\/.*|.*\\/de\\/htwg\\/CheckersModule"
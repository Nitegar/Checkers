ThisBuild / version := "0.1.0-SNAPSHOT"
// Changed to the current stable LTS version (3.7.4 does not exist yet)
ThisBuild / scalaVersion := "3.3.4"

libraryDependencies ++= Seq(
  // The missing ScalaFX wrapper
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
  "org.mockito" % "mockito-core" % "5.11.0" % Test,
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
  "com.lihaoyi" %% "upickle" % "3.1.3"
)

// Simplified JavaFX dependency management
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

// CRITICAL FOR MAC: This allows JavaFX to hook into the MacOS AppKit thread correctly
fork := true
run / connectInput := true

// This helps fix the "Unsupported JavaFX configuration" warning
javaOptions ++= Seq(
  "--add-modules", "javafx.controls,javafx.graphics",
  "--add-opens", "javafx.graphics/com.sun.glass.ui=ALL-UNNAMED"
)
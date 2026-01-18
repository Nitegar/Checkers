ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  // Update ScalaFX to a version that supports newer JavaFX
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
  "org.mockito" % "mockito-core" % "5.11.0" % Test,
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
  "com.lihaoyi" %% "upickle" % "3.1.3",
)

libraryDependencies ++= {
  val osName = System.getProperty("os.name").toLowerCase
  val osArch = System.getProperty("os.arch").toLowerCase

  val classifier = osName match {
    case n if n.contains("win") => "win"
    case n if n.contains("mac") =>
      // This is the critical part for modern Macs
      if (osArch.contains("aarch64") || osArch.contains("arm64")) "mac-aarch64" else "mac"
    case n if n.contains("nux") => "linux"
    case _ => throw new Exception("Unknown platform!")
  }

  // Use JavaFX 21 or 23 (more stable than 16 for Java 21+ environments)
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map(m => "org.openjfx" % s"javafx-$m" % "21" classifier classifier)
}
// This helps bridge the gap between Scala's flat classpath and JavaFX's modules
fork := true
run / connectInput := true
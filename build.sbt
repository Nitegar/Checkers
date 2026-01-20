ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
  "org.mockito" % "mockito-core" % "5.21.0" % Test,
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
  "com.lihaoyi" %% "upickle" % "4.4.2",
)

coverageExcludedFiles += ".*\\/de\\/htwg\\/view\\/gui\\/.*|.*\\/de\\/htwg\\/file\\/.*|.*\\/de\\/htwg\\/CheckersModule"
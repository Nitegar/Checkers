ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
libraryDependencies ++= Seq(
  "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
  "org.mockito" % "mockito-core" % "5.11.0" % Test
)

// build.sbt
inThisBuild(List(
  organization := "de.htwg",
  homepage := Some(url("https://github.com/Nitegar/Checkers")),
  // Alternatively License.Apache2 see https://github.com/sbt/librarymanagement/blob/develop/core/src/main/scala/sbt/librarymanagement/License.scala
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "hans",
      "Hans Peter",
      "hans@gmail.com",
      url("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    )
  )
))
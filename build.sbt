name := """ticket-overlords"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  specs2 % Test,
  jdbc,
  cache,
  ws,
  "org.webjars" % "jquery" % "2.1.3"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
name := """ticket-overlords"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  specs2 % Test,
  cache,
  ws,
  "org.webjars" % "jquery" % "2.1.4",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.0",
  "com.h2database" % "h2" % "1.4.187",
  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "org.webjars" % "requirejs" % "2.1.18",
  "org.webjars" % "react" % "0.13.3"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

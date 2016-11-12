name := "PA"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.scala-lang" % "scala-actors" % "2.11.8"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.11"

libraryDependencies += "com.google.guava" % "guava" % "r05"

libraryDependencies += "commons-io" % "commons-io" % "2.4"
libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "org.scalaz" %% "scalaz-effect" % "7.1.0",
  "org.scalaz" %% "scalaz-typelevel" % "7.1.0",
  "org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.0" % "test"
)

scalacOptions += "-feature"

initialCommands in console := "import scalaz._, Scalaz._"

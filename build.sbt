name := "test-chatbot"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalaj" % "scalaj-http_2.12" % "2.3.0",
  "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)
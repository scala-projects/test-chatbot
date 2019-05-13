name := "scala_tests"

version := "0.1"

scalaVersion := "2.12.8"

val akkaV = "2.5.22"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.5" % "test",
                            "org.scalaj" % "scalaj-http_2.12" % "2.3.0",
                            "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.7.2",
                            "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
                            "com.typesafe.akka" %% "akka-actor" % akkaV,
                            //                            "com.typesafe.akka" %% "akka-persistence" % akkaV,
                            "com.typesafe.akka" %% "akka-remote" % akkaV,
                            "com.typesafe.akka" %% "akka-testkit" % akkaV % Test,
                            "com.github.romix.akka" % "akka-kryo-serialization_2.12" % "0.5.2"
                          )

import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._

name := "shocktrade-server"

organization := "com.ldaniels528"

version := "0.4"

scalaVersion := "2.11.6"

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

val akakVersion = "2.3.9"
val avroVersion = "1.7.7"
val bijectionVersion = "0.7.2"
val sprayVersion = "1.3.2"
val slf4jVersion = "1.7.10"

assemblySettings

mainClass in assembly := Some("com.ldaniels528.broadway.BroadwayServer")

test in assembly := {}

jarName in assembly := "shocktrade-server_" + version.value + ".bin.jar"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case PathList("stax", "stax-api", xs@_*) => MergeStrategy.first
  case PathList("log4j-over-slf4j", xs@_*) => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
}

Seq(sbtavro.SbtAvro.avroSettings: _*)

(version in avroConfig) := avroVersion

(stringType in avroConfig) := "String"

//(javaSource in avroConfig) := file("target/java7/src_managed")

(sourceDirectory in avroConfig) := file("src/main/resources/avro")

// Shocktrade Dependencies
libraryDependencies ++= Seq(
  //  "com.ldaniels528" %% "broadway" % "0.9.0",
  "com.ldaniels528" %% "commons-helpers" % "0.1.0",
  "com.ldaniels528" %% "shocktrade-services" % "0.3.0",
  "com.ldaniels528" %% "tabular" % "0.1.0",
  "com.ldaniels528" %% "trifecta" % "0.19.0"
    exclude("org.mongodb", "casbah-commons")
    exclude("org.mongodb", "casbah-core")
)

// Avro Dependencies
libraryDependencies ++= Seq(
  "com.twitter" %% "bijection-core" % bijectionVersion,
  "com.twitter" %% "bijection-avro" % bijectionVersion,
  "org.apache.avro" % "avro" % avroVersion
)

// Kafka/Zookeeper Dependencies
libraryDependencies ++= Seq(
  "org.apache.curator" % "curator-framework" % "2.7.1",
  "org.apache.kafka" %% "kafka" % "0.8.2.0"
    exclude("org.apache.zookeeper", "zookeeper")
    exclude("org.slf4j", "log4j-over-slf4j"),
  "org.apache.zookeeper" % "zookeeper" % "3.4.6"
)

// Spray Dependencies
libraryDependencies ++= Seq(
  "com.wandoulabs.akka" %% "spray-websocket" % "0.1.4",
  "io.spray" %% "spray-can" % sprayVersion,
  "io.spray" %% "spray-client" % sprayVersion,
  "io.spray" %% "spray-io" % sprayVersion,
  "io.spray" %% "spray-json" % "1.3.1",
  "io.spray" %% "spray-routing" % sprayVersion,
  "io.spray" %% "spray-testkit" % sprayVersion % "test"
)

// SQL/NOSQL Dependencies
libraryDependencies ++= Seq(
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.5",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "org.mongodb" %% "casbah-commons" % "2.8.0",
  "org.mongodb" %% "casbah-core" % "2.8.0",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7"
)

// General Dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.9",
  "net.liftweb" %% "lift-json" % "3.0-M3",
  "org.apache.httpcomponents" % "httpcore" % "4.3.2",
  "org.apache.httpcomponents" % "httpmime" % "4.3.2",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.10",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
)

// Testing Dependencies
libraryDependencies ++= Seq(
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.scalatest" %% "scalatest" % "2.2.3" % "test"
)

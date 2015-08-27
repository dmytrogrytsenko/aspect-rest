name := "aspect"
 
version := "1.0-SNAPSHOT"
 
organization := "com.delirium"
 
scalaVersion := "2.11.6"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray Repository" at "http://repo.spray.io"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.9",
  "com.typesafe" % "config" % "1.2.1",
  "org.joda" % "joda-convert" % "1.6",
  "joda-time" % "joda-time" % "2.6",
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.2",
  "io.spray" %% "spray-json" % "1.3.1",
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
  "org.reactivemongo" %% "reactivemongo-extensions-bson" % "0.10.5.0.0.akka23",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.twitter4j" % "twitter4j-core" % "4.0.2",
  "org.twitter4j" % "twitter4j-async" % "4.0.2",
  "org.twitter4j" % "twitter4j-stream" % "4.0.2",
  "org.twitter4j" % "twitter4j-media-support" % "4.0.2",
  "org.scalatest" %% "scalatest" % "2.2.0",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
)

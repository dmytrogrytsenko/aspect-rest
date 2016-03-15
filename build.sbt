name := "aspect"
 
version := "1.0-SNAPSHOT"
 
organization := "com.delirium"
 
scalaVersion := "2.11.7"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray Repository" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.6",
    "com.typesafe" % "config" % "1.3.0",
    "org.joda" % "joda-convert" % "1.8.1",
    "joda-time" % "joda-time" % "2.9.1",
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
    "org.scalatest" %% "scalatest" % "2.2.6"
  )
}

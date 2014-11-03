import com.github.play2war.plugin._

name := """reactive-stocks"""

version := "1.0-SNAPSHOT"

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "3.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

//resolvers += Resolver.mavenLocal

//resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  filters,
  anorm,
  jdbc,
  ws, // Play's web services module
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test",
  "org.springframework" % "spring-jdbc" % "4.1.1.RELEASE",
  "org.springframework" % "spring-context" % "4.1.1.RELEASE",
  "commons-dbcp" % "commons-dbcp" % "1.4",
  "com.github.nscala-time" %% "nscala-time" % "1.4.0",
  "ws.securesocial" % "securesocial_2.11" % "3.0-M1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "net.databinder.dispatch" % "dispatch-json4s-native_2.11" % "0.11.2"
)


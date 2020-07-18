import sbtassembly.MergeStrategy

name := "PrimeScalaFriend"
version := "0.1"
organization := "manning"
scalacOptions += "-feature"
scalaVersion := "2.12.8"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
scalacOptions += "-target:jvm-1.8"

lazy val akkaVersion = "2.5.21"
lazy val quillVersion = "2.6.0"
lazy val logBackVersion = "1.2.3"
lazy val macwireVersion = "2.3.0"
lazy val catsVersion       = "1.6.0"
lazy val catsEffectVersion = "1.1.0"
lazy val fs2Version        = "1.0.3"
lazy val commonSettings = Seq(
  test in assembly := {}
)

lazy val app = (project in file("app")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("ru.AppStart"),
  )
assemblyMergeStrategy in assembly := {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.first
    }
  case _ => MergeStrategy.first}
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback"     %  "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "net.logstash.logback" % "logstash-logback-encoder" % "5.3",
)

libraryDependencies ++= Seq(
  "io.getquill" %% "quill-async-mysql" % quillVersion,
  "io.getquill" %% "quill-async" % quillVersion,
  "io.getquill" %% "quill-sql" % quillVersion,
  "io.getquill" %% "quill-core" % quillVersion,
  "io.getquill" %% "quill" % quillVersion,
  "io.getquill" %% "quill-orientdb" % quillVersion,
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "twirl-api" % "1.4.0",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.1",
  "com.typesafe.play" %% "play-ws" % "2.7.0",
  "com.typesafe.play" %% "play-json" % "2.7.1"
)

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "org.scalamock" %% "scalamock" % "4.1.0" % Test,
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version
)




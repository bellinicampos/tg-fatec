name := "tg-fatec"
version := "1.0.0"
scalaVersion := "2.12.18"

val flinkVersion = "1.14.6"
val logbackVersion = "1.5.6"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-clients" % flinkVersion,
  "org.apache.flink" %% "flink-scala" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka" % flinkVersion
)

val logging = Seq(
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

val handlingJson = Seq(
  "com.typesafe.play" %% "play-json" % "2.10.6"
)

libraryDependencies ++= flinkDependencies ++ logging ++ handlingJson
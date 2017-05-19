
val Dependencies = Seq(
  "io.monix" %% "monix" % "2.3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.5.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.1"
)

lazy val benchmarks = (project in file("benchmarks"))
  .settings(
    inThisBuild(
      List(
        organization := "com.example",
        scalaVersion := "2.12.1",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    name := "Monix Blog benchmarks",
    libraryDependencies ++= Dependencies
  )
  .enablePlugins(JmhPlugin)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(
      List(
        organization := "com.example",
        scalaVersion := "2.12.1",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    name := "Monix Blog Examples",
    libraryDependencies ++= Dependencies
  )

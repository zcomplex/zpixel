val scala3Version = "3.7.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Z-PiXel",
    version := "0.1.0-EXPERIMENTAL",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "1.2.1" % Test
  )

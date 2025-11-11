import sbt.Compile
import sbt.Def

import java.text.SimpleDateFormat

val scala3Version = "3.7.3"

val major = 0
val minor = 1
val patch = 2

val ver = s"$major.$minor.$patch"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Z-PiXel",
    version := ver,

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.2.1" % Test,

      // GPU, PyTorch library
      "ai.djl"         % "api"                  % "0.35.0",
      "ai.djl.pytorch" % "pytorch-engine"       % "0.35.0",
      "ai.djl.pytorch" % "pytorch-native-cu121" % "2.3.1" classifier "linux-x86_64",

      "org.slf4j" % "slf4j-simple" % "2.0.17"
    ),

    Compile / sourceGenerators += Def.task {
      import java.util.Date
      val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
      val file = (Compile / sourceManaged).value / "/zpixel/generated/BuildInfo.scala"
      val contents = IO.read(new File("build-info.scala.template"))
        .replace("{{NAME}}", name.value)
        .replace("{{VERSION}}", version.value)
        .replace("{{BUILT_AT}}", formatter.format(new Date))
      IO.write(file, contents)
      Seq(file)
    }.taskValue
  )
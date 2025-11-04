import sbt.Compile
import sbt.Def

import java.text.SimpleDateFormat

val scala3Version = "3.7.3"

val major = 0
val minor = 1
val patch = 1

val ver = s"$major.$minor.$patch"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Z-PiXel",
    version := ver,

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "1.2.1" % Test,

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

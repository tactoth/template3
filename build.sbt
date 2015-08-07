
name := """template3"""

version := "1.0"

mainClass in (Compile, run) := Some("Main")

lazy val root = project in file(".")

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.beust" % "jcommander" % "1.30",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "com.novocode" % "junit-interface" % "0.11" % "test"

)


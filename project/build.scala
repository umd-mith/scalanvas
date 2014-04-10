import sbt._
import Keys._

object Scalanvas extends Build {
  lazy val bananaUtils: Project = Project(
    id = "banana-utils",
    base = file("banana"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.apache.jena" % "jena-arq" % "2.11.1",
        "com.github.jsonld-java" % "jsonld-java-jena" % "0.4-SNAPSHOT"
      )
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git"), "banana-jena")
  )

  lazy val core: Project = Project(
    id = "scalanvas-core",
    base = file("core"),
    dependencies = Seq(bananaUtils),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.scalesxml" % "scales-xml_2.10" % "0.6.0-M1"
      )
    )
  )

  lazy val extensions: Project = Project(
    id = "scalanvas-extensions",
    base = file("extensions"),
    dependencies = Seq(core),
    settings = commonSettings
  )

  lazy val teiUtils: Project = Project(
    id = "tei-utils",
    base = file("tei"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
      )
    )
  )

  lazy val sga: Project = Project(
    id = "scalanvas-sga",
    base = file("sga"),
    dependencies = Seq(extensions, teiUtils),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "net.sf.opencsv" % "opencsv" % "2.3"
      )
    )
  )

  lazy val wwa: Project = Project(
    id = "scalanvas-wwa",
    base = file("wwa"),
    dependencies = Seq(extensions, teiUtils),
    settings = commonSettings
  )

  lazy val root: Project = Project(
    id = "scalanvas",
    base = file("."),
    settings = commonSettings
  ).aggregate(sga, wwa)

  def commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.umd.mith",
    version := "0.0.0-SNAPSHOT",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    scalaVersion := "2.10.3",
    scalacOptions := Seq(
      "-feature",
      "-language:implicitConversions",
      "-deprecation",
      "-unchecked"
    ),
    libraryDependencies <++= scalaVersion(sv => Seq(
      "com.typesafe" % "config" % "1.2.0",
      "org.slf4j" % "slf4j-simple" % "1.7.6"
    ))
  )
}

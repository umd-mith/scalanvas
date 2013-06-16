import sbt._
import Keys._

object Scalanvas extends Build {
  lazy val core: Project = Project(
    id = "scalanvas-core",
    base = file("core"),
    dependencies = Seq(prefixes),
    settings = commonSettings ++ Seq(
      libraryDependencies <++= scalaVersion { sv => Seq(
        "org.slf4j" % "slf4j-simple" % "1.6.4",
        "com.github.jsonld-java" % "jsonld-java-jena" % "0.1" excludeAll(
          ExclusionRule(organization = "org.apache.jena"),
          ExclusionRule(organization = "org.slf4j")
        )
      )}
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git"), "banana-jena")
  )

  lazy val prefixes: Project = Project(
    id = "scalanvas-prefixes",
    base = file("prefixes"),
    dependencies = Seq(schemas, generators),
    settings = commonSettings
  )

  lazy val generators: Project = Project(
    id = "scalanvas-generators",
    base = file("generators"),
    dependencies = Seq(root),
    settings = commonSettings ++ Seq(
      libraryDependencies <+= scalaVersion(
        "org.scala-lang" % "scala-compiler" % _
      )
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git"), "banana-sesame")
  )

  lazy val schemas: Project = Project(
    id = "scalanvas-schemas",
    base = file("schemas"),
    settings = commonSettings
  )

  lazy val root: Project = Project(
    id = "scalanvas",
    base = file("."),
    settings = commonSettings
  ).aggregate(
    schemas, generators, prefixes, core
  )

  def commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.umd.mith",
    version := "0.0.0-SNAPSHOT",
    resolvers += "Sonatype snapshots" at
      "http://oss.sonatype.org/content/repositories/snapshots",
    scalaVersion := "2.10.2",
    scalaBinaryVersion := "2.10",
    scalacOptions := Seq(
      "-feature",
      "-language:implicitConversions",
      "-deprecation",
      "-unchecked"
    ),
    libraryDependencies <++= scalaVersion(sv => Seq(
      "org.slf4j" % "slf4j-simple" % "1.6.4"
    ))
  )
}


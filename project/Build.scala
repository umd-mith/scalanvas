import sbt._
import Keys._

object Scalanvas extends Build {
  lazy val core: Project = Project(
    id = "scalanvas-core",
    base = file("core"),
    dependencies = Seq(schemas),
    settings = commonSettings ++ Seq(
      libraryDependencies <++= scalaVersion { sv => Seq(
        //"org.w3" %% "banana-rdf" % "0.4",
        //"org.w3" %% "banana-jena" % "0.4",
        "net.sf.opencsv" % "opencsv" % "2.3",
        "org.apache.jena" % "jena-arq" % "2.11.1",
        "com.github.jsonld-java" % "jsonld-java-jena" % "0.2" excludeAll(
          ExclusionRule(organization = "org.apache.jena"),
          ExclusionRule(organization = "org.slf4j")
        )
      )}
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git#e641b8a5fccc5e8efa9a74c2a420462ee826f8ea"), "banana-jena"),
    ProjectRef(uri("git://github.com/umd-mith/banana-utils.git#arq-2.11"), "banana-io-jena"),
    ProjectRef(uri("git://github.com/umd-mith/banana-utils.git#arq-2.11"), "banana-prefixes"),
    ProjectRef(uri("git://github.com/umd-mith/banana-utils.git#arq-2.11"), "banana-argonaut")
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
  ).aggregate(schemas, core)

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
      "org.slf4j" % "slf4j-simple" % "1.6.4",
      "io.argonaut" %% "argonaut" % "6.0"
    ))
  )
}


import sbt._
import Keys._

object Scalanvas extends Build {
  lazy val util: Project = Project(
    id = "util",
    base = file("util"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-concurrent" % "7.0.6",
        "org.scalaz" %% "scalaz-core" % "7.0.6",
        "org.scalesxml" %% "scales-xml" % "0.6.0-M1",
        "xalan" % "xalan" % "2.7.1",
        "xalan" % "serializer" % "2.7.1"
      )
    )
  )

  lazy val bananaUtil: Project = Project(
    id = "banana-util",
    base = file("banana"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.apache.jena" % "jena-arq" % "2.11.1",
        "org.scalaz" %% "scalaz-core" % "7.0.6",
        "com.github.jsonld-java" % "jsonld-java-jena" % "0.4.1"
      )
    )
  ).dependsOn(
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git#95069024cf0184172c6dba9fc0be55efbeb5b863"), "banana-rdf"),
    ProjectRef(uri("git://github.com/w3c/banana-rdf.git#95069024cf0184172c6dba9fc0be55efbeb5b863"), "banana-jena")
  )

  lazy val core: Project = Project(
    id = "scalanvas-core",
    base = file("core"),
    dependencies = Seq(util, bananaUtil),
    settings = commonSettings
  )

  lazy val extensions: Project = Project(
    id = "scalanvas-extensions",
    base = file("extensions"),
    dependencies = Seq(core),
    settings = commonSettings
  )

  /*lazy val sga: Project = Project(
    id = "scalanvas-sga",
    base = file("sga"),
    dependencies = Seq(extensions, teiUtil),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "net.sf.opencsv" % "opencsv" % "2.3"
      )
    )
  )

  lazy val wwa: Project = Project(
    id = "scalanvas-wwa",
    base = file("wwa"),
    dependencies = Seq(extensions, teiUtil),
    settings = commonSettings
  )*/

  lazy val root: Project = Project(
    id = "scalanvas",
    base = file("."),
    settings = commonSettings
  ).aggregate(extensions)

  def commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.umd.mith",
    version := "0.0.0-SNAPSHOT",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    scalaVersion := "2.10.4",
    javaOptions := Seq(
      "-XX:MaxPermSize=512m",
      "-Xmx6G"
    ),
    scalacOptions := Seq(
      "-feature",
      "-language:implicitConversions",
      "-deprecation",
      "-unchecked"
    ),
    libraryDependencies <++= scalaVersion(sv => Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.6"
    ))
  )
}


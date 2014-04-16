package edu.umd.mith.scalanvas.extensions

import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._
import java.io.{ FileReader, File }
import edu.umd.mith.banana.jena.DefaultGraphJenaModule
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.implicits._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.util.xml.tei.implicits._
import edu.umd.mith.scalanvas.{ CollectionDoc, Configuration, Service, TeiCollection }
import edu.umd.mith.scalanvas.extensions._
import java.net.URI
import org.w3.banana.binder._
import org.w3.banana.jena._
import edu.umd.mith.scalanvas.rdf._
import edu.umd.mith.scalanvas.extensions.rdf._
import org.w3.banana._
import edu.umd.mith.scalanvas.io._

class MithStack(files: List[File]) extends MithPrefixes
  with MithObjectBinders
  with MithPropertyBinders
  with Helpers
  with TeiHelpers
  with MithPhysicalManifestParser
  with MithLogicalManifestParser
  with MithRangeParser[MithCanvas]
  with MithCanvasParser
  with MithTeiCollection { this: RDFOpsModule with MithConfiguration =>
  val docs = loadFiles(files).map(
    _.map {
      case (systemId, doc) =>
        val fileName = systemId.split("/").last
        (fileName -> CollectionDoc(XmlLabeler.addCharOffsets(doc), systemId, fileName))
    }
  ).run

  val idMap = TeiCollection.createIdMap(docs).run
  val handNames = MithTeiCollection.createHandNames(docs)
}

trait FrankensteinConfiguration extends MithConfiguration {
  lazy val FrankensteinServicePattern = """ox-frankenstein_([^_]+)_(.+)""".r
  lazy val baseUri = new URI("http://shelleygodwinarchive.org/data/ox")

  def constructManifestLabel(titleText: String): String = titleText.split(", ")(1)
  def constructManifestTitle(titleText: String): String = titleText.split(", ")(0)

  def constructCanvasUri(id: String) = basePlus(f"/$id%s")
  def constructRangeUri(id: String, n: String) = basePlus(f"/$id%s/$n%s")
  def constructReadingUri(id: String) = new URI(
    f"http://shelleygodwinarchive.org/tei/readingTEI/html/$id%s.html"
  )

  def constructSourceUri(id: String) = new URI(
    f"http://shelleygodwinarchive.org/tei/ox/$id%s.xml"
  )

  def constructManifestService(id: String) =  Some(
    Service(
      id match {
        case FrankensteinServicePattern(group, item) =>
          new URI(
            "http://shelleygodwinarchive.org/sc/oxford/frankenstein/%s/%s".format(
              group,
              item
            )
          )
        case other =>
          new URI(
            f"http://shelleygodwinarchive.org/sc/oxford/$other%s"
          )
      }
    )
  )

  lazy val imageFormat = "image/jp2"
  lazy val imageService = Some(
    Service(
      new URI("http://tiles2.bodleian.ox.ac.uk:8080/adore-djatoka/resolver"),
      Some(new URI("http://sourceforge.net/projects/djatoka/"))
    )
  )
}

class Frankenstein(filePaths: List[String]) extends MithStack(filePaths.map(new File(_)))
  with DefaultGraphJenaModule with JenaManifestWriter with FrankensteinConfiguration {

  def savePhysicalManifests(): Unit =
    docs.keys.foreach(savePhysicalManifest)

  def savePhysicalManifest(fileName: String): Unit = {
    val doc = docs(fileName)
    val manifest = parsePhysicalManifest(doc).run
    saveJsonLd[MithCanvas, MithPhysicalManifest](manifest)(
      "/edu/umd/mith/scalanvas/context.json",
      new File("output")
    )
  }
}

object Demo extends App {
  val frankenstein = new Frankenstein(args(0).split("\\s").toList)
  
  if (args.length == 1) {
    frankenstein.savePhysicalManifests()
  } else {
    frankenstein.savePhysicalManifest(args(1))
  }
}


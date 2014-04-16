package edu.umd.mith.scalanvas.extensions

import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._
import java.io.File
import edu.umd.mith.banana.jena.DefaultGraphJenaModule
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.scalanvas.{ CollectionDoc, TeiCollection }
import edu.umd.mith.scalanvas.extensions._
import edu.umd.mith.scalanvas.extensions.rdf._
import edu.umd.mith.scalanvas.io._
import edu.umd.mith.scalanvas.rdf._
import org.w3.banana.binder._
import org.w3.banana.jena._
import org.w3.banana._
import scalaz._, Scalaz._

class MithStack(files: List[File]) extends MithPrefixes
  with MithObjectBinders
  with MithPropertyBinders
  with Helpers
  with TeiHelpers
  with MithPhysicalManifestParser
  with MithLogicalManifestParser
  with MithRangeParser[MithCanvas]
  with MithCanvasParser
  with MithTeiCollection
  with DefaultGraphJenaModule
  with JenaManifestWriter { this: MithConfiguration =>
  val docs = loadFiles(files).map(
    _.map {
      case (systemId, doc) =>
        val fileName = systemId.split("/").last
        (fileName -> CollectionDoc(XmlLabeler.addCharOffsets(doc), systemId, fileName))
    }
  ).run

  val idMap = TeiCollection.createIdMap(docs).run
  val handNames = MithTeiCollection.createHandNames(docs)

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

  def saveLogicalManifests(): Unit = docs.foreach {
    case (fileName, doc) =>
      // Assuming that every msItem with an xml:id can be treated as a logical
      // manifest.
      val msItems = (top(doc.doc).\\*(teiNs("msItem")).*(_ \@ xmlIdAttr)).toList
      val manifests = msItems.traverseU(parseLogicalManifest(doc)).run

      manifests.foreach { manifest =>
        saveJsonLd[MithCanvas, MithLogicalManifest](manifest)(
          "/edu/umd/mith/scalanvas/context.json",
          new File("output")
        )
      }
  }
}


package edu.umd.mith.scalanvas.extensions

import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._
import java.io.{ FileReader, File }
import edu.umd.mith.banana.jena.DefaultGraphJenaModule
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.implicits._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.util.xml.tei.implicits._
import edu.umd.mith.scalanvas.extensions._
import edu.umd.mith.scalanvas.{ CollectionDoc, Configuration, Service, TeiCollection }
import java.net.URI
import org.w3.banana.binder._
import org.w3.banana.jena._
import edu.umd.mith.scalanvas.rdf._
import edu.umd.mith.scalanvas.extensions.rdf._
import org.w3.banana._
import edu.umd.mith.scalanvas.io._

object Demo extends App with DefaultGraphJenaModule with MithObjectBinders with MithPropertyBinders with Helpers {
  val teiDocs = loadFiles(args.toList.map(new File(_))).map(
    _.map {
      case (systemId, doc) =>
        val fileName = systemId.split("/").last
        (fileName -> CollectionDoc(XmlLabeler.addCharOffsets(doc), systemId, fileName))
    }
  ).run

  val FrankensteinServicePattern = """ox-frankenstein_([^_]+)_(.+)""".r

  val parser = new MithPhysicalManifestParser with MithLogicalManifestParser
    with MithRangeParser[MithCanvas] with MithCanvasParser with MithConfiguration with MithTeiCollection {
    val docs = teiDocs
    val idMap = TeiCollection.createIdMap(docs).run
    val handNames = MithTeiCollection.createHandNames(docs)
    val baseUri = new URI("http://shelleygodwinarchive.org/data/ox")

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

    val imageFormat = "image/jp2"
    val imageService = Some(
      Service(
        new URI("http://tiles2.bodleian.ox.ac.uk:8080/adore-djatoka/resolver"),
        Some(new URI("http://sourceforge.net/projects/djatoka/"))
      )
    )
  }

  val doc = parser.docs.toList.head._2
  //val msItem = (top(doc.doc) \\* teiNs("msItem")).toList.apply(2)
  val manifest = parser.parsePhysicalManifest(doc).run

  val writer = new edu.umd.mith.scalanvas.io.JenaManifestWriter {}
  writer.saveJsonLd[MithCanvas, MithPhysicalManifest](manifest)("/edu/umd/mith/scalanvas/context.json", new File("output"))
}


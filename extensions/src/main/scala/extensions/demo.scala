package edu.umd.mith.scalanvas.extensions

import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._
import java.io.{ FileReader, File }
import edu.umd.mith.scalanvas.util.xml._
import edu.umd.mith.scalanvas.util.xml.implicits._
import edu.umd.mith.scalanvas.util.xml.tei._
import edu.umd.mith.scalanvas.util.xml.tei.implicits._
import edu.umd.mith.scalanvas.extensions._
import edu.umd.mith.scalanvas.extensions.parsing._
import edu.umd.mith.scalanvas.extensions.model._
import edu.umd.mith.scalanvas.model.{ Configuration, Service }
import edu.umd.mith.scalanvas.parsing.{ TeiCollection, CollectionDoc }
import java.net.URI
import org.w3.banana.binder._
import org.w3.banana.jena._
import edu.umd.mith.scalanvas.rdf._
import edu.umd.mith.scalanvas.extensions.rdf._
import org.w3.banana._
import edu.umd.mith.scalanvas.io._

object Demo extends App {
  val teiDocs = loadFiles(args.toList.map(new File(_))).map(
    _.map {
      case (systemId, doc) =>
        val fileName = systemId.split("/").last
        (fileName -> CollectionDoc(XmlLabeler.addCharOffsets(doc), systemId, fileName))
    }
  ).run

  val ServicePattern = """ox-frankenstein_([^_]+)_(.+)""".r

  val parser = new MithPhysicalManifestParser with MithLogicalManifestParser
    with MithRangeParser[MithCanvas] with MithCanvasParser with MithConfiguration with MithTeiCollection {
    val docs = teiDocs
    val idMap = TeiCollection.createIdMap(docs).run
    val handNames = MithTeiCollection.createHandNames(docs)
    val baseUri = new URI("http://shelleygodwinarchive.org/data/ox")

    def constructManifestLabel(titleText: String): String = titleText.split(", ")(1)
    def constructManifestTitle(titleText: String): String = titleText.split(", ")(0)

    def constructCanvasUri(id: String) = basePlus(id)
    def constructRangeUri(id: String, n: String) = basePlus(f"$id%s/$n%s")
    def constructReadingUri(id: String) = new URI(
      f"http://shelleygodwinarchive.org/tei/readingTEI/html/$id%s.html"
    )

    def constructSourceUri(id: String) = new URI(
      f"http://shelleygodwinarchive.org/tei/ox/$id%s.xml"
    )

    def constructManifestService(id: String) =  Some(
      Service(
        id match {
          case ServicePattern(group, item) =>
            new URI(
              "http://shelleygodwinarchive.org/sc/oxford/frankenstein/%s/%s".format(
                group,
                item
              )
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
}


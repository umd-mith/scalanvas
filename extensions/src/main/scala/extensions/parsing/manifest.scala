package edu.umd.mith.scalanvas.extensions.parsing

import edu.umd.mith.scalanvas.parsing._
import edu.umd.mith.scalanvas.extensions.model.{ MithCanvas, LogicalManifest }
import edu.umd.mith.scalanvas.model.{ Canvas, Configuration, ImageForPainting, Link, Range }
import edu.umd.mith.scalanvas.util.concurrent._
import edu.umd.mith.scalanvas.util.xml._
import edu.umd.mith.scalanvas.util.xml.implicits._
import edu.umd.mith.scalanvas.util.xml.tei._
import edu.umd.mith.scalanvas.util.xml.tei.implicits._
import java.net.URI
import scalaz._, Scalaz._
import scalaz.concurrent._
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

trait LogicalManifestParser[C <: MithCanvas, M <: LogicalManifest[C, M]] { this: MithCanvasParser with MithRangeParser[C] with MithTeiCollection with Configuration =>
  def parseManifest(doc: CollectionDoc)(msItem: XmlPath): Task[M] = {
    /*val parentId = parseMsItemId(msItem)

    (msItem \* teiNs("msItem")).toList.traverseU { child =>
      val surfaces = Nondeterminism[Task].gather(
        (
          child \*
          teiNs("locusGrp") \*
          teiNs("locus") \@
          NoNamespaceQName("target")
        ).flatMap(_.value.split("\\s")).map(idRef =>
          resolveIdRef(doc)(idRef).toTask(
            new Exception(f"Missing xml:id reference $idRef%s in ${ doc.fileName }%s.")
          ).flatMap(parseCanvas(doc))
        ).toSeq
      )

      (
        parentId.map(constructRangeUri) |@|
        parseRangeLabel(child) |@|
        surfaces
      )(Range.apply)
    }*/
    ???
  }
}


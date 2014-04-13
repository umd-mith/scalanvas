package edu.umd.mith.scalanvas.extensions.parsing

import edu.umd.mith.scalanvas.parsing._
import edu.umd.mith.scalanvas.model.{ Canvas, Configuration, Range }
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

trait MithRangeParser[C <: Canvas] extends RangeParser[C] { this: CanvasParser[C] with TeiCollection with Configuration =>
  def parseMsItemId(msItem: XmlPath): Task[String] =
    (msItem \@ xmlIdAttr).one.headOption.map(_.attribute.value).toTask(
      MissingXmlIdError("msItem")
    )

  def parseRangeLabel(msItem: XmlPath): Task[String] =
    (msItem \* teiNs("bibl") \* teiNs("title")).\+.text.one.headOption.map(_.item.value).toTask(
      new Exception("Missing title on range msItem.")
    )

  def parseRangeN(msItem: XmlPath): Task[String] =
    (msItem \@ NoNamespaceQName("n")).one.headOption.map(text(_)).toTask(
      new Exception("Missing title on range msItem.")
    )

  def parseRanges(doc: CollectionDoc)(msItem: XmlPath): Task[List[Range[C]]] = {
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

      for {
        allSurfaces <- surfaces
        id <- parseMsItemId(msItem)
        n <- parseRangeN(child)
        label <- parseRangeLabel(child)
      } yield Range(constructRangeUri(id, n), label, allSurfaces)
    }
  }
}


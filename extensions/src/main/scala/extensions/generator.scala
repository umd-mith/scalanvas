package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas.extensions.model._
import edu.umd.mith.scalanvas.model.{ Canvas, Manifest, Range, Sequence }
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

object TeiCollection {
  val IdRef = """([^#]*)#(.+)""".r
}

trait TeiCollection {
  import TeiCollection._

  def docs: Map[String, Doc]

  def resolveIdRef(doc: Doc)(idRef: String): Task[XmlPath] = validationTask(
    idRef match {
      case IdRef("", ref) =>
        top(doc).\\*.withId(ref).\^.one.headOption.toSuccess(
          MissingElementError(idRef)
        )
      case IdRef(file, ref) =>
        docs.find(_._1.endsWith(file)).flatMap {
          case (_, doc) => top(doc).\\*.withId(ref).\^.one.headOption
        }.toSuccess(
          MissingElementError(idRef)
        )
      case other => MissingElementError(other).fail
    }
  )
}

trait CanvasParser[C <: Canvas] { this: TeiCollection =>
  def parseCanvas(surface: XmlPath): Task[C]
}

trait RangeParser[C <: Canvas] { this: CanvasParser[C] with TeiCollection with Configuration =>
  def parseRangeId(msItem: XmlPath): Task[String] =
    (msItem \@ xmlIdAttr).one.headOption.map(_.attribute.value).toTask(
      MissingXmlIdError("msItem")
    )

  def parseRangeLabel(msItem: XmlPath): Task[String] =
    (msItem \* teiNs("bibl") \* teiNs("title")).text.one.headOption.map(_.item.value).toTask(
      new Exception("Missing title on range msItem.")
    )

  def parseRanges(doc: Doc, msItem: XmlPath): Task[List[Range[C]]] = {
    (msItem \* teiNs("msItem")).toList.traverseU { child =>
      val surfaces = Nondeterminism[Task].gather(
        (
          child \*
          teiNs("locusGrp") \*
          teiNs("locus") \@
          NoNamespaceQName("target")
        ).flatMap(_.value.split("\\s")).map(idRef =>
          resolveIdRef(doc)(idRef).flatMap(parseCanvas)
        ).toSeq
      )

      (
        parseRangeId(child).map(constructRangeUri) |@|
        parseRangeLabel(child) |@|
        surfaces
      )(Range.apply)
    }
  }
}


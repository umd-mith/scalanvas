package edu.umd.mith.scalanvas.parsing

import edu.umd.mith.scalanvas.model.{ Canvas, Configuration, Range }
import edu.umd.mith.scalanvas.util.concurrent._
import edu.umd.mith.scalanvas.util.xml._
import edu.umd.mith.scalanvas.util.xml.implicits._
import edu.umd.mith.scalanvas.util.xml.tei._
import edu.umd.mith.scalanvas.util.xml.tei.implicits._
import java.net.URI
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

object TeiCollection {
  val IdRef = """([^#]*)#(.+)""".r
}

trait TeiCollection {
  import TeiCollection._

  def docs: Map[String, Doc]

  def resolveIdRef(root: XmlPath)(idRef: String): Task[XmlPath] = validationTask(
    idRef match {
      case IdRef("", ref) =>
        root.\\*.withId(ref).\^.one.headOption.toSuccess(
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

trait CanvasParser[C <: Canvas] {
  def parseCanvas(surface: XmlPath): Task[C]
}

trait RangeParser[C <: Canvas] {
  def parseRanges(msItem: XmlPath): Task[List[Range[C]]]
}


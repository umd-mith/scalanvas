package edu.umd.mith.scalanvas

import edu.umd.mith.util.concurrent._
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.implicits._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.util.xml.tei.implicits._
import java.net.URI
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

case class CollectionDoc(doc: Doc, systemId: String, fileName: String)

object TeiCollection {
  val IdRef = """([^#]*)#(.+)""".r

  def createIdMap(docs: Map[String, CollectionDoc]) = Task {
    docs.values.foldLeft(Map.empty[String, XmlPath]) {
      case (map, CollectionDoc(doc, _, fileName)) =>
        (top(doc) \\@ xmlIdAttr).\^.foldLeft(map) {
          case (map, path) =>
            val id = text(path \@ xmlIdAttr)
            val idRef = fileName + "#" + id

            if (map.contains(idRef)) throw new Exception(
              f"Duplicate xml:id $id%s in $fileName."
            ) else map.updated(idRef, path)
        }
    }
  }
}

trait TeiCollection {
  import TeiCollection._

  def docs: Map[String, CollectionDoc]
  def idMap: Map[String, XmlPath]

  def resolveIdRef(doc: CollectionDoc)(idRef: String) = idRef match {
    case IdRef("", ref) => idMap.get(doc.fileName + "#" + ref)
    case IdRef(_, _) => idMap.get(idRef)
  }
}

trait CanvasParser[C <: Canvas] {
  def parseCanvas(doc: CollectionDoc)(surface: XmlPath): Task[C]
}

trait RangeParser[C <: Canvas] {
  def parseRanges(doc: CollectionDoc)(msItem: XmlPath): Task[List[Range[C]]]
}


package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas.extensions.model._
import edu.umd.mith.scalanvas.util.xml._
import edu.umd.mith.scalanvas.util.xml.implicits._
import edu.umd.mith.scalanvas.util.xml.tei._
import edu.umd.mith.scalanvas.util.xml.tei.implicits._
import scalaz._, Scalaz._
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

case class Metadata(
  workTitle: String 
)

trait Generator {
  def collectMetadata(current: XmlPath) = {
    val items = current.ancestor_or_self_::.filter(
      _.tree.section.name == teiNs("msItem")
    ).one.toList
  }

  def resolveIdRef(docs: Map[String, Doc])(idRef: String): ValidationNel[Throwable, XmlPath] =
    idRef match {
      case IdRef("", ref) =>
        docs.flatMap {
          case (_, doc) => top(doc).\\*.withId(ref).\^.one.headOption
        }.headOption.toSuccess(
          NonEmptyList(MissingElementError(idRef): Throwable)
        )
      case IdRef(file, ref) =>
        docs.find(_._1.endsWith(file)).flatMap {
          case (_, doc) => top(doc).\\*.withId(ref).\^.one.headOption
        }.toSuccess(
          NonEmptyList(MissingElementError(idRef): Throwable)
        )
      case other => MissingElementError(other).failNel
    }

  val IdRef = """([^#]*)#(.+)""".r

  def createLogical(docs: Map[String, Doc], id: String) = docs.flatMap {
    case (_, doc) => doc.rootElem.msItemFor(id)
  }.headOption.map {
    case path =>
      val children = (path \* teiNs("msItem")).toList.map { msItem =>
        val targets = (
          msItem \*
          teiNs("locusGrp") \*
          teiNs("locus") \@
          NoNamespaceQName("target")
        ).flatMap(_.value.split("\\s")).toList.traverseU(resolveIdRef(docs))
      

    /*new MithManifest[MithCanvas, MithManifest] {
    }*/
        targets 
      }
      children
  }
}


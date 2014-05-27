package edu.umd.mith.scalanvas.util.xml.tei

import edu.umd.mith.scalanvas.util.xml.XmlLabeler
import scala.xml._
import scalaz.{ Node => _, _ }, Scalaz._

case class Annotation(pos: (Int, Int), place: Option[String], attrs: Map[String, String])

case class AnnotationExtractor(doc: Elem, ignore: Set[String]) {
  def attrEquals(name: String, value: String)(node: Node) =
    node.attributes.asAttrMap.get(name).exists(_ == value)

  def fromElem(e: Node): Annotation = {
    val attrs = e.attributes.asAttrMap
    Annotation((attrs("mu:b").toInt, attrs("mu:e").toInt), attrs.get("place"), attrs - "mu:b" - "mu:e" - "place" - "xml:id" - "spanTo")
  }

  def fromMilestone(e: Node): ValidationNel[String, Option[Annotation]] = {
    val attrs = e.attributes.asAttrMap

    attrs.get("spanTo").fold(
      ("Missing endpoint for " + e + "!").failNel[Option[Annotation]]
    ) { spanTo =>
      (doc \\ "anchor").filter(
        attrEquals("xml:id", spanTo.tail)
      ).headOption.map { anchor =>
        Annotation(
          (attrs("mu:b").toInt, anchor.attributes.asAttrMap("mu:b").toInt),
          attrs.get("place"),
          attrs - "mu:b" - "mu:e" - "place" - "xml:id" - "spanTo"
        ).some.success
      }.getOrElse(
        if (ignore(spanTo.tail)) none.success
          else ("Missing anchor: " + spanTo + "!").failNel
      )
    }
  }

  def additions: ValidationNel[String, List[Annotation]] =
    (doc \\ "addSpan").toList.traverseU(fromMilestone).map(
      _.flatten ++ (doc \\ "add").map(fromElem)
    )

  def deletions: ValidationNel[String, List[Annotation]] =
    (doc \\ "delSpan").toList.traverseU(fromMilestone).map(
      _.flatten ++ (doc \\ "del").map(fromElem)
    )

  def marginaliaMetamarks: ValidationNel[String, List[Annotation]] =
    (doc \\ "metamark").toList.filter { milestone =>
      val attrs = milestone.attributes.asAttrMap

      attrs.get("function").exists(_ == "marginalia")
    }.traverseU(fromMilestone).map(_.flatten)
}


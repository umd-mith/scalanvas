package edu.umd.mith.scalanvas.util.xml.tei

import edu.umd.mith.scalanvas.util.xml._
import edu.umd.mith.scalanvas.util.xml.implicits._
import scalaz._, Scalaz._
import scales.xml._
import scales.xml.ScalesXml._

case class Annotation(
  pos: (Int, Int),
  place: Option[String],
  attrs: Attributes
)

trait AnnotationExtractor {
  import implicits._

  def additions(doc: XmlTree): ValidationNel[Throwable, List[Annotation]] =
    doc.allElems("addSpan").traverseU(fromMilestone(doc)) |+|
    doc.allElems("add").traverseU(fromElem)

  def deletions(doc: XmlTree): ValidationNel[Throwable, List[Annotation]] =
    doc.allElems("delSpan").traverseU(fromMilestone(doc)) |+|
    doc.allElems("del").traverseU(fromElem)

  def createAnnotation(
    elem: Elem,
    bv: Validation[Throwable, Int],
    ev: Validation[Throwable, Int]
  ): ValidationNel[Throwable, Annotation] =
    (bv.toValidationNel |@| ev.toValidationNel)((b, e) =>
      Annotation(
        (b, e),
        elem.attributes(teiAttrs.place).map(_.value),
        elem.attributes - teiAttrs.place - teiAttrs.spanTo - beginOffset - endOffset
      )
    )

  def fromElem(elem: Elem): ValidationNel[Throwable, Annotation] =
    createAnnotation(elem, elem.beginningOffset, elem.endingOffset)

  def fromMilestone(doc: XmlTree)(elem: Elem): ValidationNel[Throwable, Annotation] =
    elem.attributes(teiAttrs.spanTo).fold(
      (MissingEndPointError(elem): Throwable).failNel[Annotation]
    ) {
      case spanToId =>
        doc.anchorFor(spanToId.value).map(_.tree.section).fold(
          (MissingAnchorError(spanToId.value): Throwable).failNel[Annotation]
        ) { anchor =>
          createAnnotation(
            elem,
            elem.beginningOffset,
            anchor.beginningOffset
          )
        }
    }
}


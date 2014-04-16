package edu.umd.mith.scalanvas.extensions
package rdf

import edu.umd.mith.scalanvas._
import edu.umd.mith.scalanvas.rdf.{ Helpers, ObjectBinders, PropertyBinders, ScalanvasPrefixes }
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.implicits._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.util.xml.tei.implicits._
import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._
import scalaz.{ Source => _, _ }, Scalaz._
import scales.xml._, ScalesXml._

trait TeiHelpers extends AnnotationExtractor with ZoneReader {
  this: RDFOpsModule with MithPrefixes with MithObjectBinders with MithPropertyBinders with Helpers with MithTeiCollection =>
  import Ops._

  lazy val Hand = "#(\\S+)".r
  lazy val BrokenHand = "(\\S+)".r

  def toHandClass(handString: String) = handString match {
    case Hand(hand) => Some(s"hand-$hand")
    case BrokenHand(hand) => Some(s"hand-$hand")
    case _ => None
  }

  def isAuthorialLine(line: XmlPath) = attrText(
    line \* teiNs("add") \* teiNs("note") \@ "type"
  ).fold(false)(_ == "authorial")

  def rendToTextAlignment(rend: String) =
    if (!rend.startsWith("indent")) Some(rend) else None

  def rendToIndentLevel(rend: String) =
    if (rend.startsWith("indent")) Some(rend.drop(6).toInt) else None

  def addCssClass(g: PointedGraph[Rdf], cls: String) = g -- mith.hasClass ->- cls

  private def bail(throwable: Throwable) = throw throwable

  def readTextAnnotations(canvas: MithCanvas): List[PointedGraph[Rdf]] =
    canvas.transcription.fold(List.empty[PointedGraph[Rdf]]) {
      case (doc, transcription) =>
        val canvasBeginningOffset = transcription.beginningOffset.valueOr(bail)

        val (linesWithIds, marginalia) = (transcription \\* teiNs("line")).toList.zipWithIndex.map {
          case (line, i) =>
            val id = attrText(line \@ xmlIdAttr)
            val b = line.beginningOffset.valueOr(bail) - canvasBeginningOffset
            val e = line.endingOffset.valueOr(bail) - canvasBeginningOffset

            val zone = line.ancestor_::.filter(node =>
              !node.isItem && node.tree.section.name == teiNs("zone")
            ).headOption.getOrElse(
              throw new Exception(f"No enclosing zone for line ${ i + 1 }%d in ${ canvas.uri.toString }%s.")
            )

            val inLibraryZone = attrText(zone \@ "type") == "library"

            val libraryHand = if (inLibraryZone) Some("hand-library") else None
            val handClass = lastHandShift(line).flatMap(toHandClass) 

            val rend = attrText(line \@ "rend")

            val lineAnnotation = (
              URI(canvas.uri.toString + f"/line-annotations/${ i + 1 }%04d")
                .a(oa.Annotation)
                .a(mith.LineAnnotation)
                .a(oax.Highlight)
                -- oa.hasTarget ->- (
                  textOffsetSelection(canvas.source, b, e)
                    -- mith.hasClass ->- libraryHand.orElse(handClass)
                )
                -- mith.textAlignment ->- rend.flatMap(rendToTextAlignment)
                -- mith.textIndentLevel ->- rend.flatMap(rendToIndentLevel)
            )

            val marginalia: Option[(String, (String, PointedGraph[Rdf]))] = if (isAuthorialLine(line)) {
              for {
                zoneTarget <- attrText(zone \@ "target")
                zoneType <- attrText(zone \@ "type")
              } yield zoneTarget -> (zoneType, lineAnnotation)
            } else None

            (id -> lineAnnotation, marginalia)
        }.unzip

        val lines = linesWithIds.map(_._2)

        val linesById = linesWithIds.collect {
          case (Some(id), line) => id -> line
        }.toMap

        val marginaliaAnnotations = marginalia.flatten.groupBy(_._1).map {
          case (id, lines) =>
            val targetLine = linesById.getOrElse(id, throw new RuntimeException(f"No line with id $id%s!"))

            val place = lines.head._2._1

            (
              bnode()
                .a(oa.Annotation)
                .a(mith.MarginalAnnotation)
                -- mith.hasPlace ->- place
                -- oa.hasTarget ->- targetLine
                -- oa.hasBody ->- lines.map(_._2._2)
            )
        }.toList

        val additions = getAdditions(doc.doc.rootElem).valueOr(errors => bail(errors.head)).map {
          case annotation @ Annotation((b, e), place, _) =>
            val placeCss = place.flatMap {
              case "superlinear" => Some("vertical-align: super;")
              case "sublinear"   => Some("vertical-align: sub;")
              case _ => None
            }

            val Hand = "#(\\S+)".r
            val BrokenHand = "(\\S+)".r

            val handClass = annotation.getAttr("hand").flatMap {
              case Hand(hand) => Some(s"hand-$hand")
              case BrokenHand(hand) => Some(s"hand-$hand")
              case _ => None
            }

            (
              bnode()
                .a(oa.Annotation)
                .a(mith.AdditionAnnotation)
                .a(oax.Highlight)
                -- oa.hasTarget ->- (
                  textOffsetSelection(canvas.source, b - canvasBeginningOffset, e - canvasBeginningOffset)
                    -- oa.hasStyle ->- (
                      placeCss.map { css => (
                        bnode().a(cnt.ContentAsText)
                          -- dc.format ->- "text/css"
                          -- cnt.chars ->- css
                      )}
                    )
                    -- mith.hasClass ->- handClass
                )
            )
        }

        val deletions = getDeletions(doc.doc.rootElem).valueOr(errors => bail(errors.head)).map {
          case annotation @ Annotation((b, e), _, _) =>
            val Hand = "#(\\S+)".r

            val handClass = annotation.getAttr("hand").flatMap {
              case Hand(hand) => Some(s"hand-$hand")
              case _ => None
            }

            (
              bnode()
                .a(oa.Annotation)
                .a(mith.DeletionAnnotation)
                .a(oax.Highlight)
                -- oa.hasTarget ->- (
                  textOffsetSelection(canvas.source, b - canvasBeginningOffset, e - canvasBeginningOffset)
                    -- mith.hasClass ->- handClass
                )
            )
        }

        val highlights = (transcription \\* "hi").toList.map { hi => 
          val b = hi.beginningOffset.valueOr(bail) - canvasBeginningOffset
          val e = hi.endingOffset.valueOr(bail) - canvasBeginningOffset

          val os = textOffsetSelection(canvas.source, b, e)

          val offset = attrText(hi \@ "rend") match {
            case Some("double-underline") => addCssClass(os, "double-underline")
            case Some("underline") => addCssStyle(os, "text-decoration: underline")
            case Some("italic") => addCssStyle(os, "font-style: italic")
            case Some("sup") => addCssStyle(os, "vertical-align: super")
            case Some("superscript") => addCssStyle(os, "vertical-align: super")
            case Some("subscript") => addCssStyle(os, "vertical-align: sub")
            case Some(rend) => sys.error(s"Unexpected rend value: $rend.")
            case None => os
          }

          bnode().a(oa.Annotation).a(oax.Highlight) -- oa.hasTarget ->- offset
        }

        val metamarkHighlights = getMarginaliaMetamarks(doc.doc.rootElem).valueOr(
          errors => sys.error(errors.toList.mkString("\n"))
        ).map {
          case annotation @ Annotation((b, e), _, _) =>
            val borderCss = annotation.getAttr("rend").flatMap {
              case "singleLine-left" => Some("border-left-style: solid")
              case "singleLine-right" => Some("border-right-style: solid")
              case "doubleLine-left" => Some("border-left-style: double")
              case "doubleLine-right" => Some("border-right-style: double")
              case _ => None
            }

            (
              bnode()
                .a(oa.Annotation)
                .a(oax.Highlight)
                -- oa.hasTarget ->- (
                  textOffsetSelection(canvas.source, b, e)
                    -- oa.hasStyle ->- (
                      borderCss.map { css => (
                        bnode().a(cnt.ContentAsText)
                          -- dc.format ->- "text/css"
                          -- cnt.chars ->- css
                      )}
                    )
                )
            )
        }

        lines ::: additions ::: deletions ::: (highlights ++ metamarkHighlights) ::: marginaliaAnnotations
  }
}


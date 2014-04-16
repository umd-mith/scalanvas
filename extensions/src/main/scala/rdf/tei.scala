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

trait TeiHelpers extends AnnotationExtractor {
  this: RDFOpsModule with MithPrefixes with MithPropertyBinders with Helpers with MithTeiCollection =>

  lazy val Hand = "#(\\S+)".r
  lazy val BrokenHand = "(\\S+)".r

  def handClass(handString: String) = handString match {
    case Hand(hand) => Some(s"hand-$hand")
    case BrokenHand(hand) => Some(s"hand-$hand")
    case _ => None
  }

  def isAuthorialLine(line: XmlPath) = attrText(
    line \* teiNs("add") \* teiNs("note") \@ "type"
  ).fold(false)(_ == "authorial")

  def readTextAnnotations(canvas: MithCanvas): List[PointedGraph[Rdf]] = Nil
/*    canvas.transcription.fold(Nil) {
      case (doc, transcription) =>
        val canvasBeginningOffset = transcription.beginningOffset.getOrElse(throwable => throw throwable)

        val (linesWithIds, marginalia) = (transcription \\* "line").toList.zipWithIndex.map {
          case (line, i) =>
            val attrs = line.attributes.asAttrMap

            val b = line.beginningOffset.getOrElse(throwable => throw throwable) - canvasBeginningOffset
            val e = line.endingOffset.getOrElse(throwable => throw throwable) - canvasBeginningOffset

            val inLibraryZone = attrsText(
              line.ancestor_:: \* teiNs("zone") \@ "type"
            ).contains("library")

            val libraryHand = if (inLibraryZone) Some("hand-library") else None

            val lineAnnotation = (
              URI(canvas.uri.toString + f"/line-annotations/${ i + 1 }%04d")
                .a(oa.Annotation)
                .a(mith.LineAnnotation)
                .a(oax.Highlight)
                -- oa.hasTarget ->- (
                  textOffsetSelection(canvas.source, b, e)
                  -- mith.hasClass ->- libraryHand.orElse(
                    handClass.filter { _ =>
                      !canvas.uri.toString.endsWith("ox-ms_abinger_c58/canvas/0047") || i > 7 
                    }
                  )
              )
              -- mith.textAlignment ->- attrs.get("rend").filterNot(_.startsWith("indent"))
              -- mith.textIndentLevel ->- attrs.get("rend").filter(_.startsWith("indent")).map(_.drop(6).toInt)
          )

          val marginalia = if (isAuthorialLine(line)) {
            (canvas.transcription.get \\ "zone").filter(
              zone => (zone \\ "line").contains(line)
            ).headOption.flatMap {
              zone =>
                val attrs = zone.attributes.asAttrMap
                
                for {
                  target <- attrs.get("target").map(_.tail)
                  zoneType <- attrs.get("type")
                } yield target -> (zoneType, lineAnnotation)
            }
          } else None

          (attrs.get("xml:id") -> lineAnnotation, marginalia)
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

        val additions = annotationExtractor.additions.valueOr(es => sys.error(es.toList.mkString("\n"))).map {
          case Annotation((b, e), place, attrs) =>
            val placeCss = place.flatMap {
              case "superlinear" => Some("vertical-align: super;")
              case "sublinear"   => Some("vertical-align: sub;")
              case _ => None
            }

            val Hand = "#(\\S+)".r
            val BrokenHand = "(\\S+)".r

            val handClass = attrs.get("hand").flatMap {
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
                  textOffsetSelection(canvas.source, b, e)
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

        val deletions = annotationExtractor.deletions.valueOr(es => sys.error(es.toList.mkString("\n"))).map {
          case Annotation((b, e), _, attrs) =>
            val Hand = "#(\\S+)".r

            val handClass = attrs.get("hand").flatMap {
              case Hand(hand) => Some(s"hand-$hand")
              case _ => None
            }

            (
              bnode()
                .a(oa.Annotation)
                .a(mith.DeletionAnnotation)
                .a(oax.Highlight)
                -- oa.hasTarget ->- (
                  textOffsetSelection(canvas.source, b, e)
                    -- mith.hasClass ->- handClass
                )
            )
        }

        val highlights = (canvas.transcription.get \\ "hi").toList.map { hi => 
          val attrs = hi.attributes.asAttrMap

          val b = attrs("mu:b").toInt
          val e = attrs("mu:e").toInt

          val os = textOffsetSelection(canvas.source, b, e)

          val offset = attrs.get("rend") match {
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

        val metamarkHighlights = annotationExtractor.marginaliaMetamarks.valueOr(
          errors => sys.error(errors.toList.mkString("\n"))
        ).map {
          case Annotation((b, e), _, attrs) =>
            val borderCss = attrs.get("rend").flatMap {
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
      i}
*/
}

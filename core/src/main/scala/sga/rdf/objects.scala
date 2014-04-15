package edu.umd.mith.sga.rdf

import edu.umd.mith.scalanvas.model._
import edu.umd.mith.scalanvas.rdf.{
  ObjectBinders => ScalanvasObjectBinders,
  PropertyBinders => ScalanvasPropertyBinders,
  ScalanvasPrefixes
}

import edu.umd.mith.scalanvas.util.xml.tei.{ Annotation, AnnotationExtractor }
import edu.umd.mith.sga.model._

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

import scalaz.{ Source => _, _ }, Scalaz._

trait ObjectBinders {
  this: PropertyBinders with ScalanvasPropertyBinders with ScalanvasObjectBinders =>

  implicit def ImageToPG[Rdf <: RDF](implicit ops: RDFOps[Rdf]): ToPG[Rdf, Image] =
    new SgaPrefixes[Rdf]
      with ResourceToPG[Rdf, Image]
      with RectToPG[Rdf, Image]
      with FormattedToPG[Rdf, Image]
      with HasRelatedServiceToPG[Rdf, Image]
      with MotivatedToPG[Rdf, Image] {
      override def toPG(image: Image) = super.toPG(image)
    }

  implicit def SgaCanvasToPG[Rdf <: RDF](implicit
    ops: RDFOps[Rdf]
  ): ToPG[Rdf, SgaCanvas] =
    new SgaPrefixes[Rdf]
      with ResourceToPG[Rdf, SgaCanvas]
      with LabeledToPG[Rdf, SgaCanvas]
      with RectToPG[Rdf, SgaCanvas]
      with MetadataLabeledToPG[Rdf, SgaCanvas]
      with SgaMetadataLabeledToPG[Rdf, SgaCanvas]
      with HasRelatedServiceToPG[Rdf, SgaCanvas] {
      override def toPG(canvas: SgaCanvas) = (
        super.toPG(canvas)
          .a(sc.Canvas)
          //.a(dms.Canvas)
      )
    }

  implicit def SgaManifestToPG[Rdf <: RDF](implicit ops: RDFOps[Rdf]): ToPG[Rdf, SgaManifest] =
    new SgaPrefixes[Rdf] with OreHelper[Rdf] with SpecificResourceHelper[Rdf]
      with ResourceToPG[Rdf, SgaManifest]
      with LabeledToPG[Rdf, SgaManifest]
      with MetadataLabeledToPG[Rdf, SgaManifest]
      with SgaMetadataLabeledToPG[Rdf, SgaManifest]
      with HasRelatedServiceToPG[Rdf, SgaManifest] {

      import ops._

      def readZones(canvas: SgaCanvas): List[PointedGraph[Rdf]] = {
        val zoneReader = new ZoneReader(canvas)
        zoneReader.readZones
      }

      def addCssStyle(g: PointedGraph[Rdf], css: String) = g -- oa.hasStyle ->- (
        bnode().a(cnt.ContentAsText)
          -- dc.format ->- "text/css"
          -- cnt.chars ->- css
      )

      def addCssClass(g: PointedGraph[Rdf], cls: String) = g -- sga.hasClass ->- cls

      def readTextAnnotations(canvas: SgaCanvas): List[PointedGraph[Rdf]] = {
        val annotationExtractor = AnnotationExtractor(canvas.transcription, Set("c56-0113.02"))

        val handShifts = (canvas.transcription \\ "handShift").toList.flatMap { handShift =>
          val attrs = handShift.attributes.asAttrMap
          for {
            handId <- attrs.get("new")
            begin <- attrs.get("mu:b").toInt
          } yield (handId, begin)
        }

        val Hand = "#(\\S+)".r
        val BrokenHand = "(\\S+)".r

        def isAuthorialLine(lineElem: xml.Node) =
          (lineElem \ "add" \ "note" \ "@type").exists {
            case xml.Text("authorial") => true
            case _ => false
          } 

        val (linesWithIds, marginalia) = (canvas.transcription \\ "line").toList.zipWithIndex.map { case (line, i) =>
          val attrs = line.attributes.asAttrMap

          val inLibraryZone = (canvas.transcription \\ "zone").filter(
            _.attributes.asAttrMap("type") == "library"
          ).flatMap(_ \\ "line").contains(line)

          val libraryHand = inLibraryZone match {
            case true => Some("hand-library")
            case false => None
          }

          val lineBegin = attrs("mu:b").toInt
          val hand = handShifts.takeWhile {
            case (handId, begin) => begin < lineBegin
          }.lastOption.map(_._1)

          val handClass = hand.flatMap {
            case Hand(hand) => Some(s"hand-$hand")
            case BrokenHand(hand) => Some(s"hand-$hand")
            case _ => None
          }

          val lineAnnotation = (
            URI(canvas.uri.toString + "/line-annotations/%04d".format(i + 1))
              .a(oa.Annotation)
              .a(sga.LineAnnotation)
              .a(oax.Highlight)
              -- oa.hasTarget ->- (
                textOffsetSelection(canvas.source, attrs("mu:b").toInt, attrs("mu:e").toInt)
                  -- sga.hasClass ->- libraryHand.orElse(
                    handClass.filter { _ =>
                      // This is a horrible hack.
                      !canvas.uri.toString.endsWith("ox-ms_abinger_c58/canvas/0047") || i > 7 
                    }
                  )
              )
              -- sga.textAlignment ->- attrs.get("rend").filterNot(_.startsWith("indent"))
              -- sga.textIndentLevel ->- attrs.get("rend").filter(_.startsWith("indent")).map(_.drop(6).toInt)
          )

          val marginalia = if (isAuthorialLine(line)) {
            (canvas.transcription \\ "zone").filter(
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
                .a(sga.MarginalAnnotation)
                -- sga.hasPlace ->- place
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
                .a(sga.AdditionAnnotation)
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
                    -- sga.hasClass ->- handClass
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
                .a(sga.DeletionAnnotation)
                .a(oax.Highlight)
                -- oa.hasTarget ->- (
                  textOffsetSelection(canvas.source, b, e)
                    -- sga.hasClass ->- handClass
                )
            )
        }

        val highlights = (canvas.transcription \\ "hi").toList.map { hi => 
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
      }

      override def toPG(manifest: SgaManifest) = {
        val imageAnnotations =
          manifest.sequence.canvases.flatMap { canvas =>
            canvas.images.map { image =>
              (
                bnode()
                //manifest.itemBasePlus("/image-annotations/" + canvas.seq).toUri
                  .a(oa.Annotation)
                  //.a(dms.ImageAnnotation)
                  -- oa.hasTarget ->- canvas
                  -- oa.hasBody ->- image
              )
            }
          }

        val rangeless = ( 
          super.toPG(manifest)
            .a(sc.Manifest)
            //.a(dms.Manifest)
            .a(ore.Aggregation)
            -- dc.title ->- manifest.title
            -- rdfs.label ->- manifest.label
            -- tei.idno ->- manifest.id
            //-- sc.hasSequences ->- List(manifest.sequence)
            -- sc.hasCanvases ->- manifest.sequence.canvases
            -- ore.aggregates ->- manifest.sequence
            -- ore.aggregates ->- (
              manifest.itemBasePlus("/reading-html").toUri
                .a(sc.AnnotationList)
                .a(sc.Layer)
                -- rdfs.label ->- "Reading layer"
                -- sc.forMotivation ->- sga.reading
            ).aggregates(
              manifest.sequence.canvases.map { canvas =>
                List(
                  bnode()
                  //manifest.itemBasePlus("/reading-html/" + canvas.seq).toUri
                    .a(oa.Annotation)
                    -- oa.hasTarget ->- canvas
                    -- oa.hasBody ->- canvas.reading
                    -- sc.motivatedBy ->- sga.reading
                )
              }
            ) 
            -- ore.aggregates ->- (
              manifest.itemBasePlus("/source-tei").toUri
                .a(sc.AnnotationList)
                .a(sc.Layer)
                -- rdfs.label ->- "TEI source"
                -- sc.forMotivation ->- sga.source
            ).aggregates(
              manifest.sequence.canvases.map { canvas =>
                List(
                  bnode()
                  //manifest.itemBasePlus("/source-tei/" + canvas.seq).toUri
                    .a(oa.Annotation)
                    -- oa.hasTarget ->- canvas
                    -- oa.hasBody ->- canvas.source
                )
              }
            )
            -- sc.hasImageAnnotations ->- (imageAnnotations)
            -- ore.aggregates ->- (
              manifest.itemBasePlus("/image-annotations").toUri
                .a(sc.AnnotationList)
                //.a(dms.ImageAnnotationList)
                -- sc.forMotivation ->- sc.painting
            ).aggregates(imageAnnotations)
            -- ore.aggregates ->- (
              manifest.itemBasePlus("/zone-annotations").toUri
                .a(sc.AnnotationList)
            ).aggregates(
              manifest.sequence.canvases.flatMap(readZones)
            )
            -- ore.aggregates ->- (
              manifest.itemBasePlus("/text-annotations").toUri
                .a(sc.AnnotationList)
                .a(sc.Layer)
                -- rdfs.label ->- "Transcription"
                -- sc.forMotivation ->- sc.painting
            ).aggregates(
              manifest.sequence.canvases.flatMap(readTextAnnotations)
            )
        )

        manifest.ranges.foldLeft(rangeless) {
          case (acc, range) =>
            acc -- ore.aggregates ->- (
              range.uri.toUri
                .a(sc.Range)
                -- dcterms.isPartOf ->- manifest.sequence
                -- rdfs.label ->- range.label
            ).aggregates(range.canvases)
        }
      }
    }
}


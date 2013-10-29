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
          .a(dms.Canvas)
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

      def readTextAnnotations(canvas: SgaCanvas): List[PointedGraph[Rdf]] = {
        val annotationExtractor = AnnotationExtractor(canvas.transcription, Set("c56-0113.02"))

        val hand = (canvas.transcription \\ "handShift").toList match {
          case List(handShift) => handShift.attributes.asAttrMap.get("new").filter(_ == "#pbs")
          case Nil => None
          case _ => throw new RuntimeException("Too many hand shifts.")
        }

        val Hand = "#(\\S+)".r
        val BrokenHand = "(\\S+)".r

        val handClass = hand.flatMap {
          case Hand(hand) => Some(s"hand-$hand")
          case BrokenHand(hand) => Some(s"hand-$hand")
          case _ => None
        }

        val lines = (canvas.transcription \\ "line").toList.zipWithIndex.map { case (line, i) =>
          val attrs = line.attributes.asAttrMap

          (
            URI(canvas.uri.toString + "/line-annotations/%04d".format(i + 1))
              .a(oa.Annotation)
              .a(sga.LineAnnotation)
              .a(oax.Highlight)
              -- oa.hasTarget ->- (
                textOffsetSelection(canvas.source, attrs("mu:b").toInt, attrs("mu:e").toInt)
                  -- sga.hasClass ->- handClass.filter { _ =>
                    // This is a horrible hack.
                    !canvas.uri.toString.endsWith("ox-ms_abinger_c58b/canvas/0011") || i > 5 
                  }
                  -- sga.textAlignment ->- attrs.get("rend").filterNot(_.startsWith("indent"))
                  -- sga.textIndentLevel ->- attrs.get("rend").filter(_.startsWith("indent")).map(_.drop(6).toInt)
              )
          )
        }

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

        lines ::: additions ::: deletions
      }

      override def toPG(manifest: SgaManifest) = {
        val rangeless = ( 
          super.toPG(manifest)
            .a(sc.Manifest)
            .a(dms.Manifest)
            .a(ore.Aggregation)
            -- dc.title ->- manifest.title
            -- rdfs.label ->- manifest.label
            -- tei.idno ->- manifest.id
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
            -- ore.aggregates ->- (
              manifest.itemBasePlus("/image-annotations").toUri
                .a(sc.AnnotationList)
                .a(dms.ImageAnnotationList)
                -- sc.forMotivation ->- sc.painting
            ).aggregates(
              manifest.sequence.canvases.flatMap { canvas =>
                canvas.images.map { image =>
                  (
                    bnode()
                    //manifest.itemBasePlus("/image-annotations/" + canvas.seq).toUri
                      .a(oa.Annotation)
                      .a(dms.ImageAnnotation)
                      -- oa.hasTarget ->- canvas
                      -- oa.hasBody ->- image
                  )
                }
              }
            )
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


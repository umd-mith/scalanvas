package edu.umd.mith.scalanvas.extensions
package rdf

import edu.umd.mith.scalanvas._
import edu.umd.mith.scalanvas.rdf.{ Helpers, ObjectBinders, PropertyBinders, ScalanvasPrefixes }
import edu.umd.mith.util.xml.tei.{ Annotation, AnnotationExtractor }
import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

import scalaz.{ Source => _, _ }, Scalaz._

trait MithObjectBinders extends ObjectBinders {
  this: RDFOpsModule with MithPrefixes with MithPropertyBinders with Helpers with TeiHelpers =>
  implicit def ImageToPG: ToPG[Rdf, Image] =
    new ImageToPG[Image] with MithMotivationHelpers {}

  implicit def MithCanvasToPG: ToPG[Rdf, MithCanvas] =
    new CanvasToPG[MithCanvas] with MithMetadataLabeledToPG[MithCanvas] {}

  implicit def MithLogicalManifestToPG: ToPG[Rdf, MithLogicalManifest] =
    new MithManifestToPG[MithCanvas, MithLogicalManifest] with MithMetadataLabeledToPG[MithLogicalManifest]

  implicit def MithPhysicalManifestToPG: ToPG[Rdf, MithPhysicalManifest] =
    new MithManifestToPG[MithCanvas, MithPhysicalManifest] with MithMetadataLabeledToPG[MithPhysicalManifest]

  trait MithManifestToPG[C <: MithCanvas, M <: MithManifest[C, M]] extends ManifestToPG[C, M] {
    this: MithMetadataLabeledToPG[M] =>
    import Ops._

    override def toPG(manifest: M) = {
      val imageAnnotations =
        manifest.sequence.canvases.flatMap { canvas =>
          canvas.images.map { image =>
            (
              bnode()
                .a(oa.Annotation)
                .a(dms.ImageAnnotation)
                -- oa.hasTarget ->- canvas
                -- oa.hasBody ->- image
            )
          }
        }

      val rangeless = ( 
        super.toPG(manifest)
          .a(sc.Manifest)
          .a(ore.Aggregation)
          -- dc.title ->- manifest.title
          -- rdfs.label ->- manifest.label
          -- tei.idno ->- manifest.id
          -- sc.hasCanvases ->- manifest.sequence.canvases
          -- ore.aggregates ->- manifest.sequence
          -- ore.aggregates ->- {
            if (manifest.hasTranscriptions)
              (
                manifest.itemBasePlus("/reading-html").toUri
                  .a(sc.AnnotationList)
                  .a(sc.Layer)
                  -- rdfs.label ->- "Reading layer"
                  -- sc.forMotivation ->- mith.reading
              ).aggregates(
                manifest.sequence.canvases.map { canvas =>
                  List(
                    bnode()
                      .a(oa.Annotation)
                      -- oa.hasTarget ->- canvas
                      -- oa.hasBody ->- canvas.reading
                      -- sc.motivatedBy ->- mith.reading
                  )
                }
              ).some else None
          }
          -- ore.aggregates ->- {
            if (manifest.hasTranscriptions)
              (
                manifest.itemBasePlus("/source-tei").toUri
                  .a(sc.AnnotationList)
                  .a(sc.Layer)
                  -- rdfs.label ->- "TEI source"
                  -- sc.forMotivation ->- mith.source
              ).aggregates(
                manifest.sequence.canvases.map { canvas =>
                  List(
                    bnode()
                      .a(oa.Annotation)
                      -- oa.hasTarget ->- canvas
                      -- oa.hasBody ->- canvas.source
                  )
                }
              ).some else None
          }
          -- sc.hasImageAnnotations ->- (imageAnnotations)
          -- ore.aggregates ->- (
            manifest.itemBasePlus("/image-annotations").toUri
              .a(sc.AnnotationList)
              -- sc.forMotivation ->- sc.painting
          ).aggregates(imageAnnotations)
          -- ore.aggregates ->- (
            manifest.itemBasePlus("/zone-annotations").toUri
              .a(sc.AnnotationList)
          ).aggregates(
            manifest.sequence.canvases.flatMap(readZones)
          )
          -- ore.aggregates ->- {
            if (manifest.hasTranscriptions)
              (
                manifest.itemBasePlus("/text-annotations").toUri
                  .a(sc.AnnotationList)
                  .a(sc.Layer)
                  -- rdfs.label ->- "Transcription"
                  -- sc.forMotivation ->- sc.painting
              ).aggregates(
                if (manifest.hasTranscriptions) manifest.sequence.canvases.flatMap(readTextAnnotations) else Nil
              ).some else None
          }
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


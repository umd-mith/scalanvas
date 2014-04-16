package edu.umd.mith.scalanvas
package rdf

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait ObjectBinders { this: PropertyBinders with Helpers =>
  trait ManifestToPG[Rdf <: RDF, C <: Canvas, A <: Manifest[C, A]] extends ToPG[Rdf, A]
    with ResourceToPG[Rdf, A]
    with LabeledToPG[Rdf, A]
    with HasRelatedServiceToPG[Rdf, A] { this: ScalanvasPrefixes[Rdf] =>
  }

  trait CanvasToPG[Rdf <: RDF, A <: Canvas] extends ToPG[Rdf, A]
    with ResourceToPG[Rdf, A]
    with RectToPG[Rdf, A]
    with LabeledToPG[Rdf, A]
    with HasRelatedServiceToPG[Rdf, A] { this: ScalanvasPrefixes[Rdf] =>
    override def toPG(canvas: A) = super.toPG(canvas).a(sc.Canvas)
  }
      
  trait ImageToPG[Rdf <: RDF, A <: Image] extends ToPG[Rdf, A]
    with ResourceToPG[Rdf, A]
    with RectToPG[Rdf, A]
    with FormattedToPG[Rdf, A]
    with HasRelatedServiceToPG[Rdf, A]
    with MotivatedToPG[Rdf, A] { this: ScalanvasPrefixes[Rdf] with MotivationHelpers[Rdf] =>
    override def toPG(image: A) = super.toPG(image)
  } 

  implicit def ServiceToPG[Rdf <: RDF](implicit ops: RDFOps[Rdf]): ToPG[Rdf, Service] =
    new ScalanvasPrefixes[Rdf]
      with ResourceToPG[Rdf, Service] {
      override def toPG(service: Service) = (
        super.toPG(service) -- dcterms.conformsTo ->- service.profile
      )
    }

  implicit def LinkToPG[Rdf <: RDF](implicit ops: RDFOps[Rdf]): ToPG[Rdf, Link] =
    new ScalanvasPrefixes[Rdf]
      with ResourceToPG[Rdf, Link]
      with FormattedToPG[Rdf, Link]

  implicit def SequenceToPG[Rdf <: RDF, C <: Canvas](implicit ops: RDFOps[Rdf], cToPG: ToPG[Rdf, C]): ToPG[Rdf, Sequence[C]] =
    new ScalanvasPrefixes[Rdf] with OreHelper[Rdf]
      with ResourceToPG[Rdf, Sequence[C]]
      with LabeledToPG[Rdf, Sequence[C]] {
      override def toPG(sequence: Sequence[C]) = (
        super.toPG(sequence).a(sc.Sequence)
      ).aggregates(sequence.canvases)
    }

  implicit def ResourceMapToPG[Rdf <: RDF, A](implicit ops: RDFOps[Rdf], ev: ToPG[Rdf, A]): ToPG[Rdf, ResourceMap[A]] =
    new ScalanvasPrefixes[Rdf]
      with ResourceToPG[Rdf, ResourceMap[A]]
      with FormattedToPG[Rdf, ResourceMap[A]] {
      override def toPG(resourceMap: ResourceMap[A]) = (
        super.toPG(resourceMap)
          .a(ore.ResourceMap)
          -- dc.format ->- resourceMap.format
          -- ore.describes ->- resourceMap.described
      )
    }

  implicit def TextSelectionToPG[Rdf <: RDF, A](implicit ops: RDFOps[Rdf], ev: ToPG[Rdf, A]): ToPG[Rdf, TextSelection[A]] =
    new ScalanvasPrefixes[Rdf]
      with ResourceToPG[Rdf, TextSelection[A]] {
      override def toPG(textSelection: TextSelection[A]) = (
        super.toPG(textSelection)
          .a(oa.SpecificResource)
          -- oa.hasSource ->- textSelection.source
          -- oa.hasSelector ->- (
            ops.bnode().a(oax.TextOffsetSelector)
              -- oax.begin ->- textSelection.begin
              -- oax.end ->- textSelection.end
          )
      )
    }
}


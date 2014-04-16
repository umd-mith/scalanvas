package edu.umd.mith.scalanvas
package rdf

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait ObjectBinders { this: RDFOpsModule with ScalanvasPrefixes with PropertyBinders with Helpers =>
  trait ManifestToPG[C <: Canvas, A <: Manifest[C, A]] extends ToPG[Rdf, A]
    with ResourceToPG[A]
    with LabeledToPG[A]
    with HasRelatedServiceToPG[A] {
  }

  trait CanvasToPG[A <: Canvas] extends ToPG[Rdf, A]
    with ResourceToPG[A]
    with RectToPG[A]
    with LabeledToPG[A]
    with HasRelatedServiceToPG[A] {
    override def toPG(canvas: A) = super.toPG(canvas).a(sc.Canvas)
  }
      
  trait ImageToPG[A <: Image] extends ToPG[Rdf, A]
    with ResourceToPG[A]
    with RectToPG[A]
    with FormattedToPG[A]
    with HasRelatedServiceToPG[A]
    with MotivatedToPG[A] { this: MotivationHelpers =>
    override def toPG(image: A) = super.toPG(image)
  } 

  implicit def ServiceToPG: ToPG[Rdf, Service] =
    new ResourceToPG[Service] {
      override def toPG(service: Service) = (
        super.toPG(service) -- dcterms.conformsTo ->- service.profile
      )
    }

  implicit def LinkToPG: ToPG[Rdf, Link] =
    new ResourceToPG[Link]
      with FormattedToPG[Link]

  implicit def SequenceToPG[C <: Canvas](implicit cToPG: ToPG[Rdf, C]): ToPG[Rdf, Sequence[C]] =
    new ResourceToPG[Sequence[C]]
      with LabeledToPG[Sequence[C]] {
      override def toPG(sequence: Sequence[C]) = (
        super.toPG(sequence).a(sc.Sequence)
      ).aggregates(sequence.canvases)
    }

  implicit def ResourceMapToPG[A](implicit ev: ToPG[Rdf, A]): ToPG[Rdf, ResourceMap[A]] =
    new ResourceToPG[ResourceMap[A]]
      with FormattedToPG[ResourceMap[A]] {
      override def toPG(resourceMap: ResourceMap[A]) = (
        super.toPG(resourceMap)
          .a(ore.ResourceMap)
          -- dc.format ->- resourceMap.format
          -- ore.describes ->- resourceMap.described
      )
    }

  implicit def TextSelectionToPG[A](implicit ev: ToPG[Rdf, A]): ToPG[Rdf, TextSelection[A]] =
    new ResourceToPG[TextSelection[A]] {
      override def toPG(textSelection: TextSelection[A]) = (
        super.toPG(textSelection)
          .a(oa.SpecificResource)
          -- oa.hasSource ->- textSelection.source
          -- oa.hasSelector ->- (
            Ops.bnode().a(oax.TextOffsetSelector)
              -- oax.begin ->- textSelection.begin
              -- oax.end ->- textSelection.end
          )
      )
    }
}


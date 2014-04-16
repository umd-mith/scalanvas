package edu.umd.mith.scalanvas
package rdf

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait PropertyBinders { this: RDFOpsModule with ScalanvasPrefixes with ObjectBinders =>
  trait ResourceToPG[A <: Resource] extends ToPG[Rdf, A] {
    import Ops._

    def toPG(a: A) = PointedGraph(
      a.muri.fold[Rdf#Node](bnode())(_.toUri)
    )
  }

  trait RectToPG[A <: Rect] extends ToPG[Rdf, A] {
    abstract override def toPG(a: A) = (
      super.toPG(a)
        -- exif.width ->- a.width
        -- exif.height ->- a.height
    )
  }

  trait LabeledToPG[A <: Labeled] extends ToPG[Rdf, A] {
    abstract override def toPG(a: A) = (
      super.toPG(a) -- rdfs.label ->- a.label
    )
  }

  trait FormattedToPG[A <: Formatted] extends ToPG[Rdf, A] {
    abstract override def toPG(a: A) = (
      super.toPG(a) -- dc.format ->- a.format
    )
  }

  trait HasRelatedServiceToPG[A <: HasRelatedService] extends ToPG[Rdf, A] {
    abstract override def toPG(a: A) = (
      super.toPG(a) -- sc.hasRelatedService ->- a.service
    )
  }

  trait MetadataLabeledToPG[A <: MetadataLabeled] extends ToPG[Rdf, A] {
    abstract override def toPG(a: A) = (
      super.toPG(a)
        -- sc.agentLabel ->- a.agent
        -- sc.attributionLabel ->- a.attribution
        -- sc.dateLabel ->- a.date
    )
  }

  trait MotivationHelpers {
    def motivationUri: PartialFunction[Motivation, Rdf#URI] = {
      case Painting => sc.painting 
    }
  }

  trait MotivatedToPG[A <: Motivated] extends ToPG[Rdf, A] { this: MotivationHelpers =>
    abstract override def toPG(a: A) = (
      super.toPG(a) -- sc.motivatedBy ->- a.motivation.map(motivationUri.lift)
    )
  }

  trait ContentsMotivatedToPG[A <: ContentsMotivated] extends ToPG[Rdf, A] { this: MotivationHelpers =>
    abstract override def toPG(a: A) = (
      super.toPG(a) -- sc.forMotivation ->- a.motivation.map(motivationUri.lift)
    )
  }
}


package edu.umd.mith.scalanvas.rdf

import edu.umd.mith.scalanvas.model._

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait ObjectBinders { this: PropertyBinders with Helpers =>
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

  implicit def SequenceToPG[Rdf <: RDF, C <: Canvas](implicit
    ops: RDFOps[Rdf],
    cToPG: ToPG[Rdf, C]
  ): ToPG[Rdf, Sequence[C]] =
    new ScalanvasPrefixes[Rdf] with OreHelper[Rdf]
      with ResourceToPG[Rdf, Sequence[C]]
      with LabeledToPG[Rdf, Sequence[C]] {
      override def toPG(sequence: Sequence[C]) = (
        super.toPG(sequence).a(sc.Sequence).a(dms.Sequence)
      ).aggregates(sequence.canvases)
    }

  /*implicit def RangeToPG[Rdf <: RDF, C <: Canvas](implicit
    ops: RDFOps[Rdf],
    cToPG: ToPG[Rdf, C]
  ): ToPG[Rdf, Range[C]] =
    new ScalanvasPrefixes[Rdf] with OreHelper[Rdf]
      with ResourceToPG[Rdf, Range[C]]
      with LabeledToPG[Rdf, Range[C]] {
      override def toPG(range: Range[C]) = (
        super.toPG(range).a(sc.Range)
      ).aggregates(range.canvases)
    }*/

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


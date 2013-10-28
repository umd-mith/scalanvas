package edu.umd.mith.sga.rdf

import edu.umd.mith.scalanvas.model._
import edu.umd.mith.scalanvas.rdf._ //{ PropertyBinders => ScalanvasPropertyBinders }
import edu.umd.mith.sga.model._

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait PropertyBinders { //this: ScalanvasPropertyBinders => 
  trait MotivatedToPG[Rdf <: RDF, A <: Motivated] extends ToPG[Rdf, A] {
    this: SgaPrefixes[Rdf] =>

    val o = ops

    abstract override def toPG(a: A) = (
      super.toPG(a) -- sc.motivatedBy ->- a.motivation.map {
        case Painting => sc.painting
        case Reading => sga.reading
        case Source => sga.source
      }
    )
  }

  trait ContentsMotivatedToPG[Rdf <: RDF, A <: ContentsMotivated] extends ToPG[Rdf, A] {
    this: SgaPrefixes[Rdf] =>

    abstract override def toPG(a: A) = (
      super.toPG(a) -- sc.forMotivation ->- a.motivation.map {
        case Painting => sc.painting
        case Reading => sga.reading
        case Source => sga.source
      }
    )
  }

  trait SgaMetadataLabeledToPG[
    Rdf <: RDF,
    A <: SgaMetadataLabeled
  ] extends ToPG[Rdf, A] {
    this: SgaPrefixes[Rdf] =>

    abstract override def toPG(a: A) = (
      super.toPG(a)
        -- sga.shelfmarkLabel ->- a.shelfmark
        -- sga.folioLabel ->- a.folio
        -- sga.handLabel ->- a.hand
        -- sga.stateLabel ->- a.state
    )
  }
}


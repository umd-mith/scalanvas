package edu.umd.mith.scalanvas.extensions.rdf

import edu.umd.mith.scalanvas.model._
import edu.umd.mith.scalanvas.rdf._
import edu.umd.mith.scalanvas.extensions.model._

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait MithPropertyBinders extends PropertyBinders { this: MithObjectBinders =>
  trait MithMetadataLabeledToPG[Rdf <: RDF, A <: MithMetadataLabeled] extends MetadataLabeledToPG[Rdf, A] { this: MithPrefixes[Rdf] =>
    abstract override def toPG(a: A) = (
      super.toPG(a)
        -- mith.shelfmarkLabel ->- a.shelfmark
        -- mith.folioLabel ->- a.folio
        -- mith.handLabel ->- a.hand
        -- mith.stateLabel ->- a.state
    )
  }

  trait MithMotivationHelpers[Rdf <: RDF] extends MotivationHelpers[Rdf] { this: MithPrefixes[Rdf] =>
    override def motivationUri: PartialFunction[Motivation, Rdf#URI] = super.motivationUri orElse {
      case Reading => mith.reading
      case Source => mith.source
    }
  }
}


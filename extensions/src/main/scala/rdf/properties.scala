package edu.umd.mith.scalanvas.extensions
package rdf

import edu.umd.mith.scalanvas._
import edu.umd.mith.scalanvas.rdf._

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait MithPropertyBinders extends PropertyBinders { this: RDFOpsModule with MithPrefixes with MithObjectBinders =>
  trait MithMetadataLabeledToPG[A <: MithMetadataLabeled] extends MetadataLabeledToPG[A] {
    abstract override def toPG(a: A) = (
      super.toPG(a)
        -- mith.shelfmarkLabel ->- a.shelfmark
        -- mith.folioLabel ->- a.folio
        -- mith.handLabel ->- a.hand
        -- mith.stateLabel ->- a.state
    )
  }

  trait MithMotivationHelpers extends MotivationHelpers {
    override def motivationUri: PartialFunction[Motivation, Rdf#URI] = super.motivationUri orElse {
      case Reading => mith.reading
      case Source => mith.source
    }
  }
}


package edu.umd.mith.sga.rdf

import edu.umd.mith.scalanvas.rdf.ScalanvasPrefixes
import edu.umd.mith.sga.rdf.prefixes._
import org.w3.banana._
import org.w3.banana.binder._

abstract class SgaPrefixes[Rdf <: RDF](implicit val _ops: RDFOps[Rdf])
  extends ScalanvasPrefixes[Rdf] {
  val sga = SgaPrefix[Rdf]
}


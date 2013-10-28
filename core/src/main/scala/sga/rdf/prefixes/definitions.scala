package edu.umd.mith.sga.rdf.prefixes

import org.w3.banana._

class SgaPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("sga", "http://www.shelleygodwinarchive.org/ns1#")(ops) {
  val hasClass = apply("hasClass")
  val LineAnnotation = apply("LineAnnotation")
  val AdditionAnnotation = apply("AdditionAnnotation")
  val DeletionAnnotation = apply("DeletionAnnotation")
  val LineBreak = apply("LineBreak")
  val reading = apply("reading")
  val source = apply("source")
  val shelfmarkLabel = apply("shelfmarkLabel")
  val stateLabel = apply("stateLabel")
  val handLabel = apply("handLabel")
  val folioLabel = apply("folioLabel")
  val textAlignment = apply("textAlignment")
  val textIndentLevel = apply("textIndentLevel")
}

object SgaPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new SgaPrefix[Rdf](ops)
}


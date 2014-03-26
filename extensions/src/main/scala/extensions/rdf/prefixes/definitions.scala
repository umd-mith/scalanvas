package edu.umd.mith.scalanvas.extensions.rdf.prefixes

import org.w3.banana._

class MithPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("mith", "http://mith.umd.edu/sc/ns1#")(ops) {
  val hasClass = apply("hasClass")
  val LineAnnotation = apply("LineAnnotation")
  val AdditionAnnotation = apply("AdditionAnnotation")
  val DeletionAnnotation = apply("DeletionAnnotation")
  val MarginalAnnotation = apply("MarginalAnnotation")
  val LineBreak = apply("LineBreak")
  val reading = apply("reading")
  val source = apply("source")
  val shelfmarkLabel = apply("shelfmarkLabel")
  val folioLabel = apply("folioLabel")
  val stateLabel = apply("stateLabel")
  val handLabel = apply("handLabel")
  val textAlignment = apply("textAlignment")
  val textIndentLevel = apply("textIndentLevel")
  val hasPlace = apply("hasPlace")
}

object MithPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new MithPrefix[Rdf](ops)
}


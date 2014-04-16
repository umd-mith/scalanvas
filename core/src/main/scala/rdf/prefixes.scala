package edu.umd.mith.scalanvas.rdf

import edu.umd.mith.scalanvas.rdf.prefixes._
import org.w3.banana._
import org.w3.banana.binder._

abstract class ScalanvasPrefixes[Rdf <: RDF](implicit val ops: RDFOps[Rdf]) {
//abstract class ScalanvasPrefixes extends RDFOpsModule {
  val cnt = ContentPrefix[Rdf]
  val dc = DCElementsPrefix[Rdf]
  val dcterms = DCTermsPrefix[Rdf]
  val dct = DCTypesPrefix[Rdf]
  val dms = DmsPrefix[Rdf]
  val exif = ExifPrefix[Rdf]
  val oa = OpenAnnotationPrefix[Rdf]
  val oax = OpenAnnotationExtensionPrefix[Rdf]
  val ore = OREPrefix[Rdf]
  val rdf = RDFPrefix[Rdf]
  val rdfs = RDFSPrefix[Rdf]
  val sc = SharedCanvasPrefix[Rdf]
  val tei = TeiPrefix[Rdf]
}


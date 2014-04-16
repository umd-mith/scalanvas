package edu.umd.mith.scalanvas.rdf

import edu.umd.mith.scalanvas.rdf.prefixes._
import org.w3.banana._
import org.w3.banana.binder._

trait ScalanvasPrefixes { this: RDFOpsModule =>
  lazy val cnt = ContentPrefix[Rdf]
  lazy val dc = DCElementsPrefix[Rdf]
  lazy val dcterms = DCTermsPrefix[Rdf]
  lazy val dct = DCTypesPrefix[Rdf]
  lazy val dms = DmsPrefix[Rdf]
  lazy val exif = ExifPrefix[Rdf]
  lazy val oa = OpenAnnotationPrefix[Rdf]
  lazy val oax = OpenAnnotationExtensionPrefix[Rdf]
  lazy val ore = OREPrefix[Rdf]
  lazy val rdf = RDFPrefix[Rdf]
  lazy val rdfs = RDFSPrefix[Rdf]
  lazy val sc = SharedCanvasPrefix[Rdf]
  lazy val tei = TeiPrefix[Rdf]
}


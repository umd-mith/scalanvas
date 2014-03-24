package edu.umd.mith.scalanvas.rdf.prefixes

import org.w3.banana._

class DCElementsPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends DCPrefix[Rdf](ops) {
  val title = apply("title")
  val format = apply("format")
}

object DCElementsPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new DCElementsPrefix[Rdf](ops)
}

class DCTermsPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends DCTPrefix[Rdf](ops) {
  val conformsTo = apply("conformsTo")
  val isPartOf = apply("isPartOf")
}

object DCTermsPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new DCTermsPrefix[Rdf](ops)
}

class DCTypesPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("dct", "http://purl.org/dc/dcmitype/")(ops) {
  val Image = apply("Image")
}

object DCTypesPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new DCTypesPrefix[Rdf](ops)
}

class ContentPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("cnt", "http://www.w3.org/2011/content#")(ops) {
  val chars = apply("chars")
  val ContentAsText = apply("ContentAsText")
}

object ContentPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ContentPrefix[Rdf](ops)
}

class OpenAnnotationPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("oa", "http://www.w3.org/ns/openannotation/core/")(ops) {
  val hasBody = apply("hasBody")
  val hasSelector = apply("hasSelector")
  val hasSource = apply("hasSource")
  val hasStyle = apply("hasStyle")
  val hasTarget = apply("hasTarget")
  val Annotation = apply("Annotation")
  val Style = apply("Style")
  val SpecificResource = apply("SpecificResource")
  val FragmentSelector = apply("FragmentSelector")
}

object OpenAnnotationPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new OpenAnnotationPrefix[Rdf](ops)
}

class OpenAnnotationExtensionPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("oax", "http://www.w3.org/ns/openannotation/extension/")(ops) {
  val begin = apply("begin")
  val end = apply("end")
  val Highlight = apply("Highlight")
  val TextOffsetSelector = apply("TextOffsetSelector")
}

object OpenAnnotationExtensionPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new OpenAnnotationExtensionPrefix[Rdf](ops)
}

class OREPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("ore", "http://www.openarchives.org/ore/terms/")(ops) {
  val aggregates = apply("aggregates")
  val describes = apply("describes")
  val isDescribedBy = apply("isDescribedBy")
  val Aggregation = apply("Aggregation")
  val ResourceMap = apply("ResourceMap")
}

object OREPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new OREPrefix[Rdf](ops)
}

class TeiPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("tei", "http://www.tei-c.org/ns/1.0/")(ops) {
  val idno = apply("idno")
}

object TeiPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new TeiPrefix[Rdf](ops)
}

class ExifPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("exif", "http://www.w3.org/2003/12/exif/ns#")(ops) {
  val width = apply("width")
  val height = apply("height")
}

object ExifPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ExifPrefix[Rdf](ops)
}

class SharedCanvasPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("sc", "http://www.shared-canvas.org/ns/")(ops) {
  val Canvas = apply("Canvas")
  val Manifest = apply("Manifest")
  val Sequence = apply("Sequence")
  val Range = apply("Range")
  val Zone = apply("Zone")
  val ContentAnnotation = apply("ContentAnnotation")
  val AnnotationList = apply("AnnotationList")
  val Layer = apply("Layer")
  val forMotivation = apply("forMotivation")
  val hasRelatedService = apply("hasRelatedService")
  val motivatedBy = apply("motivatedBy")
  val painting = apply("painting")
  val agentLabel = apply("agentLabel")
  val dateLabel = apply("dateLabel")
  val locationLabel = apply("locationLabel")
  val attributionLabel = apply("attributionLabel")
  val rightsLabel = apply("rightsLabel")
  val hasCanvases = apply("hasCanvases")
  val hasSequences = apply("hasSequences")
  val hasImageAnnotations = apply("hasImageAnnotations")
}

object SharedCanvasPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new SharedCanvasPrefix[Rdf](ops)
}

class DmsPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("dms", "http://dms.stanford.edu/ns/")(ops) {
  val Canvas = apply("Canvas")
  val Manifest = apply("Manifest")
  val Sequence = apply("Sequence")
  val ImageBody = apply("ImageBody")
  val ImageAnnotation = apply("ImageAnnotation")
  val ImageAnnotationList = apply("ImageAnnotationList")
}

object DmsPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new DmsPrefix[Rdf](ops)
}

package edu.umd.mith.scalanvas

import edu.umd.mith.scalanvas.prefixes.StandardPrefixes
import org.w3.banana._
import org.w3.banana.binder._

trait Aliases[Rdf <: RDF] {
  type PG = PointedGraph[Rdf]
  type PGLike[A] = ToPG[Rdf, A]
  type URI = Rdf#URI
}

abstract class Prefixes[Rdf <: RDF](implicit ops: RDFOps[Rdf])
  extends StandardPrefixes[Rdf] {
  val dc = DCElementsPrefix[Rdf]
  val dms = DmsPrefix[Rdf]
  val exif = ExifPrefix[Rdf]
  val oa = OpenAnnotationPrefix[Rdf]
  val oax = OpenAnnotationExtensionPrefix[Rdf]
  val rdf = RDFPrefix[Rdf]
  val rdfs = RDFSPrefix[Rdf]
  val sc = SharedCanvasPrefix[Rdf]
  val sga = SgaPrefix[Rdf]
  val tei = TeiPrefix[Rdf]
}

trait OreUtils[Rdf <: RDF] { this: Aliases[Rdf] =>
  implicit val ops: RDFOps[Rdf]
  import ops._
  import org.w3.banana.diesel._
  import scala.language.reflectiveCalls

  def dc: DCElementsPrefix[Rdf]
  def ore: Prefix[Rdf] {
     val Aggregation: Rdf#URI
     val ResourceMap: Rdf#URI
     val aggregates: Rdf#URI
     val describes: Rdf#URI
  }
  def rdf: RDFPrefix[Rdf]

  def resourceMap[A: PGLike](uri: String, format: String, g: A) = (
    URI(uri).a(ore.ResourceMap)
      -- dc.format ->- format
      -- ore.describes ->- g
  )

  def jsonResourceMap[A: PGLike](base: String, g: A) =
    resourceMap(base + ".json", "application/rdf+json", g)

  def jsonldResourceMap[A: PGLike](base: String, g: A) =
    resourceMap(base + ".jsonld", "application/ld+json", g)

  def xmlResourceMap[A: PGLike](base: String, g: A) =
    resourceMap(base + ".xml", "application/rdf+xml", g)

  implicit class Aggregates(g: PG) {
    def aggregates[A: PGLike](aggregated: List[A]) = aggregated match {
      case h :: t =>
        aggregated.foldLeft(
          g.a(rdf.List).a(ore.Aggregation)
            -- rdf.first ->- h
            -- rdf.rest ->- t
        ) {
          case (acc, item) => acc -- ore.aggregates ->- item 
        }
      case Nil => g
    }
  }
}


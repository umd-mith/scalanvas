package edu.umd.mith.scalanvas.generators

import org.w3.banana._
import org.w3.banana.diesel._
import org.w3.banana.syntax._
import scalax.io.InputResource
import scalaz.{ Success => _, Failure => _, _ }, Scalaz._
import scala.util._

abstract class SchemaParser[Rdf <: RDF](implicit
  val ops: RDFOps[Rdf],
  val sparqlOps: SparqlOps[Rdf],
  val reader: RDFReader[Rdf, RDFXML],
  val sparqlGraph: SparqlGraph[Rdf] 
) {
  import ops._
  import sparqlOps._

  def toEither[A](t: util.Try[A]): Throwable \/ A = t match {
    case Success(s) => \/-(s)
    case Failure(f) => -\/(f)
  }

  def parseSchema[R](base: String, pre: String, resource: InputResource[R]) =
    toEither(reader.read(resource, base)).flatMap { graph =>
      val baseUri = URI(base)
      val engine = sparqlGraph(graph)

      def toName(uri: Rdf#URI) = {
        val rel = baseUri.relativize(uri).getString
        if (rel.charAt(0) == '#') rel.substring(1) else rel
      }

      val properties = engine.executeSelect(SelectQuery("""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        SELECT DISTINCT ?uri WHERE {
          ?uri a rdf:Property
        }
      """.format(baseUri))).toIterable.toList.traverseU {
        t => toEither(t("uri").flatMap(
          _.as[Rdf#URI].map(toName)
        ))
      }

      val classes = engine.executeSelect(SelectQuery("""
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        SELECT DISTINCT ?uri WHERE {
          ?uri a rdfs:Class
        }
      """.format(baseUri))).toIterable.toList.traverseU {
        t => toEither(t("uri").flatMap(
          _.as[Rdf#URI].map(toName)
        ))
      }

      for {
        ps <- properties
        cs <- classes
      } yield (ps, cs)
    }
}


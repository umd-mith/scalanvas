package edu.umd.mith.banana.io

/** Represents JSON-LD serialization. */
trait JsonLD

/** Represents Talis's RDF JSON serialization. */
trait RDFJson

/** Represents evidence that a type can be used as a JSON-LD context. */
trait JsonLDContext[C] {
  def toMap(context: C): java.util.Map[String, Object]
}


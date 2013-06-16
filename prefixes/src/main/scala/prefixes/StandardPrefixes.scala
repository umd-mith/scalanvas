package edu.umd.mith.scalanvas.prefixes

import edu.umd.mith.scalanvas.generators.PrefixGenerator
import org.w3.banana._

abstract class StandardPrefixes[Rdf <: RDF](implicit ops: RDFOps[Rdf]) {
  val cnt = PrefixGenerator.fromSchema(
    "http://www.w3.org/2011/content#",
    "cnt",
    "/edu/umd/mith/scalanvas/schemas/content.rdf"
  )

  val dct = PrefixGenerator.fromSchema(
    "http://purl.org/dc/dcmitype/",
    "dct",
    "/edu/umd/mith/scalanvas/schemas/dctype.rdf"
  )

  val ore = PrefixGenerator.fromSchema(
    "http://www.openarchives.org/ore/terms/",
    "ore",
    "/edu/umd/mith/scalanvas/schemas/oreterms.rdf"
  )
}


package edu.umd.mith.scalanvas.extensions.rdf

import edu.umd.mith.scalanvas.rdf.ScalanvasPrefixes
import edu.umd.mith.scalanvas.extensions.rdf.prefixes._
import org.w3.banana._
import org.w3.banana.binder._

trait MithPrefixes extends ScalanvasPrefixes { this: RDFOpsModule =>
  lazy val mith = MithPrefix[Rdf]
}


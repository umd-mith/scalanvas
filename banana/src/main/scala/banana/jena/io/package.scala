package edu.umd.mith.banana.jena.io

import edu.umd.mith.banana.io.JsonLDContext

/** Provides Jena implementations of JSON serializations.
  *
  * @author Travis Brown
  * @todo Should also provide readers. 
  */
package object jena {
  implicit def JsonLDWriter[C: JsonLDContext](implicit ctx: C) = new JsonLDWriter[C] {
    val context = ctx
  }

  implicit def RDFJsonWriter = new RDFJsonWriter {}
}


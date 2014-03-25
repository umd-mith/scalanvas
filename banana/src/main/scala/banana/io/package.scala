package edu.umd.mith.banana

import org.w3.banana.{ MimeType, Syntax }
import scalaz.NonEmptyList

/** Provides syntaxes for JSON serialization.
  *
  * @author Travis Brown
  */
package object io {
  implicit val JsonLD = new Syntax[JsonLD] {
    val mimeTypes = NonEmptyList(MimeType("application/ld+json"))
  }

  implicit val RDFJson = new Syntax[RDFJson] {
    val mimeTypes = NonEmptyList(MimeType("application/rdf+json"))
  }

  implicit object NamespaceMapJsonLDContext extends JsonLDContext[Map[String, String]] {
    def toMap(context: Map[String, String]): java.util.Map[String, Object] = {
      val javaContext = new java.util.HashMap[String, Object]()
      context.foreach { case (k, v) => javaContext.put(k, v) }
      javaContext
    }
  }

  implicit object NamespaceJavaMapJsonLdContext
    extends JsonLDContext[java.util.Map[String, Object]] {
    def toMap(context: java.util.Map[String, Object]) = context
  }
}


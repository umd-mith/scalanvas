package edu.umd.mith.sga

import edu.umd.mith.banana.jena.DefaultGraphJenaModule
import org.w3.banana.jena.Jena

package object frankenstein extends DefaultGraphJenaModule {
  type Rdf = Jena

  /*trait RDFJson

  import org.w3.banana.{ MimeType, Syntax }
  import scalaz.NonEmptyList

  implicit val RDFJson = new Syntax[RDFJson] {
    val mimeTypes: NonEmptyList[MimeType] = NonEmptyList(MimeType("application/rdf+json"))
  }

  import com.github.jsonldjava.core.JSONLD
  import com.github.jsonldjava.utils.JSONUtils
  import com.github.jsonldjava.impl.JenaRDFParser
  import com.hp.hpl.jena.rdf.model.ModelFactory
  import java.io.{ Writer => jWriter }
  //import org.openjena.riot.system.JenaWriterRdfJson
  import org.w3.banana._
  import org.w3.banana.jena.Jena
  import scala.util._

  implicit object RDFJsonWriter extends RDFWriter[Jena, RDFJson] {
    val syntax = RDFJson

    def write(
      graph: Jena#Graph,
      os: java.io.OutputStream,
      base: String
    ): Try[Unit] = Try {
      val model = ModelFactory.createModelForGraph(graph)

      //val os = java.io.OutputStreamWriter(os)

      org.apache.jena.riot.RDFDataMgr.write(os, model, org.apache.jena.riot.Lang.RDFJSON)
      //new JenaWriterRdfJson().write(model, new java.io.OutputStreamWriter(os), null)
      os.close()
    }
  }*/
}


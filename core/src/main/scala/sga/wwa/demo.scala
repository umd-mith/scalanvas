package edu.umd.mith.sga.wwa

import com.github.jsonldjava.utils.JSONUtils
//import argonaut._, Argonaut._
import org.w3.banana._
import org.w3.banana.syntax._
import edu.umd.mith.sga.model.SgaManifest
import edu.umd.mith.sga.json.IndexManifest
import edu.umd.mith.sga.rdf._
//import edu.umd.mith.banana.argo._
import edu.umd.mith.banana.io._
import edu.umd.mith.banana.io.jena._
import java.io.{ File, PrintWriter }
import scalax.io.Resource

// object JsonLdDemoBuilder extends JsonLdBuilder with App {
//   val outputDir = new File("jsonld-demo")

//   trait Dev extends WwaConfiguration
//     with BodleianImages
//     with SgaTei { this: WwaManifest => }

//   save(new LessingManifest with Dev, outputDir)
// }

trait JsonLdBuilder {
  def save(manifest: SgaManifest, outputDir: File) = {
    val dir = new File(outputDir, manifest.id)
    dir.mkdirs

    val output = new File(dir, "Manifest.jsonld")
    if (output.exists) output.delete()

    implicit object MSOContext extends JsonLDContext[java.util.Map[String, Object]] {
      def toMap(ctx: java.util.Map[String, Object]) = ctx
    }

    val writer = new JsonLDWriter[java.util.Map[String, Object]] {
      val context = JSONUtils.fromString(
        io.Source.fromInputStream(
          getClass.getResourceAsStream("/edu/umd/mith/scalanvas/context.json")
        ).mkString
      ).asInstanceOf[java.util.Map[String, Object]]
    }

    writer.write(
      manifest.jsonResource.toPG[Rdf].graph,
      Resource.fromFile(output),
      manifest.base.toString
    )
  }
}


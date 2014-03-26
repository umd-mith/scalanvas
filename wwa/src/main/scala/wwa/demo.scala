package edu.umd.mith.wwa

import com.github.jsonldjava.utils.JsonUtils
import org.w3.banana._
import org.w3.banana.syntax._
import edu.umd.mith.wwa.model.WwaManifest
import edu.umd.mith.rdf._
import edu.umd.mith.banana.io._
import edu.umd.mith.banana.jena.io._
import java.io.{ BufferedOutputStream, File, FileOutputStream }

trait JsonLdBuilder {
  def save(manifest: WwaManifest, outputDir: File) = {
    import ops._

    val dir = new File(outputDir, manifest.id)
    dir.mkdirs

    val output = new File(dir, "Manifest.jsonld")
    if (output.exists) output.delete()

    val writer = new JsonLDWriter[java.util.Map[String, Object]] {
      val context = JsonUtils.fromString(
        io.Source.fromInputStream(
          getClass.getResourceAsStream("/edu/umd/mith/scalanvas/context.json")
        ).mkString
      ).asInstanceOf[java.util.Map[String, Object]]
    }

    writer.write(
      manifest.jsonResource.toPG.graph,
      new BufferedOutputStream(new FileOutputStream(output)),
      manifest.base.toString
    )
  }
}

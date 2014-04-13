package edu.umd.mith.scalanvas.io

import edu.umd.mith.scalanvas.model.Manifest

import com.github.jsonldjava.utils.JsonUtils
import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.jena._
import org.w3.banana.syntax._
import edu.umd.mith.scalanvas.model.{ Canvas, Manifest, ResourceMap }
import edu.umd.mith.banana.io._
import edu.umd.mith.banana.jena.io._
import java.util.{ Map => JMap }
import java.io.{ BufferedOutputStream, File, FileOutputStream, InputStream, OutputStream }
import scala.io.Source

trait ManifestWriter[Rdf <: RDF] {
  def readContext(in: InputStream): JMap[String, Object] = {
    val source = Source.fromInputStream(in)
    val json = source.mkString
    source.close()

    JsonUtils.fromString(json).asInstanceOf[JMap[String, Object]]
  }

  def readContextFromResource(path: String): JMap[String, Object] = readContext(
    getClass.getResourceAsStream(path)
  )

  def write[F, C <: Canvas, M <: Manifest[C, M]](manifest: M)(out: OutputStream)(implicit
    writer: RDFWriter[Rdf, F],
    ops: RDFOps[Rdf],
    toPg: ToPG[Rdf, ResourceMap[M]]
  ) = {
    import ops._
    RDFWriter[Rdf, F].write(manifest.jsonldResource.toPG.graph, out, manifest.base.toString)
  }
}

trait JenaManifestWriter extends ManifestWriter[Jena] {
  def writeJsonLd[C <: Canvas, M <: Manifest[C, M], Ctx: JsonLdContext](manifest: M)(ctx: Ctx, out: OutputStream)(implicit
    toPg: ToPG[Jena, ResourceMap[M]]
  ) = write[JsonLd, C, M](manifest)(out)(
    new JsonLdWriter[Ctx] { val context = ctx },
    implicitly[RDFOps[Jena]],
    toPg
  )

  def saveJsonLd[C <: Canvas, M <: Manifest[C, M]](manifest: M)(contextPath: String, outputDir: File)(implicit
    toPg: ToPG[Jena, ResourceMap[M]]
  ) = {
    val dir = new File(outputDir, manifest.id)
    dir.mkdirs

    val output = new File(dir, "Manifest.jsonld")
    if (output.exists) output.delete()

    val out = new BufferedOutputStream(new FileOutputStream(output))

    writeJsonLd[C, M, JMap[String, Object]](manifest)(readContextFromResource(contextPath), out)
  }
}


package edu.umd.mith.sga.frankenstein

import org.w3.banana._
import org.w3.banana.syntax._
import edu.umd.mith.sga.model.SgaManifest
import edu.umd.mith.sga.json.IndexManifest
import edu.umd.mith.sga.rdf._
import edu.umd.mith.banana.io._
import edu.umd.mith.banana.io.jena._
import java.io.{ File, PrintWriter }
import scalax.io.Resource

object Builder extends App {
  save(
    new NotebookAManifest
      with FrankensteinConfiguration
      with BodleianImages
      with SgaTei
      with Cratylus
  )
  
  save(
    new NotebookBManifest
      with FrankensteinConfiguration
      with BodleianImages
      with SgaTei
      with Cratylus
  )

  save(
    new NotebookC1Manifest
      with FrankensteinConfiguration
      with BodleianImages
      with SgaTei
      with Cratylus
  )

  save(
    new NotebookC2Manifest
      with FrankensteinConfiguration
      with BodleianImages
      with SgaTei
      with Cratylus
  )

  save(
    new DraftManifest
      with FrankensteinConfiguration
      with BodleianImages
      with SgaTei
      with Cratylus
  )

  save(
    new FairCopyManifest
      with FrankensteinConfiguration
      with BodleianImages
      with SgaTei
      with Cratylus
  )

  def save(manifest: SgaManifest) = {
    val dir = new File("output", manifest.id)
    dir.mkdirs

    val output = new File(dir, "Manifest.json")
    if (output.exists) output.delete()

    val indexOutput = new File(dir, "Manifest-index.jsonld")
    if (indexOutput.exists) indexOutput.delete()

    val writer = RDFWriter[Rdf, RDFJson]
    val indexWriter = new PrintWriter(indexOutput)

    val indexManifest = new IndexManifest(manifest)

    writer.write(
      manifest.jsonResource.toPG[Rdf].graph,
      Resource.fromFile(output),
      manifest.base.toString
    )

    indexWriter.println(indexManifest.toJsonLd.spaces2)
    indexWriter.close()
  }
}


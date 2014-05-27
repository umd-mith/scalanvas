package edu.umd.mith.sga.frankenstein

import com.github.jsonldjava.utils.JSONUtils
import org.w3.banana._
import org.w3.banana.syntax._
import edu.umd.mith.sga.model.SgaManifest
import edu.umd.mith.sga.json.IndexManifest
import edu.umd.mith.sga.rdf._
import edu.umd.mith.banana.io._
import edu.umd.mith.banana.io.jena._
import java.io.{ File, PrintWriter }
import scalax.io.Resource

// object DevelopmentBuilder extends Builder with App {
//   val outputDir = new File(new File("output", "development"), "primary")

//   trait Dev extends FrankensteinConfiguration
//     with DevelopmentConfiguration
//     with BodleianImages
//     with SgaTei { this: FrankensteinManifest => }

//   save(new NotebookAManifest with Dev, outputDir)
//   // save(new NotebookBManifest with Dev, outputDir)
//   // save(new NotebookC1Manifest with Dev, outputDir)
//   // save(new NotebookC2Manifest with Dev, outputDir)
//   // save(new VolumeIManifest with Dev, outputDir)
//   // save(new VolumeIIManifest with Dev, outputDir)
//   // save(new VolumeIIIManifest with Dev, outputDir)
//   // save(new LessingManifest with Dev, outputDir)
// }

// object ProductionBuilder extends Builder with App {
//   val outputDir = new File("output", "production")
  
//   trait Production extends FrankensteinConfiguration
//     with SgaTei { this: FrankensteinManifest => }

//   val primaryOutputDir = new File(outputDir, "primary")
//   save(new NotebookAManifest with Production with BodleianImages, primaryOutputDir)
//   save(new NotebookBManifest with Production with BodleianImages, primaryOutputDir)
//   save(new NotebookC1Manifest with Production with BodleianImages, primaryOutputDir)
//   save(new NotebookC2Manifest with Production with BodleianImages, primaryOutputDir)
//   save(new VolumeIManifest with Production with BodleianImages, primaryOutputDir)
//   save(new VolumeIIManifest with Production with BodleianImages, primaryOutputDir)
//   save(new VolumeIIIManifest with Production with BodleianImages, primaryOutputDir)

//   val secondaryOutputDir = new File(outputDir, "fallback")
//   save(new NotebookAManifest with Production with MithDjatokaImages, secondaryOutputDir)
//   save(new NotebookBManifest with Production with MithDjatokaImages, secondaryOutputDir)
//   save(new NotebookC1Manifest with Production with MithDjatokaImages, secondaryOutputDir)
//   save(new NotebookC2Manifest with Production with MithDjatokaImages, secondaryOutputDir)
//   save(new VolumeIManifest with Production with MithDjatokaImages, secondaryOutputDir)
//   save(new VolumeIIManifest with Production with MithDjatokaImages, secondaryOutputDir)
//   save(new VolumeIIIManifest with Production with MithDjatokaImages, secondaryOutputDir)

//   val staticOutputDir = new File(outputDir, "fallback-static")
//   save(new NotebookAManifest with Production with MithStaticImages, staticOutputDir)
//   save(new NotebookBManifest with Production with MithStaticImages, staticOutputDir)
//   save(new NotebookC1Manifest with Production with MithStaticImages, staticOutputDir)
//   save(new NotebookC2Manifest with Production with MithStaticImages, staticOutputDir)
//   save(new VolumeIManifest with Production with MithStaticImages, staticOutputDir)
//   save(new VolumeIIManifest with Production with MithStaticImages, staticOutputDir)
//   save(new VolumeIIIManifest with Production with MithStaticImages, staticOutputDir)
// }

trait Builder {
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


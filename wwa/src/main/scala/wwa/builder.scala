package edu.umd.mith.wwa

import com.github.jsonldjava.utils.JsonUtils
import org.w3.banana._
import org.w3.banana.syntax._
import edu.umd.mith.scalanvas.io.Writer
import edu.umd.mith.wwa.model.WwaManifest
import edu.umd.mith.wwa.rdf._
import edu.umd.mith.banana.io._
import edu.umd.mith.banana.jena.io._
import java.io.{ BufferedOutputStream, File, FileOutputStream }

object DevelopmentBuilder extends Builder with App {
  val outputDir = new File(new File("output", "development"), "primary")

  trait Dev extends WwaConfiguration
    with DevelopmentConfiguration
    with BodleianImages
    with WwaTei { this: WwaManifest => }

  save(new LessingManifest with Dev, outputDir)
  save(new BunsenManifest with Dev, outputDir)
}

trait Builder extends Writer {
}


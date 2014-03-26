package edu.umd.mith.sga.frankenstein

import com.github.jsonldjava.utils.JsonUtils
import org.w3.banana._
import org.w3.banana.syntax._
import edu.umd.mith.sga.model.SgaManifest
import edu.umd.mith.sga.rdf._
import java.io.{ File, PrintWriter }

trait JsonLdBuilder {
  def save(manifest: SgaManifest, outputDir: File) = {
    val dir = new File(outputDir, manifest.id)
    dir.mkdirs
  }
}


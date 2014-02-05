package edu.umd.mith.sga.model

import edu.umd.mith.scalanvas.model.{ Canvas, Manifest }

trait SgaCanvas extends Canvas with SgaMetadataLabeled

trait SgaManifest extends Manifest[SgaCanvas, SgaManifest] with SgaMetadataLabeled {
  def hasTranscriptions = true
}


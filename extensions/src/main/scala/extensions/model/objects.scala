package edu.umd.mith.scalanvas.extensions.model

import edu.umd.mith.scalanvas.model.{ Canvas, Manifest }

trait MithCanvas extends Canvas with MithMetadataLabeled

trait MithManifest extends Manifest[MithCanvas, MithManifest] with MithMetadataLabeled {
  def hasTranscriptions = true
}


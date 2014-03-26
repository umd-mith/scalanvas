package edu.umd.mith.scalanvas.extensions.model

import edu.umd.mith.scalanvas.model.{ Canvas, Manifest }

trait MithCanvas extends Canvas with MithMetadataLabeled

trait MithManifest[C <: MithCanvas, M <: MithManifest[C, M]] extends Manifest[C, M] with MithMetadataLabeled { this: M =>
  def hasTranscriptions = true
}


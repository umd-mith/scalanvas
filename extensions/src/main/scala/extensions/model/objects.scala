package edu.umd.mith.scalanvas.extensions.model

import edu.umd.mith.scalanvas.model.{ Canvas, Manifest, Sequence }

trait MithCanvas extends Canvas with MithMetadataLabeled

trait MithManifest[C <: MithCanvas, M <: MithManifest[C, M]] extends Manifest[C, M] with MithMetadataLabeled { this: M =>
  def hasTranscriptions = true
}

trait PhysicalManifest[C <: MithCanvas, M <: PhysicalManifest[C, M]] extends MithManifest[C, M] { this: M =>
  def canvases: List[C]

  lazy val sequence = Sequence[C](
    Some(itemBasePlus("/physical-sequence")),
    "Physical sequence",
    canvases
  )
}

trait LogicalManifest[C <: MithCanvas, M <: LogicalManifest[C, M]] extends MithManifest[C, M] { this: M =>
  lazy val sequence = Sequence[C](
    Some(itemBasePlus("/logical-sequence")),
    "Logical sequence",
    ranges.flatMap(_.canvases)
  )
}


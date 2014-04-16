package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas.{ Canvas, Manifest, Sequence }

trait MithCanvas extends Canvas with MithMetadataLabeled

trait MithManifest[C <: MithCanvas, M <: MithManifest[C, M]] extends Manifest[C, M] with MithMetadataLabeled { this: M =>
  def hasTranscriptions = true
}

trait PhysicalManifest[C <: Canvas, M <: PhysicalManifest[C, M]] extends Manifest[C, M] { this: M =>
  def canvases: List[C]

  def ranges = Nil

  lazy val sequence = Sequence[C](
    Some(itemBasePlus("/physical-sequence")),
    "Physical sequence",
    canvases
  )
}

trait LogicalManifest[C <: Canvas, M <: LogicalManifest[C, M]] extends Manifest[C, M] { this: M =>
  lazy val sequence = Sequence[C](
    Some(itemBasePlus("/logical-sequence")),
    "Logical sequence",
    ranges.flatMap(_.canvases)
  )
}

trait MithPhysicalManifest extends PhysicalManifest[MithCanvas, MithPhysicalManifest]
  with MithManifest[MithCanvas, MithPhysicalManifest]

trait MithLogicalManifest extends LogicalManifest[MithCanvas, MithLogicalManifest]
  with MithManifest[MithCanvas, MithLogicalManifest]


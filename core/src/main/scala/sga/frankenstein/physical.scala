package edu.umd.mith.sga.frankenstein

import edu.umd.mith.scalanvas.model.Sequence
import edu.umd.mith.sga.model.SgaCanvas

trait PhysicalManifest extends FrankensteinManifest {
  this: FrankensteinConfiguration =>
  val ranges = Nil

  def firstIndex: Int
  def pageCount: Int

  private lazy val pages = shelfmarkMap.drop(firstIndex).take(pageCount)

  private lazy val canvases = pages.map {
    case (fileId, (shelfmark, folio)) => parseTeiFile(fileId, shelfmark, folio)
  }

  lazy val sequence = Sequence[SgaCanvas](
    Some(itemBasePlus("/physical-sequence")),
    "Physical sequence",
    canvases
  )
}

trait NotebookAManifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein-notebook_a"
  val firstIndex = 0
  val pageCount = 170
}

trait NotebookBManifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein-notebook_b"
  val firstIndex = 170
  val pageCount = 154
}

trait NotebookC1Manifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein-notebook_c1"
  val firstIndex = 324
  val pageCount = 36 
}

trait NotebookC2Manifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein-notebook_c2"
  val firstIndex = 360
  val pageCount = 26
}


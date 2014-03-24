package edu.umd.mith.sga.wwa

import edu.umd.mith.scalanvas.model.Sequence
import edu.umd.mith.sga.model.SgaCanvas

trait PhysicalManifest extends WwaManifest {
  this: WwaConfiguration =>
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

trait LessingManifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00055"
  val firstIndex = 0
  val pageCount = 6
}

trait BunsenManifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00200"
  val firstIndex = 6
  val pageCount = 2
}
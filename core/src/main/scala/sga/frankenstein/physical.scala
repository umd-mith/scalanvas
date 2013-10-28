package edu.umd.mith.sga.frankenstein

import edu.umd.mith.scalanvas.model.Sequence
import edu.umd.mith.sga.model.SgaCanvas

trait PhysicalManifest extends FrankensteinManifest {
  this: FrankensteinConfiguration =>
  val ranges = Nil

  private lazy val pages = shelfmarkMap.getOrElse(
    id,
    throw new RuntimeException(s"Invalid identifier: $id!")
  ).toList.sorted

  private lazy val canvases = pages.map {
    case (pageSeq, (_, _)) => parseTeiFile(s"$id-$pageSeq")
  }

  override lazy val shelfmark = pages.map(_._2._1).distinct match {
    case value :: Nil => Some(value)
    case _ => throw new RuntimeException("Multiple shelfmarks!")
  }

  lazy val sequence = Sequence[SgaCanvas](
    Some(itemBasePlus("/physical-sequence")),
    "Physical sequence",
    canvases
  )
}

trait NotebookAManifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-ms_abinger_c56"
}

trait NotebookBManifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-ms_abinger_c57"
}

trait NotebookC1Manifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-ms_abinger_c58a"
}

trait NotebookC2Manifest extends PhysicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-ms_abinger_c58b"
}


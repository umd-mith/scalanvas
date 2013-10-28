package edu.umd.mith.sga.frankenstein

import edu.umd.mith.scalanvas.model.{ Range, Sequence }
import edu.umd.mith.sga.frankenstein.util.ChapterMapReader
import edu.umd.mith.sga.model.SgaCanvas

trait LogicalManifest extends FrankensteinManifest with ChapterMapReader {
  this: FrankensteinConfiguration =>
  lazy val ranges = chapters.zipWithIndex.map {
    case ((title, pages), i) => Range(
      itemBasePlus("/range/%04d".format(i + 1)),
      title,
      pages.map {
        case (idWithSeq, _, _) => parseTeiFile(idWithSeq)
      }
    )
  }

  lazy val sequence = Sequence[SgaCanvas](
    Some(itemBasePlus("/logical-sequence")),
    "Logical sequence",
    ranges.flatMap(_.canvases)
  )
}

trait DraftManifest extends LogicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein_draft"
}

trait FairCopyManifest extends LogicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein_faircopy"
}


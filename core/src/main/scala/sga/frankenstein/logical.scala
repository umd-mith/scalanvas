package edu.umd.mith.sga.frankenstein

import edu.umd.mith.scalanvas.model.{ Range, Sequence }
import edu.umd.mith.sga.frankenstein.util.ChapterMapReader
import edu.umd.mith.sga.model.SgaCanvas

trait LogicalManifest extends FrankensteinManifest with ChapterMapReader {
  this: FrankensteinConfiguration =>
  lazy val ranges = chapters.filterNot(_._1 == "???").zipWithIndex.map {
    case ((title, pages), i) => Range(
      itemBasePlus("/range/%04d".format(i + 1)),
      title,
      pages.map {
        case (idWithSeq, shelfmark, folio) => parseTeiFile(idWithSeq, shelfmark, folio)
      }
    )
  }

  lazy val sequence = Sequence[SgaCanvas](
    Some(itemBasePlus("/logical-sequence")),
    "Logical sequence",
    ranges.flatMap(_.canvases)
  )
}

trait VolumeIManifest extends LogicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein-volume_i"
}

trait VolumeIIManifest extends LogicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein-volume_ii"
}

trait VolumeIIIManifest extends LogicalManifest {
  this: FrankensteinConfiguration =>
  val id = "ox-frankenstein-volume_iii"
}


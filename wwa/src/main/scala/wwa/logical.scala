package edu.umd.mith.sga.wwa

import edu.umd.mith.scalanvas.model.{ Range, Sequence }
import edu.umd.mith.sga.wwa.util.ChapterMapReader
import edu.umd.mith.sga.model.SgaCanvas

trait LogicalManifest extends WwaManifest with ChapterMapReader {
  this: WwaConfiguration =>
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
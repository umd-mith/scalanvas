package edu.umd.mith.wwa

import edu.umd.mith.scalanvas.model.{ Range, Sequence }
import edu.umd.mith.wwa.util.ChapterMapReader
import edu.umd.mith.wwa.model.WwaCanvas

trait LogicalManifest extends MarginaliaManifest with ChapterMapReader {
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

  lazy val sequence = Sequence[WwaCanvas](
    Some(itemBasePlus("/logical-sequence")),
    "Logical sequence",
    ranges.flatMap(_.canvases)
  )
}

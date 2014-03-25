package edu.umd.mith.sga.wwa.util

import edu.umd.mith.sga.wwa.LogicalManifest
import edu.umd.mith.sga.model.SgaManifest
import scala.io.Source

trait ChapterMapReader { this: SgaManifest =>
  private[this] val IdPattern = "ox-wwa-(volume_i{1,3})".r

  private[this] lazy val stream = getClass.getResourceAsStream(
    "/edu/umd/mith/sga/wwa/%s-chapters.txt".format(
      id match {
        case IdPattern(volume) => volume
        case id => throw new RuntimeException(
          "No chapter map for %s!".format(id)
        )
      }
    )
  )

  private[this] lazy val Line =
    """^((?:.+)-\d\d\d\d)\s+([^,]+),\s([^#]+)(?:\s#.+)?$""".r

  private[this] lazy val source = Source.fromInputStream(stream)

  lazy val chapters = source.getLines.foldLeft(
    (
      List.empty[(String, List[(String, String, String)])],
      ("", List.empty[(String, String, String)])
    )
  ) {
    case ((list, (name, current)), Line(id, shelfmark, leaf)) =>
      (list, (name, current :+ (id, shelfmark, leaf)))
    case ((list, (name, current)), line) if line.isEmpty =>
      (list :+ (name, current), ("", List.empty[(String, String, String)]))
    case ((list, (name, current)), line) => (list, (line, current))
  }._1
}


package edu.umd.mith.sga.frankenstein.util

import edu.umd.mith.sga.frankenstein.LogicalManifest
import edu.umd.mith.sga.model.SgaManifest
import scala.io.Source

trait ChapterMapReader { this: SgaManifest =>
  private[this] lazy val stream = getClass.getResourceAsStream(
    "/edu/umd/mith/sga/frankenstein/%s-chapters.txt".format(
      id match {
        case "ox-frankenstein_draft" => "draft"
        case "ox-frankenstein_faircopy" => "faircopy"
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


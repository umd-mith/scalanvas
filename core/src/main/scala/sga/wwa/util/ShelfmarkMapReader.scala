package edu.umd.mith.sga.wwa.util

import scala.io.Source

trait ShelfmarkMapReader {
  private val stream = getClass.getResourceAsStream(
    "/edu/umd/mith/sga/wwa/shelfmark-map.txt"
  )

  private val Line = """^(duk|loc|mid|nyp)(\.[^-]+-\d\d\d\d)\s+([^;]+);\s(.+)$""".r

  private val source = Source.fromInputStream(stream)

  val shelfmarkMap: List[(String, (String, String))] =
    source.getLines.map {
      case Line(linePref, lineId, shelfmark, leaf) => linePref + lineId -> (shelfmark, leaf)
    }.toList
}


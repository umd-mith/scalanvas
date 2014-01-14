package edu.umd.mith.sga.frankenstein.util

import scala.io.Source

trait ShelfmarkMapReader {
  private val stream = getClass.getResourceAsStream(
    "/edu/umd/mith/sga/frankenstein/shelfmark-map.txt"
  )

  private val Line = """^(duk\.[^-]+-\d\d\d\d)\s+([^,]+),\s(.+)$""".r

  private val source = Source.fromInputStream(stream)

  val shelfmarkMap: List[(String, (String, String))] =
    source.getLines.map {
      case Line(lineId, shelfmark, leaf) => lineId -> (shelfmark, leaf)
    }.toList
}


package edu.umd.mith.sga.frankenstein.util

import scala.io.Source

trait ShelfmarkMapReader {
  private val stream = getClass.getResourceAsStream(
    "/edu/umd/mith/sga/frankenstein/shelfmark-map.txt"
  )

  private val Line = """^(ox-ms_abinger_c5\d[ab]?)-(\d\d\d\d)\s+([^,]+),\s(.+)$""".r

  private val source = Source.fromInputStream(stream)

  val shelfmarkMap: Map[String, Map[String, (String, String)]] =
    source.getLines.map {
      case Line(lineId, seq, shelfmark, leaf) =>
        lineId -> (seq -> (shelfmark, leaf))
    }.toList.groupBy(_._1).mapValues(
      _.groupBy(_._2._1).mapValues {
        case (_, (_, (shelfmark, leaf))) :: Nil => (shelfmark, leaf)
        case _ => throw new RuntimeException("Invalid shelfmark map!")
      }
    )
}


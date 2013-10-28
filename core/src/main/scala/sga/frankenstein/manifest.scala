package edu.umd.mith.sga.frankenstein

import edu.umd.mith.scalanvas.model.Service
import edu.umd.mith.sga.model.SgaManifest
import edu.umd.mith.sga.frankenstein.util.ShelfmarkMapReader

import java.io.File
import java.net.URI

trait FrankensteinManifest extends SgaManifest with ShelfmarkMapReader with TeiManager {
  this: FrankensteinConfiguration =>

  def resolvableDomain = "%sshelleygodwinarchive.org".format(
    if (development) "dev." else ""
  )

  def domain = "shelleygodwinarchive.org" 
  
  def base = new URI(
    "http://%s/data/ox".format(domain) 
  )

  def service = Some(
    Service(
      new URI("http://%s/sc/ox/%s".format(resolvableDomain, id))
    )
  )

  val title = "Frankenstein"

  def label = id match {
    case "ox-ms_abinger_c56" => "Draft Notebook A"
    case "ox-ms_abinger_c57" => "Draft Notebook B"
    case "ox-ms_abinger_c58a" => "Draft Notebook C1"
    case "ox-ms_abinger_c58b" => "Draft Notebook C2"
    case "ox-frankenstein_draft" => "Draft Manuscript in Final Sequence"
    case "ox-frankenstein_faircopy" => "Fair-Copy Manuscript in Final Sequence"
    case _ => throw new RuntimeException("Unknown identifier.")
  }

  override def state = Some(
    id match {
      case "ox-ms_abinger_c58a"
         | "ox-ms_abinger_c58b"
         | "ox-frankenstein_faircopy" => "Fair copy"
      case _ => "Draft"
    }
  )

  override def date = Some(
    id match {
      case "ox-ms_abinger_c56" => "[August or September]-[?December] 1816"
      case "ox-ms_abinger_c57" => "[?December] 1816-April 1817"
      case "ox-ms_abinger_c58a" => "18 April-[?13] May 1817"
      case "ox-ms_abinger_c58b" => "18 April-[?13] May 1817"
      case "ox-frankenstein_draft" => "1816-1817"
      case "ox-frankenstein_faircopy" => "1817"
      case id => throw new RuntimeException(s"Unknown identifier: $id!")
    }
  )

  import FrankensteinManifest.IdWithSeq

  def toFileId(idWithSeq: String) = idWithSeq match {
    case IdWithSeq("c58a", seq) => "ox-ms_abinger_c58-%s".format(seq)
    case IdWithSeq("c58b", seq) => "ox-ms_abinger_c58-%04d".format(seq.toInt + 36)
    case other => other
  }

  def fromFileId(fileIdWithSeq: String) = fileIdWithSeq match {
    case IdWithSeq("c58", seq) if seq.toInt <= 36 => "ox-ms_abinger_c58a-%s".format(seq)
    case IdWithSeq("c58", seq) => "ox-ms_abinger_c58b-%04d".format(seq.toInt - 36)
    case other => other
  }

  override val agent = Some("Mary Shelley")
  override val attribution = Some("Bodleian Library, University of Oxford")
}

object FrankensteinManifest {
  val IdWithSeq = """ox-ms_abinger_([^-]+)-(\d\d\d\d)""".r
}


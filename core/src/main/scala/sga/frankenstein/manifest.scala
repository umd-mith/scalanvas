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
    case "ox-frankenstein-notebook_a" => "Draft Notebook A"
    case "ox-frankenstein-notebook_b" => "Draft Notebook B"
    case "ox-frankenstein-notebook_c1" => "Fair-Copy Notebook C1"
    case "ox-frankenstein-notebook_c2" => "Fair-Copy Notebook C2"
    case "ox-frankenstein-volume_i" => "Volume I Draft in Chapter Sequence"
    case "ox-frankenstein-volume_ii" => "Volume I Draft in Chapter Sequence"
    case "ox-frankenstein-volume_iii" => "Voume III Fair Copy in Chapter Sequence"
    case _ => throw new RuntimeException("Unknown identifier.")
  }

  override def state = Some(
    id match {
      case "ox-frankenstein-notebook_c1"
         | "ox-frankenstein-notebook_c2"
         | "ox-frankenstein-volume_iii" => "Fair copy"
      case _ => "Draft"
    }
  )

  override def date = Some(
    id match {
      case "ox-frankenstein-notebook_a" => "[August or September]-[?December] 1816"
      case "ox-frankenstein-notebook_b" => "[?December] 1816-April 1817"
      case "ox-frankenstein-notebook_c1" => "18 April-[?13] May 1817"
      case "ox-frankenstein-notebook_c2" => "18 April-[?13] May 1817"
      case "ox-frankenstein-volume_i" => "1816-1817"
      case "ox-frankenstein-volume_ii" => "1816-1817"
      case "ox-frankenstein-volume_iii" => "1817"
      case id => throw new RuntimeException(s"Unknown identifier: $id!")
    }
  )

  import FrankensteinManifest.IdWithSeq

  override val agent = Some("Mary Shelley")
  override val attribution = Some("Bodleian Library, University of Oxford")
}

object FrankensteinManifest {
  val IdWithSeq = """ox-ms_abinger_([^-]+)-(\d\d\d\d)""".r
}


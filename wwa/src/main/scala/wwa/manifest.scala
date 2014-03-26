package edu.umd.mith.wwa

import edu.umd.mith.scalanvas.model.Service
import edu.umd.mith.wwa.model.WwaManifest
import edu.umd.mith.wwa.util.ShelfmarkMapReader

import java.io.File
import java.net.URI

trait MarginaliaManifest extends WwaManifest with ShelfmarkMapReader with TeiManager {
  this: WwaConfiguration =>

  def resolvableDomain = "%sshelleygodwinarchive.org".format(
    if (development) "dev." else ""
  )

  def domain = "shelleygodwinarchive.org" 
  
  def base = new URI(
    "http://%s/data/ox".format(domain) 
  )

  // val ServicePattern = """ox-wwa-([^_]+)_(.+)""".r
  val ServicePattern = """(duk|loc)\.(\d+)""".r

  def service = Some(
    Service(
      id match {
        case ServicePattern(group, item) =>
          new URI(
            "http://%s/sc/oxford/wwa/%s/%s".format(
              resolvableDomain,
              group,
              item
            )
          )
      }
    )
  )

  val title = "Wwa"

  def label = id match {
    case "duk.00055" => "Lessing's Laocoön."
    case "duk.00200" => "Bunsen"
    case _ => throw new RuntimeException("Unknown identifier.")
  }

  override val state = Some("...")

  override def date = Some(
    id match {
      case "duk.00055" => "dummy date"
      case "duk.00200" => "dummy date"
      case id => throw new RuntimeException(s"Unknown identifier: $id!")
    }
  )

  import MarginaliaManifest.IdWithSeq

  override val agent = Some("...")
  override val attribution = Some("...")
}

object MarginaliaManifest {
  val IdWithSeq = """duk\.([^-]+)-(\d\d\d\d)""".r
}

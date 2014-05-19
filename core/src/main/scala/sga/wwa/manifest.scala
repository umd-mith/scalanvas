package edu.umd.mith.sga.wwa

import edu.umd.mith.scalanvas.model.Service
import edu.umd.mith.sga.model.SgaManifest
import edu.umd.mith.sga.wwa.util.ShelfmarkMapReader

import java.io.File
import java.net.URI

trait WwaManifest extends SgaManifest with ShelfmarkMapReader with TeiManager {
  this: WwaConfiguration =>

  def resolvableDomain = "%sshelleygodwinarchive.org".format(
    if (development) "dev." else ""
  )

  def domain = "shelleygodwinarchive.org" 
  
  def base = new URI(
    "http://%s/data/ox".format(domain) 
  )

  val ServicePattern = """(duk|loc|mid|nyp)\.(\d+)""".r

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


  def title = parseHeaderFile(id+"-header").fullTitle.get

  override def label = parseHeaderFile(id+"-header").fullTitle.get

  override val state = Some("marginalia")

  override def date = parseHeaderFile(id+"-header").date

  import WwaManifest.IdWithSeq

  override def agent = parseHeaderFile(id+"-header").agent
  override def attribution = parseHeaderFile(id+"-header").attribution

  override def wwaShelfmark = parseHeaderFile(id+"-header").wwaShelfmark
  override def wwaId = parseHeaderFile(id+"-header").wwaId
  override def editors = parseHeaderFile(id+"-header").editors
  override def bibSources = parseHeaderFile(id+"-header").bibSources

}

object WwaManifest {
  val IdWithSeq = """(duk|loc|mid|nyp)\.([^-]+)-(\d{4}|header)""".r
}


package edu.umd.mith.sga.wwa

import com.typesafe.config.ConfigFactory
import edu.umd.mith.scalanvas.model.Service

import java.io.File
import java.net.URI

trait WwaConfiguration { this: WwaManifest =>
  def development: Boolean = false
  def constructReadingUri(idWithSeq: String): URI
  def imageService: Option[Service]

  def teiDir = new File(ConfigFactory.load.getString("texts.local.wwa"))

  // The primary facsimile images are JPEG 2000 files served from Djatoka.
  // This value will need to be overriden for the static fallback manifests.
  def imageFormat: String = "image/jp2"

  // The image dimensions in the TEI files are based on the full TIFF images
  // that were delivered to MITH last year, which include a 200 pixel footer
  // with copyright information. The Bodleian's Djatoka server does not
  // include the footer. 
  def adjustDimensions(w: Int, h: Int): (Int, Int) = (w, h - 200)

  // This should be the identifier for any Djatoka server, so I'm including
  // it here as a default, but it will need to be overridden in some cases.
  def constructImageUri(idWithSeq: String) = new URI(
    "http://spacely.unl.edu/waimages/%s".format(idWithSeq)
  )
}

trait DevelopmentConfiguration { this: WwaConfiguration =>
  override def development = true
}

trait BodleianImages { this: WwaConfiguration =>
  def imageService = Some(
    Service(
      new URI("http://spacely.unl.edu:8080/adore-djatoka/resolver"),
      Some(new URI("http://sourceforge.net/projects/djatoka/"))
    )
  )
}

trait MithDjatokaImages { this: WwaConfiguration =>
  def imageService = Some(
    Service(
      new URI("http://sga.mith.org:8080/adore-djatoka/resolver"),
      Some(new URI("http://sourceforge.net/projects/djatoka/"))
    )
  )

  override def constructImageUri(idWithSeq: String) = new URI(
    "%s".format(idWithSeq)
  )

  override def adjustDimensions(w: Int, h: Int): (Int, Int) = (w, h)
}

trait MithStaticImages { this: WwaConfiguration =>
  def imageService = None
  override def constructImageUri(idWithSeq: String) = new URI(
    "/demo/images/wwa/%s.jpg".format(idWithSeq)
  )

  override def adjustDimensions(w: Int, h: Int): (Int, Int) = (w, h)
}

trait SgaTei { this: WwaConfiguration with WwaManifest =>
  def constructReadingUri(idWithSeq: String) = new URI(
    "http://%s/tei/readingTEI/html/%s.html".format(resolvableDomain, idWithSeq)
  )
}


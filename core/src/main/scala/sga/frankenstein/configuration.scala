package edu.umd.mith.sga.frankenstein

import edu.umd.mith.scalanvas.model.Service

import java.io.File
import java.net.URI

trait FrankensteinConfiguration { this: FrankensteinManifest =>
  def development: Boolean = true
  def teiDir: File
  def constructReadingUri(idWithSeq: String): URI
  def imageService: Option[Service]

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
    "http://shelleygodwinarchive.org/images/ox/%s.jp2".format(
      toFileId(idWithSeq)
    )
  )
}

trait BodleianImages { this: FrankensteinConfiguration =>
  def imageService = Some(
    Service(
      new URI("http://tiles2.bodleian.ox.ac.uk:8080/adore-djatoka/resolver"),
      Some(new URI("http://sourceforge.net/projects/djatoka/"))
    )
  )
}

trait SgaTei { this: FrankensteinConfiguration with FrankensteinManifest =>
  def constructReadingUri(idWithSeq: String) = new URI(
    "http://%s/tei/readingTEI/html/%s.html".format(
      resolvableDomain,
      toFileId(idWithSeq)
    )
  )
}

trait Cratylus { this: FrankensteinConfiguration =>
  val teiDir = new File("/home/travis/code/projects/sg-data/data/tei/ox/")
}


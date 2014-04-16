package edu.umd.mith.scalanvas.extensions

import java.io.File
import edu.umd.mith.scalanvas.Service
import edu.umd.mith.scalanvas.extensions._
import java.net.URI

trait FrankensteinConfiguration extends MithConfiguration {
  lazy val FrankensteinServicePattern = """ox-frankenstein_([^_]+)_(.+)""".r
  lazy val baseUri = new URI("http://shelleygodwinarchive.org/data/ox")

  def constructManifestLabel(titleText: String): String = titleText.split(", ")(1)
  def constructManifestTitle(titleText: String): String = titleText.split(", ")(0)

  def constructCanvasUri(id: String) = basePlus(f"/$id%s")
  def constructRangeUri(id: String, n: String) = basePlus(f"/$id%s/$n%s")
  def constructReadingUri(id: String) = new URI(
    f"http://shelleygodwinarchive.org/tei/readingTEI/html/$id%s.html"
  )

  def constructSourceUri(id: String) = new URI(
    f"http://shelleygodwinarchive.org/tei/ox/$id%s.xml"
  )

  def constructManifestService(id: String) =  Some(
    Service(
      id match {
        case FrankensteinServicePattern(group, item) =>
          new URI(
            "http://shelleygodwinarchive.org/sc/oxford/frankenstein/%s/%s".format(
              group,
              item
            )
          )
        case other =>
          new URI(
            f"http://shelleygodwinarchive.org/sc/oxford/$other%s"
          )
      }
    )
  )

  lazy val imageFormat = "image/jp2"
  lazy val imageService = Some(
    Service(
      new URI("http://tiles2.bodleian.ox.ac.uk:8080/adore-djatoka/resolver"),
      Some(new URI("http://sourceforge.net/projects/djatoka/"))
    )
  )
}

object FrankensteinDemo extends App {
  // The first command-line argument should be a space-separated list of file
  // paths.
  val filePaths = args(0).split("\\s").toList

  // Mix in the appropriate configuration here.
  val frankenstein = new MithStack(filePaths.map(new File(_))) with FrankensteinConfiguration

  // If there's only one argument, create all of the logical and physical
  // manifests.
  if (args.length == 1) {
    frankenstein.savePhysicalManifests()
    frankenstein.saveLogicalManifests()
  } else {
    // Otherwise only create the physical manifest for the specified file name.
    frankenstein.savePhysicalManifest(args(1))
  }
}


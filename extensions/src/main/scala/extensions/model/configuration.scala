package edu.umd.mith.scalanvas.extensions.model

import edu.umd.mith.scalanvas.model.Configuration
import java.net.URI

trait MithConfiguration extends Configuration {
  def adjustDimensions(w: Int, h: Int): (Int, Int) = (w, h)
  def constructManifestLabel(titleText: String): String
  def constructManifestTitle(titleText: String): String
  def constructSourceUri(id: String): URI
  def constructReadingUri(id: String): URI
}

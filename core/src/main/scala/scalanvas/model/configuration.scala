package edu.umd.mith.scalanvas.model

import java.net.URI

trait Configuration {
  def development: Boolean
  def constructReadingUri(id: String): URI
  def constructImageUri(id: String): URI
  def constructCanvasUri(id: String): URI
  def constructRangeUri(id: String): URI
  def imageService: Option[Service]
  def imageFormat: String
  def adjustDimensions(w: Int, h: Int): (Int, Int) = (w, h)
}


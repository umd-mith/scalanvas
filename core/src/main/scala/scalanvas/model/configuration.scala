package edu.umd.mith.scalanvas.model

import java.net.URI

trait Configuration {
  def baseUri: URI
  def development: Boolean
  def constructSourceUri(id: String): URI
  def constructImageUri(id: String): URI
  def constructCanvasUri(id: String): URI
  def constructRangeUri(id: String): URI
  def constructManifestService(id: String): Option[Service]
  def imageService: Option[Service]
  def imageFormat: String
}


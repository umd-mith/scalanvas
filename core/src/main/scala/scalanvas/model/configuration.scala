package edu.umd.mith.scalanvas.model

import java.net.URI

trait Configuration {
  def baseUri: URI
  def constructCanvasUri(id: String): URI
  def constructRangeUri(id: String, n: String): URI
  def constructManifestService(id: String): Option[Service]
  def imageService: Option[Service]
  def imageFormat: String

  def basePlus(path: String) = new URI(
    baseUri.getScheme,
    baseUri.getUserInfo,
    baseUri.getHost,
    baseUri.getPort,
    baseUri.getPath + path,
    baseUri.getQuery,
    baseUri.getFragment
  )
}


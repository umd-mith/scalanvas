package edu.umd.mith.scalanvas.extensions.model

trait MithMetadataLabeled {
  def shelfmark: Option[String] = None
  def folio: Option[String] = None
  def state: Option[String] = None
  def hand: Option[String] = None
}


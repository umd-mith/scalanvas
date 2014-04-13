package edu.umd.mith.scalanvas.extensions.model

import edu.umd.mith.scalanvas.model.{ MetadataLabeled, Motivation }

trait MithMetadataLabeled extends MetadataLabeled {
  def shelfmark: Option[String]
  def folio: Option[String]
  def state: Option[String]
  def hand: Option[String]
}

case object Reading extends Motivation
case object Source extends Motivation


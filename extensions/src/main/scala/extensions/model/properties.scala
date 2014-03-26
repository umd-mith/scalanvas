package edu.umd.mith.scalanvas.extensions.model

import edu.umd.mith.scalanvas.model.Motivation

trait MithMetadataLabeled {
  def shelfmark: Option[String] = None
  def folio: Option[String] = None
  def state: Option[String] = None
  def hand: Option[String] = None
}

case object Reading extends Motivation
case object Source extends Motivation


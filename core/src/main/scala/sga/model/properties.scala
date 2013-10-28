package edu.umd.mith.sga.model

import java.net.URI
import scala.xml.Elem

trait SgaMetadataLabeled {
  def shelfmark: Option[String] = None
  def folio: Option[String] = None
  def state: Option[String] = None
  def hand: Option[String] = None
}


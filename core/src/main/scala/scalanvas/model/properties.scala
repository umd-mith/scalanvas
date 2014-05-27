package edu.umd.mith.scalanvas.model

import java.net.URI

trait Resource {
  def muri: Option[URI]
}

trait LocatedResource extends Resource {
  def uri: URI
  def muri = Some(uri)
}

trait Rect {
  def width: Int
  def height: Int
}

trait Labeled {
  def label: String
}

trait Formatted {
  def format: String
}

trait HasRelatedService {
  def service: Option[Service]
}

trait Motivated {
  def motivation: Option[Motivation]
}

trait ContentsMotivated {
  def motivation: Option[Motivation]
}

trait MetadataLabeled {
  def agent: Option[String] = None
  def attribution: Option[String] = None
  def date: Option[String] = None  
  def bibSources: Option[String] = None
  def editors: Option[String] = None
}

trait WWAMetadataLabeled  extends MetadataLabeled {
  def fullTitle: Option[String] = None
  def wwaShelfmark: Option[String] = None
  def wwaId: Option[String] = None  
}

sealed trait Motivation
case object Painting extends Motivation
case object Reading extends Motivation
case object Source extends Motivation


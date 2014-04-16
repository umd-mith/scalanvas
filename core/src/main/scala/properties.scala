package edu.umd.mith.scalanvas

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
  def agent: Option[String]
  def attribution: Option[String]
  def date: Option[String]
}

trait Motivation
case object Painting extends Motivation


package edu.umd.mith.util.xml.tei

import scales.xml.Elem

case class MissingEndPointError(elem: Elem) extends Exception(
  f"Missing end point for an ${elem.name.local}%s!"
)

case class MissingAnchorError(id: String) extends Exception(
  f"Missing anchor: $id%s!"
)


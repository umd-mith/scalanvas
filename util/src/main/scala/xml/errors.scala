package edu.umd.mith.util.xml

case class MissingElementError(idRef: String) extends Exception(
  f"Expected to find an element for identifier $idRef%s!"
)

case class MissingAttributeError(name: String) extends Exception(
  f"Expected attribute $name%s!"
)

case class MissingXmlIdError(name: String) extends Exception(
  f"Expected to find xml:id on $name%s!"
)

class MissingOffsetError(msg: String) extends Exception

case class MissingBeginningOffsetError(name: String) extends MissingOffsetError(
  f"Expected to find beginning character offset on $name%s!"
)

case class MissingEndingOffsetError(name: String) extends MissingOffsetError(
  f"Expected to find ending character offset on $name%s!"
)


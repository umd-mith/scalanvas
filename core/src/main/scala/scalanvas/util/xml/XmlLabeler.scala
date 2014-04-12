package edu.umd.mith.scalanvas.util.xml

import scala.collection.immutable.IndexedSeq
import scalaz.std.anyVal._, scalaz.syntax.show._, scalaz.syntax.std.indexedSeq._
import scales.utils.collection.Tree
import scales.xml._, ScalesXml._

object XmlLabeler {
  def addCharOffsets(doc: Doc): Doc = doc.copy(
    rootElem = process(0, doc.rootElem)._2.getRight
  )

  private[this] def range(begin: Int, end: Int): List[Attribute] = List(
    beginOffset -> begin.shows,
    endOffset -> end.shows
  )

  private[this] def itemLength(item: XmlItem) = item match {
    case Comment(_)   => 0
    case PI(_, _)     => 0
    case Text(value)  => value.length
    case CData(value) => value.length
  }

  private[this] def processTree(i: Int)(tree: XmlTree) = tree match {
    case Tree(elem, children) =>
      val (j, processed) = children.to[IndexedSeq].mapAccumLeft(i, process)

      val tree = Tree(
        elem.copy(attributes = elem.attributes ++ range(i, j)),
        processed.to[XCC]
      )

      (j, tree)
  }

  private[this] def process(i: Int, node: ItemOrElem): (Int, ItemOrElem) =
    node.fold(item => (i + itemLength(item), item), processTree(i))
}


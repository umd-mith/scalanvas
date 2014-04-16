package edu.umd.mith.util.xml

import scales.utils.top
import scales.xml._, ScalesXml._

package object tei {
  import edu.umd.mith.util.xml.implicits._

  val teiNs = Namespace("http://www.tei-c.org/ns/1.0") 

  object teiAttrs {
    val elemType = NoNamespaceQName("type")
    val hand = NoNamespaceQName("hand")
    val newHand = NoNamespaceQName("new")
    val place = NoNamespaceQName("place")
    val spanTo = NoNamespaceQName("spanTo")
  }

  object implicits {
    implicit class TeiElem(val tree: XmlTree) extends AnyVal {
      def namedElemFor(name: String)(id: String): Option[XmlPath] =
        (top(tree) \\* teiNs(name) withId id.tail).\^.one.headOption

      def anchorFor(id: String): Option[XmlPath] = namedElemFor("anchor")(id)
      def surfaceFor(id: String): Option[XmlPath] = namedElemFor("surface")(id)
      def msItemFor(id: String): Option[XmlPath] = namedElemFor("msItem")(id)

      def allElems(name: String): List[Elem] = 
        (top(tree) \\* teiNs(name)).map(_.tree.section).toList
    }
  }
}


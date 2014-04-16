package edu.umd.mith.util

import java.io.File
import org.xml.sax.InputSource
import scalaz.{ Nondeterminism, Validation }, scalaz.syntax.std.option._, scalaz.syntax.std.string._
import scalaz.concurrent.Task
import scales.utils.Equiv
import scales.xml._, ScalesXml._
import scales.xml.ScalesXml.defaultVersion

package object xml {
  val xmlIdAttr = Namespace.xml("id")
  val utilNs = Namespace("http://mith.umd.edu/util/ns1#").prefixed("mu")
  val mithNs = Namespace("http://mith.umd.edu/sc/ns1#").prefixed("mith")
  val beginOffset = utilNs("b")
  val endOffset = utilNs("e")

  def loadFile(file: File): Task[(String, Doc)] = Task {
    val systemId = file.toURI.toString
    val source = new InputSource(systemId)

    (systemId, loadXml(source = source, parsers = XIncludeSAXParserFactoryPool.parsers))
  }

  def loadFiles(files: List[File]): Task[Map[String, Doc]] = 
    Nondeterminism[Task].gatherUnordered(files map loadFile).map(_.toMap)

  def attrText[PT <: Iterable[XmlPath]](paths: AttributePaths[PT]): Option[String] =
    paths.one.headOption.map(_.attribute.value)

  def elemText[PT <: Iterable[XmlPath]](path: XPath[PT]): Option[String] =
    path.\+.text.one.headOption.map(_.item.value)

  def attrsText[PT <: Iterable[XmlPath]](paths: AttributePaths[PT]): List[String] =
    paths.attributes.map(_.attribute.value).toList

  def elemsText[PT <: Iterable[XmlPath]](path: XPath[PT]): List[String] =
    path.\+.text.map(_.item.value).toList

  object implicits {
    implicit class RichElem(val elem: Elem) extends AnyVal {
      def beginningOffset: Validation[Throwable, Int] =
        elem.attributes(beginOffset).map(_.value).toSuccess(
          MissingBeginningOffsetError(elem.name.local)
        ).flatMap(_.parseInt)

      def endingOffset: Validation[Throwable, Int] =
        elem.attributes(endOffset).map(_.value).toSuccess(
          MissingEndingOffsetError(elem.name.local)
        ).flatMap(_.parseInt)

      def xmlId: Validation[Throwable, String] =
        elem.attributes(xmlIdAttr).map(_.value).toSuccess(
          MissingXmlIdError(elem.name.local)
        )
    }

    implicit class RichXPath[PT <: Iterable[XmlPath]](val xpath: XPath[PT]) extends AnyVal {
      def withId(id: String): AttributePaths[PT] = xpath \@ xmlIdAttr === id

      def beginningOffset: Validation[Throwable, Int] =
        attrText(xpath \@ beginOffset).toSuccess(
          MissingBeginningOffsetError(xpath.toString)
        ).flatMap(_.parseInt)

      def endingOffset: Validation[Throwable, Int] =
        attrText(xpath \@ endOffset).toSuccess(
          MissingEndingOffsetError(xpath.toString)
        ).flatMap(_.parseInt)
    }

    implicit class RichXmlPath(val xpath: XmlPath) extends AnyVal {
      def getAttribute(name: String): Validation[Throwable, String] =
        (xpath \@ NoNamespaceQName(name)).one.headOption.map(_.attribute.value).toSuccess(
          MissingAttributeError(name)
        )

      def getQAttribute(name: AttributeQName): Validation[Throwable, String] =
        (xpath \@ name).one.headOption.map(_.attribute.value).toSuccess(
          MissingAttributeError(name.toString)
        )

      def root: XmlPath = xpath.ancestor_::.head

      def beginningOffset: Validation[Throwable, Int] =
        attrText(xpath \@ beginOffset).toSuccess(
          MissingBeginningOffsetError(xpath.toString)
        ).flatMap(_.parseInt)

      def endingOffset: Validation[Throwable, Int] =
        attrText(xpath \@ endOffset).toSuccess(
          MissingEndingOffsetError(xpath.toString)
        ).flatMap(_.parseInt)
    }

    /*implicit class RichAttributes(val attributes: Attributes) extends AnyVal {
      def get[B, C](b: B)(implicit
        equiv: Equiv[C],
        viewA: Attribute => C,
        viewB: B => C
      ): Validation[Throwable, String] =
        attributes(b)(equiv, viewA, viewB).map(_.value).toSuccess(
          MissingAttributeError(b.toString)
        )
    }*/
  }
}


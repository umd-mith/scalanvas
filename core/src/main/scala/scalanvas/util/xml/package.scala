package edu.umd.mith.scalanvas.util

import java.io.File
import org.xml.sax.InputSource
import scalaz.{ Nondeterminism, Validation }, scalaz.syntax.std.option._, scalaz.syntax.std.string._
import scalaz.concurrent.Task
import scales.utils.Equiv
import scales.xml._, ScalesXml._
import scales.xml.ScalesXml.defaultVersion

package object xml {
  val xmlIdAttr = Namespace.xml("id")
  val utilNs = Namespace("http://mith.umd.edu/util/1#").prefixed("mu")
  val beginOffset = utilNs("b")
  val endOffset = utilNs("e")

  def loadFile(file: File): Task[(String, Doc)] = Task {
    val systemId = file.toURI.toString
    val source = new InputSource(systemId)

    (systemId, loadXml(source = source, parsers = XIncludeSAXParserFactoryPool.parsers))
  }

  def loadFiles(files: List[File]): Task[Map[String, Doc]] = 
    Nondeterminism[Task].gatherUnordered(files map loadFile).map(_.toMap)

  object implicits {
    implicit class OffsetElem(val elem: Elem) extends AnyVal {
      def beginningOffset: Validation[Throwable, Int] =
        elem.attributes(beginOffset).map(_.value).toSuccess(
          MissingBeginningOffsetError(elem.name.local)
        ).flatMap(_.parseInt)

      def endingOffset: Validation[Throwable, Int] =
        elem.attributes(endOffset).map(_.value).toSuccess(
          MissingEndingOffsetError(elem.name.local)
        ).flatMap(_.parseInt)
    }

    implicit class RichElem(val elem: Elem) extends AnyVal {
      def xmlId: Validation[Throwable, String] =
        elem.attributes(xmlIdAttr).map(_.value).toSuccess(
          MissingXmlIdError(elem.name.local)
        )
    }

    implicit class RichXPath[PT <: Iterable[XmlPath]](val xpath: XPath[PT]) {
      def withId(id: String): AttributePaths[PT] = xpath \@ xmlIdAttr === id
    }

    implicit class RichAttributes(val attributes: Attributes) {
      def get[B, C](b: B)(implicit
        equiv: Equiv[C],
        viewA: Attribute => C,
        viewB: B => C
      ): Validation[Throwable, String] =
        attributes(b)(equiv, viewA, viewB).map(_.value).toSuccess(
          MissingAttributeError(b.toString)
        )
    }
  }
}


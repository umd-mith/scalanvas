package edu.umd.mith.scalanvas.extensions.parsing

import edu.umd.mith.scalanvas.parsing._
import edu.umd.mith.scalanvas.extensions.model.{ MithCanvas, MithConfiguration }
import edu.umd.mith.scalanvas.model.{ Canvas, ImageForPainting, Link }
import edu.umd.mith.scalanvas.util.concurrent._
import edu.umd.mith.scalanvas.util.xml._
import edu.umd.mith.scalanvas.util.xml.implicits._
import edu.umd.mith.scalanvas.util.xml.tei._
import edu.umd.mith.scalanvas.util.xml.tei.implicits._
import java.net.URI
import scalaz._, Scalaz._
import scalaz.concurrent._
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

trait MithCanvasParser extends CanvasParser[MithCanvas] { this: MithTeiCollection with MithConfiguration =>
  def parseSurfaceId(surface: XmlPath): Throwable \/ String =
    (surface \@ xmlIdAttr).one.headOption.map(_.attribute.value).toRightDisjunction(
      MissingXmlIdError("surface")
    )

  def findStatus(msItem: XmlPath): Option[String] =
    (msItem.ancestor_or_self_:: \* teiNs("bibl") \@ NoNamespaceQName("status")).toList.lastOption.map(
      _.attribute.value
    )

  def findHandAbbrevs(surface: XmlPath): List[String] = {
    val hands = (surface \\@ NoNamespaceQName("hand")).attributes.map(_.attribute.value)
    val shifts = (surface \\* teiNs("line")).toList.lastOption.toList.flatMap { lastLine =>
      val preceding = lastLine.preceding_::.toList
      val (before, after) = preceding.span(_ == surface)
      before.filter(p => !p.isItem && p.tree.section.name == teiNs("handShift")) ++
        after.find(p => !p.isItem && p.tree.section.name == teiNs("handShift"))
    }

    val shiftHands = shifts.flatMap(shift =>
      (shift \@ NoNamespaceQName("new")).one.headOption.map(_.attribute.value)
    )

    (hands ++ shiftHands).toList.distinct
  }

  def parseCanvas(doc: CollectionDoc)(surface: XmlPath): Task[MithCanvas] = {
    val elem = surface.tree.section
    val root = surface.root

    val seq = (root \\* teiNs("surface")).toList.indexOf(surface) + 1
    val handNames = findHandAbbrevs(surface).map(_.tail).flatMap(lookupHandName(doc))

    val msItems: Throwable \/ List[XmlPath] =
      surface.getAttribute("partOf").toOption.toList.flatMap(_.split("\\s")).traverseU(idRef =>
        resolveIdRef(doc)(idRef).toRightDisjunction(
          new Exception(f"partOf value $idRef%s does not resolve in ${ doc.fileName }%s.")
        )
      )

    new Task(
      Future.now(
        for {
          id <- elem.xmlId.disjunction
          canvasUri <- parseSurfaceId(surface).map(constructCanvasUri)
          lrx <- surface.getAttribute("lrx").flatMap(_.parseInt).disjunction
          lry <- surface.getAttribute("lry").flatMap(_.parseInt).disjunction
          sm <- surface.getQAttribute(mithNs("shelfmark")).disjunction
          fo <- surface.getQAttribute(mithNs("folio")).disjunction
          offset <- elem.beginningOffset.disjunction
          ranges <- msItems
        } yield new MithCanvas {
          val uri = canvasUri
          val shelfmark = Some(sm)
          val folio = Some(fo)
          val label = f"$sm%s, $fo"
          val (width, height) = adjustDimensions(lrx, lry)
          val service = None
          val transcription = Some(surface)
          val images = List(
            ImageForPainting(
              constructImageUri(id),
              width,
              height,
              imageFormat,
              imageService
            )
          )
          val reading = Some(Link(constructReadingUri(id), "text/html"))
          val source = Some(Link(constructSourceUri(id), "application/tei+xml"))

          val state = {
            val states = ranges.flatMap(findStatus).map(expandStateName)

            if (states.isEmpty) None else Some(states.distinct.mkString(" and "))
          }

          val date = {
            val dates = ranges.flatMap(nearestDate)

            if (dates.isEmpty) None else Some(dates.mkString(" and "))
          }

          val agent = {
            val agents = ranges.flatMap(nearestAgent)

            if (agents.isEmpty) None else Some(agents.mkString(" and "))
          }

          val hand = if (handNames.isEmpty) None else Some(handNames.mkString(" and "))
          val attribution = topLevelRepository(doc)
        }
      )
    )
  }
}


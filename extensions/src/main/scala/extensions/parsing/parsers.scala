package edu.umd.mith.scalanvas.extensions.parsing

import edu.umd.mith.scalanvas.parsing._
import edu.umd.mith.scalanvas.extensions.model.MithCanvas
import edu.umd.mith.scalanvas.model.{ Canvas, Configuration, ImageForPainting, Link, Range }
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

object MithTeiCollection {
  def createHandNames(docs: Map[String, CollectionDoc]) = {
    docs.values.foldLeft(Map.empty[(String, String), String]) {
      case (map, CollectionDoc(doc, _, fileName)) =>
        (top(doc) \\* teiNs("handDesc") \* teiNs("handNote") \* teiNs("persName")).foldLeft(map) {
          case (map, persName) =>
            map.updated((fileName, text(persName.\^.\@(xmlIdAttr))), text(persName))
        }
    }
  }
}

trait MithTeiCollection extends TeiCollection {
  def handNames: Map[(String, String), String]

  // Abbreviation should not include the pound sign.
  def lookupHandName(doc: CollectionDoc)(abbrev: String) =
    handNames.get((doc.fileName, abbrev))
}

trait MithCanvasParser extends CanvasParser[MithCanvas] { this: MithTeiCollection with Configuration =>
  def parseSurfaceId(surface: XmlPath): Throwable \/ String =
    (surface \@ xmlIdAttr).one.headOption.map(_.attribute.value).toRightDisjunction(
      MissingXmlIdError("surface")
    )

  def findStatus(msItem: XmlPath): Option[String] =
    (msItem.ancestor_or_self_:: \* teiNs("bibl") \@ NoNamespaceQName("status")).toList.lastOption.map(
      _.attribute.value
    )

  def findHandAbbrevs(surface: XmlPath): Set[String] = {
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

    (hands ++ shiftHands).toSet
  }

  def parseCanvas(doc: CollectionDoc)(surface: XmlPath): Task[MithCanvas] = {
    val elem = surface.tree.section
    val root = surface.root

    val seq = (root \\* teiNs("surface")).toList.indexOf(surface) + 1
    val handNames = findHandAbbrevs(surface).map(_.tail).flatMap(lookupHandName(doc))

    val msItem: Option[XmlPath] =
      surface.getAttribute("partOf").toOption.flatMap(resolveIdRef(doc))

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
          val state = msItem.flatMap(findStatus).map {
            case "draft" => "Draft"
            case "fair_copy" => "Fair copy"
          }
          val hand = if (handNames.isEmpty) None else Some(handNames.mkString(" and "))
        }
      )
    )
  }
}

trait MithRangeParser[C <: Canvas] extends RangeParser[C] { this: CanvasParser[C] with TeiCollection with Configuration =>
  def parseMsItemId(msItem: XmlPath): Task[String] =
    (msItem \@ xmlIdAttr).one.headOption.map(_.attribute.value).toTask(
      MissingXmlIdError("msItem")
    )

  def parseRangeLabel(msItem: XmlPath): Task[String] =
    (msItem \* teiNs("bibl") \* teiNs("title")).\+.text.one.headOption.map(_.item.value).toTask(
      new Exception("Missing title on range msItem.")
    )

  def parseRanges(doc: CollectionDoc)(msItem: XmlPath): Task[List[Range[C]]] = {
    val parentId = parseMsItemId(msItem)

    (msItem \* teiNs("msItem")).toList.traverseU { child =>
      val surfaces = Nondeterminism[Task].gather(
        (
          child \*
          teiNs("locusGrp") \*
          teiNs("locus") \@
          NoNamespaceQName("target")
        ).flatMap(_.value.split("\\s")).map(idRef =>
          resolveIdRef(doc)(idRef).toTask(
            new Exception(f"Missing xml:id reference $idRef%s in ${ doc.fileName }%s.")
          ).flatMap(parseCanvas(doc))
        ).toSeq
      )

      (
        parentId.map(constructRangeUri) |@|
        parseRangeLabel(child) |@|
        surfaces
      )(Range.apply)
    }
  }
}


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

trait MithCanvasParser extends CanvasParser[MithCanvas] { this: TeiCollection with Configuration =>
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

  def parseCanvas(surface: XmlPath): Task[MithCanvas] = {
    val elem = surface.tree.section
    val root = surface.root

    val seq = (root \\* teiNs("surface")).toList.indexOf(surface) + 1
    val handNames = findHandAbbrevs(surface).map(_.tail).flatMap(handName(root))

    val msItem: Task[Option[XmlPath]] =
      surface.getAttribute("partOf").toOption.traverseU(resolveIdRef(root))

    msItem.map(_.flatMap(findStatus)).flatMap { status =>

    new Task(Future.now(for {
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
      val state = status.map {
        case "draft" => "Draft"
        case "fair_copy" => "Fair copy"
      }
      val hand = if (handNames.isEmpty) None else Some(handNames.mkString(" and "))
    }))
    }
  }
/*
      val images = List(
        ImageForPainting(
          constructImageUri(idWithSeq),
          w,
          h,
          imageFormat,
          imageService
        )
      )
      val reading = Some(Link(constructReadingUri(idWithSeq), "text/html"))
      val source = Some(Link(
        new URI(
          //"http://%s/tei/ox/%s.xml".format(
          "/demo/xml/%s.xml".format(
            //resolvableDomain,
            idWithSeq
          )
        ),
        "application/tei+xml"
      ))
      override val hand = Some(
        if (percyOnly) "Percy Shelley" else {
          if (fullId == "ox-ms_abinger_c58" && pageSeq == "0047")
            "Mary Shelley and Percy Shelley"
          else {
            if ((surface \\ "@hand").exists(_.text == "#pbs"))
              "Mary Shelley and Percy Shelley"
            else
              "Mary Shelley" 
          }
        }
      )

      override val shelfmark = Some(itemShelfmark)

      val service = None

      override val folio = Some(pageFolio)
    }
  }
}*/
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

  def parseRanges(msItem: XmlPath): Task[List[Range[C]]] = {
    val parentId = parseMsItemId(msItem)

    (msItem \* teiNs("msItem")).toList.traverseU { child =>
      val surfaces = Nondeterminism[Task].gather(
        (
          child \*
          teiNs("locusGrp") \*
          teiNs("locus") \@
          NoNamespaceQName("target")
        ).flatMap(_.value.split("\\s")).map(idRef =>
          resolveIdRef(msItem.root)(idRef).flatMap(parseCanvas)
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


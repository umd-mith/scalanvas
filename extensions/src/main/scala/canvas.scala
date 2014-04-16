package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas._
import edu.umd.mith.scalanvas.{ Canvas, ImageForPainting, Link }
import edu.umd.mith.util.concurrent._
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.implicits._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.util.xml.tei.implicits._
import java.net.URI
import scalaz._, Scalaz._
import scalaz.concurrent._
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

trait MithCanvasParser extends CanvasParser[MithCanvas] { this: MithTeiCollection with MithConfiguration =>
  def parseSurfaceId(surface: XmlPath): Throwable \/ String =
    attrText(surface \@ xmlIdAttr).toRightDisjunction(
      MissingXmlIdError("surface")
    )

  def findStatus(msItem: XmlPath): Option[String] =
    attrsText(msItem.ancestor_or_self_:: \* teiNs("bibl") \@ "status").lastOption

  def findHandAbbrevs(surface: XmlPath): List[String] = {
    val hands = attrsText(surface \\@ "hand")
    val handShiftsOnPage = attrsText(surface \\* teiNs("handShift") \@ "new")

    /*val firstHandShift: Option[XmlPath] = (surface.\\*(teiNs("handShift")).pos(1)).one.headOption

    val handShifts = for {
      firstHandShift <- (surface.\\*(teiNs("handShift")).pos(1)).one.headOption
      firstLine <- (surface.\\*(teiNs("line")).pos(1)).one.headOption
    } yield (
      for {
        firstHandShiftBeginning <- firstHandShift.beginningOffset.disjunction
        firstLineBeginning <- firstLine.beginningOffset.disjunction
        earlier <- if (firstHandShiftBeginning > firstLineBeginning) {
          attrText(surface.preceding_::.\*(teiNs("handShift")).pos(1).\@("new")).toRightDisjunction(
            new Exception("No appropriate earlier handshift for ${ surface.toString }%s.")
          ).map(_.some)
        } else none.right  
      } yield (hands ++ handShiftsOnPage ++ earlier.toList).distinct
    )*/
        
    println(lastHandShift(surface.\\*(teiNs("line")).pos(1)))
    (hands ++ handShiftsOnPage ++ lastHandShift(surface.\\*(teiNs("line")).pos(1)).toList).distinct
  }

  def parseCanvas(doc: CollectionDoc)(surface: XmlPath): Task[MithCanvas] = {
    val elem = surface.tree.section
    val root = surface.root

    val seq = (root \\* teiNs("surface")).toList.indexOf(surface) + 1

    val msItems: Throwable \/ List[XmlPath] =
      surface.getAttribute("partOf").toOption.toList.flatMap(_.split("\\s")).traverseU(idRef =>
        resolveIdRef(doc)(idRef).toRightDisjunction(
          new Exception(f"partOf value $idRef%s does not resolve in ${ doc.fileName }%s.")
        )
      )

    val imageUriAttr = attrText(surface \* teiNs("graphic") \@ "url")

    val handNames = findHandAbbrevs(surface).map(_.tail).flatMap(lookupHandName(doc))

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
          imageUri <- imageUriAttr.toRightDisjunction(
            new Exception(f"No graphic element for $id%s.")
          )
        } yield new MithCanvas {
          val uri = canvasUri
          val shelfmark = Some(sm)
          val folio = Some(fo)
          val label = f"$sm%s, $fo"
          val (width, height) = adjustDimensions(lrx, lry)
          val service = None
          val transcription = Some((doc, surface))
          val images = List(
            ImageForPainting(
              new URI(imageUri),
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


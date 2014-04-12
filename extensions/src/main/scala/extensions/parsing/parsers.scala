package edu.umd.mith.scalanvas.extensions.parsing

import edu.umd.mith.scalanvas.parsing._
import edu.umd.mith.scalanvas.extensions.model.MithCanvas
import edu.umd.mith.scalanvas.model.{ Canvas, Configuration, Range }
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
  def parseSurfaceId(surface: XmlPath): Task[String] =
    (surface \@ xmlIdAttr).one.headOption.map(_.attribute.value).toTask(
      MissingXmlIdError("surface")
    )

  def parseCanvas(surface: XmlPath): Task[MithCanvas] = {
    val elem = surface.tree.section

    val uri = parseSurfaceId(surface).map(constructCanvasUri)
    val seq = (surface.root \\* teiNs("surface")).toList.indexOf(surface) + 1
    val id = elem.xmlId
    val lrx = surface.getAttribute("lrx").map(_.parseInt)
    val lry = surface.getAttribute("lry").map(_.parseInt)
    val partOf = surface.getAttribute("partOf")

    val offset = elem.beginningOffset

    //(id |@| lrx |@| lry |@| )((id, w, h) =>

    //new MithCanvas {
    
/*
    new MithCanvas {
      val uri = basePlus("/%s/canvas/%s".format(fullId, pageSeq))
      val seq = pageSeq
      // Awful hack coming...
      val label = s"${ itemShelfmark.drop(12) }, $pageFolio"
      val width = w
      val height = h
      val images = List(
        ImageForPainting(
          constructImageUri(idWithSeq),
          w,
          h,
          imageFormat,
          imageService
        )
      )
      val transcription = Some(surface)
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

    ???
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


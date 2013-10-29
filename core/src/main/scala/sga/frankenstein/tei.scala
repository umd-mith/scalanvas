package edu.umd.mith.sga.frankenstein

import edu.umd.mith.scalanvas.model.{ ImageForPainting, Link }
import edu.umd.mith.scalanvas.util.xml.XmlLabeler
import edu.umd.mith.sga.model.SgaCanvas

import java.io.File
import scala.xml.XML
import java.net.URI

trait TeiManager {
  self: FrankensteinManifest with FrankensteinConfiguration =>
  def parseTeiFile(idWithSeq: String): SgaCanvas = {
    import FrankensteinManifest.IdWithSeq

    val (fullId, pageSeq) = idWithSeq match {
      case IdWithSeq(itemId, seq) => ("ox-ms_abinger_" + itemId, seq)
      case itemIdWithSeq => throw new RuntimeException(
        s"Invalid identifier: $itemIdWithSeq!"
      )
    }

    val (itemShelfmark, pageFolio) = shelfmarkMap(fullId)(pageSeq)

    val file = new File(teiDir, toFileId(idWithSeq) + ".xml")
    val surface = XmlLabeler.addCharOffsets(XML.loadFile(file))
    val attrs = surface.attributes.asAttrMap
    val _uri = basePlus("/%s/canvas/%s".format(fullId, pageSeq))

    val percyOnly =
      (surface \\ "handShift").toList.exists(
        _.attributes.asAttrMap.get("new").exists(_ == "#pbs")
      ) && ! _uri.toString.endsWith("ox-ms_abinger_c58b/canvas/0011")


    val (w, h) = adjustDimensions(attrs("lrx").toInt, attrs("lry").toInt) 
 
    new SgaCanvas {
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
      val transcription = surface
      val reading = Link(constructReadingUri(idWithSeq), "text/html")
      val source = Link(
        new URI(
          "http://%s/tei/ox/%s.xml".format(
            resolvableDomain,
            toFileId(idWithSeq)
          )
        ),
        "application/tei+xml"
      )
      override val hand = Some(
        if (percyOnly) "Percy Shelley" else {
          if (_uri.toString.endsWith("ox-ms_abinger_c58b/canvas/0011"))
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
}

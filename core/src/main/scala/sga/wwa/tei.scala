package edu.umd.mith.sga.wwa

import com.typesafe.config.ConfigFactory
import edu.umd.mith.scalanvas.model.{ ImageForPainting, Link, WWAMetadataLabeled }
import edu.umd.mith.scalanvas.util.xml.XmlLabeler
import edu.umd.mith.sga.model.SgaCanvas

import java.io.File
import scala.xml.XML
import java.net.URI

trait TeiManager {
  self: WwaManifest with WwaConfiguration =>

  def parseHeaderFile(idWithSeq: String): WWAMetadataLabeled = {
    import WwaManifest.IdWithSeq

    val (pref, fullId, pageSeq) = idWithSeq match {
      case IdWithSeq(pref, itemId, seq) => (pref, itemId, seq)
      case itemIdWithSeq => throw new RuntimeException(
        s"Invalid identifier: $itemIdWithSeq!"
      )
    }

    val file = new File(teiDir, idWithSeq + ".xml")
    val header = XmlLabeler.addCharOffsets(XML.loadFile(file))
    
    // println(header)

    new WWAMetadataLabeled {

      def formatDate(date: scala.xml.Node): String = {
        if (date.nonEmptyChildren.length > 0) {
          date.text
        } else {
          val keys = date.attributes.asAttrMap.keySet
          var formatted = ""
          date.attributes.asAttrMap.foreach { case (k,v) =>
            k match {
              case "when" => v
              case "notBefore" => 
                if (keys.contains("notAfter")) {
                  formatted = "not before %s and not after %s".format(v, date.attributes.get("notAfter").get)
                }
              case "notAfter" => 
                if (!keys.contains("notBefore")) {
                  formatted = "not after %s".format(v)
                }
              case _ => None
            }
          }
          return formatted
        }
      }
      
      def formatBib(bib: scala.xml.Node): String = {
        val au = (bib \\ "author").text + ", "
        val title = (bib \\ "title").map(_.text).toList mkString " "
        val pub = if ((bib \\ "publisher").length > 0) (bib \\ "publisher").text + ", " else ""
        val pubplace = if ((bib \\ "pubPlace").length > 0) (bib \\ "pubPlace").text + " " else ""
        val date = (bib \\ "date").toList.map(formatDate(_)) mkString "; "

        au + title + ", " + pub + pubplace + " (" + date + ")"

      }

      override val agent = Some((header \\ "sourceDesc" \\ "bibl" \\ "author").toList.map(
        x => x.text) mkString ", ")
      override val attribution = Some((header \\ "sourceDesc" \\ "bibl" \\ "orgName").text)
      override val date = Some((header \\ "sourceDesc" \\ "bibl" \\ "date").toList.map(formatDate(_)) mkString "; ")
      override val wwaShelfmark = Some((header \\ "sourceDesc" \\ "bibl" \\ "idno").text)
      override val wwaId = Some((header \\ "sourceDesc" \\ "publicationStmt" \\ "idno").text)
      override val bibSources = Some((header \\ "sourceDesc" \\ "bibl").toList.map(formatBib(_)).toList mkString " ")
      override val editors = Some((header \\ "fileDesc" \\ "notesStmt" \\ "persName").toList.map(
        x => x.text) mkString ", ")
      override val fullTitle = Some(
        (header \\ "fileDesc" \\ "titleStmt" \\ "title").filter(
            _.attributes.asAttrMap("level") == "m"
          ).filter(
            _.attributes.asAttrMap("type") == "main"
          ).text
        )
    }

  }

  def parseTeiFile(idWithSeq: String, itemShelfmark: String, pageFolio: String): SgaCanvas = {
    import WwaManifest.IdWithSeq

    val (pref, fullId, pageSeq) = idWithSeq match {
      case IdWithSeq(pref, itemId, seq) => (pref, itemId, seq)
      case itemIdWithSeq => throw new RuntimeException(
        s"Invalid identifier: $itemIdWithSeq!"
      )
    }

    val file = new File(teiDir, idWithSeq + ".xml")
    val surface = XmlLabeler.addCharOffsets(XML.loadFile(file))
    val attrs = surface.attributes.asAttrMap
    val _uri = basePlus("/%s/canvas/%s".format(fullId, pageSeq))

    val percyOnly =
      (surface \\ "handShift").toList.exists(
        _.attributes.asAttrMap.get("new").exists(_ == "#pbs")
      ) && !(fullId == "ox-ms_abinger_c58" && pageSeq == "0047")

    val (w, h) = adjustDimensions(attrs("lrx").toInt, attrs("lry").toInt) 
 
    new SgaCanvas {
      val uri = basePlus("/%s/canvas/%s".format(fullId, pageSeq))
      val seq = pageSeq
      // Awful hack coming...
      val label = s"${ itemShelfmark.drop(12) }, $pageFolio"
      val width = w
      val height = h
      val imgUrl = attrs.get("facs").map(_.toString).getOrElse("")
      val images = List(
        ImageForPainting(
          constructImageUri(imgUrl),
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
          "%s%s.xml".format(
            ConfigFactory.load.getString("texts.manifest.wwa"),
            idWithSeq
          )
        ),
        "application/tei+xml"
      )
      override val hand = Some("Walter Whitman")

      override val shelfmark = Some(itemShelfmark)

      val service = None

      override val folio = Some(pageFolio)
    }
  }
}
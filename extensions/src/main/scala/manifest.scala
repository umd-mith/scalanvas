package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas._
import edu.umd.mith.util.concurrent._
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.implicits._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.util.xml.tei.implicits._
import scalaz._, Scalaz._
import scalaz.concurrent._
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

trait MithPhysicalManifestParser {
  this: MithCanvasParser with MithTeiCollection with MithConfiguration =>

  def parsePhysicalManifest(doc: CollectionDoc): Task[MithPhysicalManifest] = {
    val msItem = (top(doc.doc) \\* teiNs("msContents") \* teiNs("msItem")).one.headOption

    val manifestState = msItem.flatMap(item =>
      (item \* teiNs("bibl") \@ NoNamespaceQName("status")).one.headOption.map(
        attr => expandStateName(text(attr))
      )
    )

    for {
      allCanvases <- (top(doc.doc) \\* teiNs("surface")).toList.traverseU(parseCanvas(doc))
      manifestId <- topLevelXmlId(doc).toTask(new Exception(f"No xml:id on TEI element for ${ doc.fileName }%s"))
      manifestTitle <- topLevelTitle(doc).toTask(new Exception(f"No title element for ${ doc.fileName }%s"))
    } yield new MithPhysicalManifest {
      val id = manifestId
      val base = baseUri
      val title = constructManifestTitle(manifestTitle)
      val label = constructManifestLabel(manifestTitle)
      val service = constructManifestService(id)
      val canvases = allCanvases
      val state = manifestState
      val folio = None
      val hand = None
      val shelfmark = topLevelShelfmark(doc)
      val attribution = topLevelRepository(doc)
      val date = msItem.flatMap(nearestDate)
      val agent = msItem.flatMap(nearestAgent)
    }
  }
}

trait MithLogicalManifestParser {
  this: MithCanvasParser with MithRangeParser[MithCanvas] with MithTeiCollection with MithConfiguration =>

  def parseLogicalManifest(doc: CollectionDoc)(msItem: XmlPath): Task[MithLogicalManifest] = {
    val id = (msItem \@ xmlIdAttr).one.headOption.map(_.attribute.value)
    val parentMsItem = (msItem.\^).one.head

    val sequenceTitle = (msItem \* teiNs("bibl") \* teiNs("title")).one.headOption.map(text(_))
    val parentTitle = (parentMsItem \* teiNs("bibl") \* teiNs("title")).one.headOption.map(text(_))

    for {
      manifestRanges <- parseRanges(doc)(msItem)
      manifestId <- id.toTask(new Exception(f"No xml:id for msItem in ${ doc.fileName }%s."))
      manifestTitle <- parentTitle.toTask(new Exception(f"No title element for parent msItem in ${ doc.fileName }%s."))
      manifestLabel <- sequenceTitle.toTask(new Exception(f"No title element for msItem in ${ doc.fileName }%s."))
    } yield new MithLogicalManifest {
      val id = manifestId
      val base = baseUri
      val title = manifestTitle
      val label = manifestLabel
      val service = constructManifestService(id)
      val ranges = manifestRanges
      val state = nearestState(msItem).map(expandStateName)
      val shelfmark = None
      val folio = None
      val hand = None
      val attribution = topLevelRepository(doc)
      val date = nearestDate(msItem)
      val agent = nearestAgent(msItem)
    }
  }
}


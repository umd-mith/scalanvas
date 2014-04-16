package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas._
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.tei._
import scales.utils._, ScalesUtils._
import scales.xml._, ScalesXml._

object MithTeiCollection {
  def createHandNames(docs: Map[String, CollectionDoc]): Map[(String, String), String] =
    docs.values.foldLeft(Map.empty[(String, String), String]) {
      case (map, CollectionDoc(doc, _, fileName)) =>
        (top(doc) \\* teiNs("handDesc") \* teiNs("handNote") \* teiNs("persName")).foldLeft(map) {
          case (map, persName) =>
            attrText(persName.\^.\@(xmlIdAttr)).fold(map) { personId =>
              map.updated((fileName, personId), text(persName))
            }
        }
    }
}

trait MithTeiCollection extends TeiCollection {
  def handNames: Map[(String, String), String]

  // Abbreviation should not include the pound sign.
  def lookupHandName(doc: CollectionDoc)(abbrev: String) =
    handNames.get((doc.fileName, abbrev))

  def topLevelRepository(doc: CollectionDoc): Option[String] = elemText(
    top(doc.doc) \\* teiNs("sourceDesc") \* teiNs("msDesc") \* teiNs("msIdentifier") \* teiNs("repository")
  )

  def topLevelShelfmark(doc: CollectionDoc): Option[String] = elemText(
    top(doc.doc) \\* teiNs("sourceDesc") \* teiNs("msDesc") \* teiNs("msIdentifier") \* teiNs("idNo")
  )

  def topLevelTitle(doc: CollectionDoc): Option[String] = elemText(
    (top(doc.doc) \\* teiNs("titleStmt") \* teiNs("title")).filter(
      title => text(title \@ "type") == "main"
    )
  )

  def topLevelXmlId(doc: CollectionDoc): Option[String] =
    attrText(top(doc.doc) \@ xmlIdAttr)

  def nearestDate(msItem: XmlPath): Option[String] = elemsText(
    msItem.ancestor_or_self_::.filter(
      node => !node.isItem && node.tree.section.name == teiNs("msItem")
    ) \* teiNs("bibl") \* teiNs("date")
  ).lastOption

  def nearestAgent(msItem: XmlPath): Option[String] = elemsText(
    msItem.ancestor_or_self_::.filter(
      node => !node.isItem && node.tree.section.name == teiNs("msItem")
    ) \* teiNs("bibl") \* teiNs("author")
  ).lastOption

  def nearestState(msItem: XmlPath): Option[String] = attrsText(
    msItem.ancestor_or_self_::.filter(
      node => !node.isItem && node.tree.section.name == teiNs("msItem")
    ) \* teiNs("bibl") \@ "status"
  ).lastOption

  def expandStateName(s: String) = s match {
    case "draft" => "Draft"
    case "fair_copy" => "Fair copy"
    case other => other
  }

  def lastHandShift[PT <: Iterable[XmlPath]](path: XPath[PT]): Option[String] = {
    val lastHandShift =
      path.preceding_::.filter(node => !node.isItem && node.tree.section.name == teiNs("handShift")).lastOption

    lastHandShift.flatMap(handShift => attrText(handShift \@ "new"))
  }
}


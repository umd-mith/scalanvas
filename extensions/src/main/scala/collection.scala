package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas._
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.tei._
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

  def topLevelRepository(doc: CollectionDoc): Option[String] = {
    val repository = top(doc.doc) \\* teiNs("sourceDesc") \* teiNs("msDesc") \* teiNs("msIdentifier") \* teiNs("repository")

    repository.one.headOption.map(text(_))
  }

  def topLevelShelfmark(doc: CollectionDoc): Option[String] = {
    val idNo = top(doc.doc) \\* teiNs("sourceDesc") \* teiNs("msDesc") \* teiNs("msIdentifier") \* teiNs("idNo")

    idNo.one.headOption.map(text(_))
  }

  def topLevelTitle(doc: CollectionDoc): Option[String] = {
    val titles = top(doc.doc) \\* teiNs("titleStmt") \* teiNs("title")

    titles.filter(title => text(title \@ NoNamespaceQName("type")) == "main").one.headOption.map(text(_))
  }

  def topLevelXmlId(doc: CollectionDoc): Option[String] =
    (top(doc.doc) \@ xmlIdAttr).one.headOption.map(text(_))

  def nearestDate(msItem: XmlPath): Option[String] = {
    val dates = msItem.ancestor_or_self_::.filter(
      node => !node.isItem && node.tree.section.name == teiNs("msItem")
    ) \* teiNs("bibl") \* teiNs("date")

    dates.\+.text.lastOption.map(_.item.value)
  }

  def nearestAgent(msItem: XmlPath): Option[String] = {
    val authors = msItem.ancestor_or_self_::.filter(
      node => !node.isItem && node.tree.section.name == teiNs("msItem")
    ) \* teiNs("bibl") \* teiNs("author")

    authors.\+.text.lastOption.map(_.item.value)
  }

  def nearestState(msItem: XmlPath): Option[String] = {
    val states = msItem.ancestor_or_self_::.filter(
      node => !node.isItem && node.tree.section.name == teiNs("msItem")
    ) \* teiNs("bibl") \@ NoNamespaceQName("status")

    states.lastOption.map(text(_))
  }

  def expandStateName(s: String) = s match {
    case "draft" => "Draft"
    case "fair_copy" => "Fair copy"
    case other => other
  }
}


package edu.umd.mith.scalanvas.extensions.parsing

import edu.umd.mith.scalanvas.parsing._
import edu.umd.mith.scalanvas.util.xml._
import edu.umd.mith.scalanvas.util.xml.tei._
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


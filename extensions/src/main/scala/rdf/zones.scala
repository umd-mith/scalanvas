package edu.umd.mith.scalanvas.extensions
package rdf

import edu.umd.mith.scalanvas._
import edu.umd.mith.scalanvas.rdf.{ Helpers, ObjectBinders, PropertyBinders, ScalanvasPrefixes }
import edu.umd.mith.util.xml._
import edu.umd.mith.util.xml.implicits._
import edu.umd.mith.util.xml.tei._
import edu.umd.mith.util.xml.tei.implicits._
import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._
import scalaz.{ Source => _, _ }, Scalaz._
import scales.xml._, ScalesXml._

trait ZoneReader { this: RDFOpsModule with MithPrefixes with MithObjectBinders with MithPropertyBinders with MithTeiCollection with Helpers =>
  private def bail(throwable: Throwable) = throw throwable

  def readZones(canvas: MithCanvas): List[PointedGraph[Rdf]] =
    canvas.transcription.fold(List.empty[PointedGraph[Rdf]]) {
      case (doc, transcription) =>
        val canvasBeginningOffset = transcription.beginningOffset.valueOr(bail)

        val zones = (transcription \\* teiNs("zone")).toList

        val typeCounts = zones.flatMap(zone =>
          attrText(zone \@ "type")
        ).groupBy(identity).map {
          case (key, values) => key -> values.size  
        }.withDefaultValue(0)

        val needExtraTop = if (typeCounts("top_marginalia_left") > 0 || typeCounts("top_marginalia_right") > 0) 
          true
        else
          false

        val extraRight = if (typeCounts("marginalia_right") > 0) 0.1 else 0.0
        val extraLeft = if (typeCounts("marginalia_left") > 0) 0.1 else 0.0
        val topHeight = if (needExtraTop) 0.10 else 0.05

        def coords(current: String, past: List[String]) = (current, past) match {
          case ("running_head", past) =>
            val runningHeadCount = typeCounts("running_head")
            val runningHeadIdx = past.count(_ == "running_head") + 1
            Some(
              ((runningHeadIdx.toDouble / runningHeadCount) - (1 / runningHeadCount.toDouble),  if (needExtraTop) 0.05 else 0.0),
              (1 / runningHeadCount.toDouble, topHeight)
            ).success
          case ("pagination", _) =>
            Some((0.8, 0.0) -> (0.1, topHeight)).success
          case ("library", _) => Some((0.9, 0.0) -> (0.1, topHeight)).success
          case ("left_margin", past) =>
            val leftMarginCount = typeCounts("left_margin")
            val leftMarginIdx = past.count(_ == "left_margin")
            Some(
              (0.0,  topHeight + (1 - topHeight) * (leftMarginIdx.toDouble / leftMarginCount)),
              (0.25, (1 - topHeight) / leftMarginCount)
            ).success
          case ("main", _) if typeCounts("left_margin") == 0 =>
            Some((0.125, topHeight) -> (0.875, 1 - topHeight)).success
          case ("main", _) => Some((0.25, topHeight) -> (0.75, 1 - topHeight)).success
          case ("logical", _) => None.success
          case ("marginalia_left", _) => None.success
          case ("marginalia_right", _) => None.success
          case ("top_marginalia_left", _) => Some((0.0, 0.0) -> (1.0, topHeight)).success
          case ("top_marginalia_right", _) => Some((0.5, 0.0) -> (1.0, topHeight)).success
          case ("column", past) => 
            val columnCount = typeCounts("column")
            val columnIdx = past.count(_ == "column") + 1
            val start = 0 + extraLeft
            val end = 1 - extraRight
            val area = (end - start) / columnCount.toDouble
            Some(
              ((area * (columnIdx.toDouble - 1) + extraLeft,  topHeight),
              (area, 1 - topHeight))
            ).success
          case ("", _) => None.success
          case other => 
             "Unknown zone in %s: %s!".format(canvas.shelfmark, other).fail
        }

        zones.foldLeft((
          List.empty[PointedGraph[Rdf]],
          List.empty[String]
        )) {
          case ((annotations, past), zone) =>
            val zoneType = attrText(zone \@ "type").getOrElse("")
            val b = zone.beginningOffset.valueOr(bail) - canvasBeginningOffset
            val e = zone.endingOffset.valueOr(bail) - canvasBeginningOffset

            val zoneCoords = coords(zoneType, past).valueOr(message => throw new Exception(message))

            zoneCoords match {
              case Some(((x, y), (zw, zh))) =>
                val xywh = "xywh=%d,%d,%d,%d".format(
                  (x * canvas.width).toInt,
                  (y * canvas.height).toInt,
                  (zw * canvas.width).toInt,
                  (zh * canvas.height).toInt
                )

                val annotation = contentAnnotation(
                  textOffsetSelection(
                    canvas.source,
                    b,
                    e
                  ),
                  fragmentSelection(canvas, xywh)
                )

                (annotations :+ annotation, zoneType :: past)
              case _ => (annotations, zoneType :: past)
            }
        }._1
    }
}


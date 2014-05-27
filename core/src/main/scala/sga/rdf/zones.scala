package edu.umd.mith.sga.rdf

import edu.umd.mith.sga.model.SgaCanvas
import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._
import scalaz._, Scalaz._

class ZoneReader[Rdf <: RDF](canvas: SgaCanvas)(implicit ops: RDFOps[Rdf])
  extends SgaPrefixes[Rdf]
  with SpecificResourceHelper[Rdf]
  with AnnotationHelper[Rdf] {

  val zones = canvas.transcription \\ "zone"

  val typeCounts = zones.map(
    zone => (zone \ "@type").text
  ).groupBy(identity).map {
    case (key, values) => key -> values.size  
  }.withDefaultValue(0)

  val needExtraTop = if (typeCounts("top_marginalia_left") > 0 
                      || typeCounts("top_marginalia_right") > 0
                      || typeCounts("top_marginalia") > 0) 
    true
  else
    false

  val needExtraBottom = if (typeCounts("bottom_marginalia_left") > 0 
                        || typeCounts("bottom_marginalia_right") > 0
                        || typeCounts("running_bottom") > 0)  
    true
  else
    false

  val needMiddleTop = if (typeCounts("top_marginalia_middle") > 0) 
    true
  else
    false

  var extraRight = if (typeCounts("marginalia_right") > 0) 
                      if (typeCounts("column") == 0) 0.25 
                      else 0.1
                   else 0.0

  var extraLeft = if (typeCounts("marginalia_left") > 0) 
                      if (typeCounts("column") ==0 && typeCounts("pasteon") == 0) 0.25 
                      else 0.1
                   else 0.0

  if (typeCounts("marginalia_right") > 0 && typeCounts("marginalia_left") > 0) {
    if (typeCounts("column") ==0 && typeCounts("pasteon") == 0) {
        extraRight = 0.15
        extraLeft = 0.15
    }
    else 0.1    
  }

  val topHeight = if (needExtraTop) 
                    if (typeCounts("running_head") > 0) 0.15
                    else 0.10 
                  else 0.05
  var runnerHeight = if (typeCounts("running_head") > 0 ) 0.05 else 0.0
  val bottomHeight = if (needExtraBottom) 0.10 else 0.0

  // HACK for tracking space taken in grid
  var curGridX: Int = 0
  var curGridY: Int = 0
  var widestGridX: Int = 0
  var tallestGridY: Int = 0

  private def coords(current: String, attrs: Map[String, String], past: List[String], pastRend: List[String]) = (current, past) match {
    case ("running_head", past) =>
      val runningHeadCount = typeCounts("running_head")
      val runningHeadIdx = past.count(_ == "running_head") + 1
      Some(
        ((runningHeadIdx.toDouble / runningHeadCount) - (1 / runningHeadCount.toDouble),  if (needExtraTop) 0.10 else 0.0),
        (1 / runningHeadCount.toDouble, topHeight)
      ).success
    case ("running_bottom", past) =>
      val runningBottomCount = typeCounts("running_bottom")
      val runningBottomIdx = past.count(_ == "running_bottom") + 1
      Some(
        ((runningBottomIdx.toDouble / runningBottomCount) - (1 / runningBottomCount.toDouble),  1 - bottomHeight),
        (1 / runningBottomCount.toDouble, 1.0)
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
    case ("main", _) if attrs.getOrElse("rend", "") contains "col" =>
      
      val c = """.*col-(\d+).*""".r
      val r = """.*row-(\d+).*""".r
      val rend = attrs("rend").toString

      val colSpan = rend match {
        case c(num) => num.toInt
        case _ => throw new RuntimeException(
          s"No column span specified in grid!"
        )
      }    

      val rowSpan = rend match {
        case r(num) => num.toInt
        case _ => throw new RuntimeException(
          s"No row span specified in grid!"
        )
      }    

      curGridY = rowSpan 
      tallestGridY = rowSpan

      Some(
        ((0.0 + extraLeft, 0.0 + topHeight),
        ((1.0 / (12.0 / colSpan.toDouble)) - extraRight, 1.0 / (6.0 / rowSpan.toDouble)))
      ).success
    case ("main", _) if (typeCounts("marginalia_left") > 0 | typeCounts("marginalia_right") > 0) =>
      Some((0.0 + extraLeft, topHeight) -> (1 - extraLeft - extraRight, 1 - bottomHeight - topHeight)).success
    case ("main", _) if typeCounts("left_margin") == 0 =>
      Some((0.125 + extraLeft, topHeight) -> (0.875 - extraLeft - extraRight, 1 - bottomHeight - topHeight)).success
    case ("main", _) => Some((0.25, topHeight) -> (0.75, 1 - bottomHeight - topHeight)).success
    case ("logical", _) => None.success
    // Unlike left_margins in SGA, these zones must occupy all the vertical space.
    // They are positioned and moved by the SC viewer.
    case ("marginalia_left", _) => 
      val margLeftCount = typeCounts("marginalia_left")
      val margLeftIdx = past.count(_ == "marginalia_left")
      Some(
        ((0.0, topHeight),
        (extraLeft, 1 - bottomHeight - topHeight))
      ).success
    case ("marginalia_right", _) => 
      val margRightCount = typeCounts("marginalia_right")
      val margRightIdx = past.count(_ == "marginalia_right")
      Some(
        ((1 - extraRight, topHeight),
        (extraRight, 1 - bottomHeight - topHeight))
      ).success
    case ("top_marginalia", _) => Some((0.0, 0.0) -> (1.0, topHeight - runnerHeight)).success
    case ("top_marginalia_left", _) => 
      val extX = if (needMiddleTop) 0.33 else 0.5
      Some((0.0, 0.0) -> (extX, topHeight - runnerHeight)).success
    case ("top_marginalia_middle", _) => Some((0.33, 0.0) -> (0.66, topHeight - runnerHeight)).success
    case ("top_marginalia_right", _) => 
      val startX = if (needMiddleTop) 0.66 else 0.5
      Some((startX, 0.0) -> (1.0, topHeight - runnerHeight)).success
    case ("bottom_marginalia_left", _) => 
      Some((0.0, 1 - bottomHeight) -> (0.5, bottomHeight)).success
    case ("bottom_marginalia_right", _) => 
      Some((0.5, 1 - bottomHeight) -> (0.5, bottomHeight)).success
    case ("column", past)  => 

      val c = """.*col-(\d+).*""".r
      val r = """.*row-(\d+).*""".r
      val rend = attrs.getOrElse("rend", "").toString

      val colSpan = rend match {
        case c(num) => num.toInt
        case _ => 0
      }    

      val rowSpan = rend match {
        case r(num) => num.toInt
        case _ => 0
      }    

      val startY = if (curGridY == 0) 0.0 else 1.0 / (6.0 / curGridY).toDouble
      val extY = ( 1.0 / (6.0 / rowSpan).toDouble ) - bottomHeight - topHeight

      val columnCount = typeCounts("column")
      val columnIdx = past.count(_ == "column") + 1
      val start = 0 + extraLeft
      val end = 1 - extraRight
      val area = (end - start) / columnCount.toDouble

      // Done calculating. Now update hack-y grid position trackers 
      // (Y only; columns are supposed to take full X)
      if (tallestGridY < curGridY + rowSpan) { tallestGridY = curGridY + rowSpan }

      Some(
        ((area * (columnIdx.toDouble - 1) + extraLeft,  startY + topHeight),
        (area, extY))
      ).success
    case ("pasteon" | "main_part", past) if attrs.getOrElse("rend", "") contains "col"  =>
     
      var columnIndex = pastRend.count(rend => rend contains "new")
      val isNewCol = attrs.values.exists(_ contains "new")
      var totColumns = 0

      val cols = zones.map(
        zone => (zone \ "@rend").text
      ).filter(z => z contains "new").groupBy(identity).map{
        case (key, values) => values.size
      }
      if (!cols.isEmpty) { 
        totColumns = cols.head + 1
      }

      val c = """.*col-(\d+).*""".r
      val r = """.*row-(\d+).*""".r
      val rend = attrs("rend").toString

      val colSpan = rend match {
        case c(num) => num.toInt
        case _ => throw new RuntimeException(
          s"No column span specified in grid!"
        )
      }    

      val rowSpan = rend match {
        case r(num) => num.toInt
        case _ => throw new RuntimeException(
          s"No row span specified in grid!"
        )
      }      

      if (isNewCol | current == "main_part") { 
        columnIndex = columnIndex + 1 
        curGridY = tallestGridY
        // check that the widest doesn't send the current pasteon too far right
        // if it does, reduce curGridX to make it visible.
        if (widestGridX + colSpan > 12) curGridX = 12 - colSpan
        else curGridX = widestGridX 
      }

      val startX = if (curGridX == 0) 0.0 else 1.0 / (12.0 / curGridX).toDouble
      val startY = if (curGridY == 0) 0.0 else 1.0 / (6.0 / curGridY).toDouble
      val extX = ( 1.0 / (12.0 / colSpan).toDouble ) - extraRight - extraLeft
      val extY = ( 1.0 / (6.0 / rowSpan).toDouble ) - bottomHeight - topHeight

      // Done calculating. Now update hack-y grid position trackers
      if (widestGridX < curGridX + colSpan) { widestGridX = curGridX + colSpan }
      curGridY = curGridY + rowSpan

      Some(
        ((startX + extraLeft, startY + topHeight),
        (extX, extY))
      ).success
    case ("", _) => None.success
    case other => 
       "Unknown zone in %s: %s!".format(canvas.shelfmark, other).fail
  }

  def readZones: List[PointedGraph[Rdf]] =
    zones.foldLeft(
      List.empty[PointedGraph[Rdf]],
      List.empty[String],
      List.empty[String]
    ) {
      case ((annotations, past, pastRend), zone) =>
        val zoneType = (zone \ "@type").text
        val zoneRend = (zone \ "@rend").text
        val attrs = zone.attributes.asAttrMap

        coords(zoneType, attrs, past, pastRend).fold(
          message => throw new RuntimeException(message),
          {
            case Some(((x, y), (zw, zh))) =>
              val xywh = "xywh=%d,%d,%d,%d".format(
                (x * canvas.width).toInt,
                (y * canvas.height).toInt,
                (zw * canvas.width).toInt,
                (zh * canvas.height).toInt
              )

              val attrs = zone.attributes.asAttrMap

              val annotation = contentAnnotation(
                textOffsetSelection(
                  canvas.source,
                  attrs("mu:b").toInt,
                  attrs("mu:e").toInt
                ),
                fragmentSelection(canvas, xywh)
              )

              (annotations :+ annotation, zoneType :: past, zoneRend :: pastRend)
            case _ => (annotations, zoneType :: past, zoneRend :: pastRend)
          }
        )
    }._1
}


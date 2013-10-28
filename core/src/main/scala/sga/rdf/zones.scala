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

  private def coords(current: String, past: List[String]) = (current, past) match {
    case ("top", _) => Some((0.4, 0.0) -> (0.2, 0.05)).success
    case ("pagination", _) =>
      //Some((0.8, 0.1) -> (0.1, 0.05)).success
      Some((0.8, 0.0) -> (0.1, 0.05)).success
    //case ("library", _) => Some((0.9, 0.10) -> (0.1, 0.05)).success
    case ("library", _) => Some((0.9, 0.0) -> (0.1, 0.05)).success
    case ("left_margin", past) =>
      val leftMarginCount = typeCounts("left_margin")
      val leftMarginIdx = past.count(_ == "left_margin")
      Some(
        (0.0,  0.05 + 0.95 * (leftMarginIdx.toDouble / leftMarginCount)),
        (0.25, 0.95 * (1.0 / leftMarginCount))
      ).success
    case ("main", _) if typeCounts("left_margin") == 0 =>
      Some((0.0, 0.05) -> (1.0, 0.95)).success
    case ("main", _) => Some((0.25, 0.05) -> (0.75, 0.95)).success
    case ("", _) => None.success
    case other => 
       "Unknown zone in %s: %s!".format(canvas.shelfmark, other).fail
  }

  def readZones: List[PointedGraph[Rdf]] =
    zones.foldLeft(
      List.empty[PointedGraph[Rdf]],
      List.empty[String]
    ) {
      case ((annotations, past), zone) =>
        val zoneType = (zone \ "@type").text

        coords(zoneType, past).fold(
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

              (annotations :+ annotation, zoneType :: past)
            case _ => (annotations, zoneType :: past)
          }
        )
    }._1
}


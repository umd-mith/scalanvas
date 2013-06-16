package edu.umd.mith.scalanvas

import edu.umd.mith.scalanvas.prefixes.StandardPrefixes
import edu.umd.mith.scalanvas.util.xml.XmlLabeler
import edu.umd.mith.scalanvas.util.xml.tei.{ Annotation, AnnotationExtractor }

import scala.language.reflectiveCalls
import scala.xml._
import scalaz._, Scalaz._

import org.w3.banana._

class ManifestCreator[Rdf <: RDF](
  base: String,
  id: String,
  title: String,
  teiSurfaces: Seq[Elem]
)(implicit val ops: RDFOps[Rdf])
  extends Prefixes[Rdf] with Aliases[Rdf] with OreUtils[Rdf] { // with StandardPrefixes[Rdf] {

  val surfaces = teiSurfaces map XmlLabeler.addCharOffsets

  val imageDerivBase = "http://sga.mith.org/images/derivatives/"
  val teiBase = "http://sga.mith.org/sc-demo/tei/"

  import ops._
  import org.w3.banana.diesel._
  import org.w3.banana.syntax._

  val ((canvases, imageAnnos), (textAnnos, zoneAnnos)) = surfaces.map { surface =>
    val attrs = surface.attributes.asAttrMap
    val id = attrs("xml:id")
    val lib = id.split("-").head
    val seqId = id.split("-").last
    val w = attrs("lrx").toInt
    val h = attrs("lry").toInt

    val annotationExtractor = AnnotationExtractor(surface, Set("c56-0113.02"))

    val canvas = (
      URI(base + "image-" + seqId)
        .a(sc.Canvas)
        .a(dms.Canvas)
        -- rdfs.label ->- ("Image " + seqId)
        -- exif.width ->- w
        -- exif.height ->- h
    )

    val images = (
      URI(base + "imageanno/image-" + seqId)
        .a(oa.Annotation)
        .a(dms.ImageAnnotation)
        -- oa.hasTarget ->- canvas
        -- oa.hasBody ->- (
          URI(imageDerivBase + lib + "/" + id + ".jpg")
            .a(dct.Image)
            .a(dms.ImageBody)
            -- dc.format ->- "image/jpg"
            -- exif.width ->- w
            -- exif.height ->- h
        )
    )

    val textFile = (
      URI(teiBase + lib + "/" + id + ".xml")
       -- dc.format ->- "application/tei+xml"
    )

    val lines = (surface \\ "line").toList.zipWithIndex.map { case (line, i) =>
      val attrs = line.attributes.asAttrMap

      (
        URI(base + "textanno/text-" + seqId + "-%04d".format(i))
          .a(oa.Annotation)
          .a(sga.LineAnnotation)
          .a(oax.Highlight)
          -- oa.hasTarget ->- (
            bnode().a(oa.SpecificResource)
              -- oa.hasSource ->- textFile
              -- oa.hasSelector ->- (
                bnode().a(oax.TextOffsetSelector)
                  -- oax.begin ->- attrs("mu:b").toInt
                  -- oax.end ->- attrs("mu:e").toInt
              )
          )
      )
    }

    val additions = annotationExtractor.additions.valueOr(es => sys.error(es.toList.mkString("\n"))).map {
      case Annotation((b, e), place) => (
        bnode()
          .a(oa.Annotation)
          .a(sga.AdditionAnnotation)
          .a(oax.Highlight)
          -- oa.hasTarget ->- (
            bnode().a(oa.SpecificResource)
              -- oa.hasSource ->- textFile
              -- oa.hasSelector ->- (
                bnode().a(oax.TextOffsetSelector)
                  -- oax.begin ->- b
                  -- oax.end ->- e
              )
              -- oa.hasStyle ->- place.flatMap { 
                case "superlinear" => Some("vertical-align: super;")
                case "sublinear"   => Some("vertical-align: sub;")
                case _ => None
              }.map { css => (
                bnode().a(cnt.ContentAsText)
                  -- dc.format ->- "text/css"
                  -- cnt.chars ->- css
              )}
          )
      )
    }

    val deletions = annotationExtractor.deletions.valueOr(es => sys.error(es.toList.mkString("\n"))).map {
      case Annotation((b, e), _) => (
        bnode()
          .a(oa.Annotation)
          .a(sga.DeletionAnnotation)
          .a(oax.Highlight)
          -- oa.hasTarget ->- (
            bnode().a(oa.SpecificResource)
              -- oa.hasSource ->- textFile
              -- oa.hasSelector ->- (
                bnode().a(oax.TextOffsetSelector)
                  -- oax.begin ->- b
                  -- oax.end ->- e
              )
          )
      )
    }
    
    val leftMarginCount = (surface \\ "zone").filter(
      zone => (zone \ "@type").text == "left_margin"
    ).size


    val zoneAnnos = (surface \\ "zone").foldLeft((List.empty[PG], 0)) {
      case ((zones, leftMarginIdx), zone) =>
        val zoneType = (zone \ "@type").text
        val isLeftMargin = (zoneType == "left_margin")

        val coords = zoneType match {
          case "top" => Some((0.4, 0.0) -> (0.2, 0.05))
          case "pagination" => Some((0.8, 0.0) -> (0.1, 0.05))
          case "library" => Some((0.9, 0.0) -> (0.1, 0.05))
          case "left_margin" => Some(
            (0.0,  0.05 + 0.95 * (leftMarginIdx.toDouble / leftMarginCount)) ->
            (0.25, 0.95 * (1.0 / leftMarginCount))
          )
          case "main" => Some((0.25, 0.05) -> (0.75, 0.95))
          case "" => None
          case other => sys.error("Unknown zone in " + id + ": " + other)
        }

        coords match {
          case Some(((x, y), (zw, zh))) =>
            val xywh = "xywh=%d,%d,%d,%d".format(
             (x * w).toInt, (y * h).toInt, (zw * w).toInt, (zh * h).toInt
            )

            val attrs = zone.attributes.asAttrMap

            val zoneAnno = (
              bnode()
                .a(oa.Annotation)
                .a(sc.ContentAnnotation)
                -- oa.hasBody ->- (
                  bnode().a(oa.SpecificResource)
                    -- oa.hasSource ->- textFile
                    -- oa.hasSelector ->- (
                      bnode().a(oax.TextOffsetSelector)
                        -- oax.begin ->- attrs("mu:b").toInt
                        -- oax.end ->- attrs("mu:e").toInt
                    )
                )
                -- oa.hasTarget ->- (
                  bnode().a(oa.SpecificResource)
                    -- oa.hasSource ->- canvas
                    -- oa.hasSelector ->- (
                      bnode().a(oa.FragmentSelector)
                        -- rdf.value ->- xywh
                    )
                )
            )
                  
            (zones :+ zoneAnno, leftMarginIdx + (if (isLeftMargin) 1 else 0))
          case _ => (zones, leftMarginIdx)
        }
    }._1

    ((canvas, images), (lines ::: additions ::: deletions, zoneAnnos))
  }.toList.unzip.bimap(_.unzip, _.unzip)
 
  val imageAnnotations = (
    URI(base + "ImageAnnotations")
      .a(sc.AnnotationList)
      .a(dms.ImageAnnotationList)
  ) aggregates imageAnnos

  val zoneAnnotations = (
    URI(base + "SectionAnnotations")
      .a(sc.AnnotationList)
  ) aggregates zoneAnnos.flatten

  val textAnnotations = (
    URI(base + "TextAnnotations")
      .a(sc.AnnotationList)
  ) aggregates textAnnos.flatten

  val sequence = (
    URI(base + "NormalSequence")
      .a(sc.Sequence)
      .a(dms.Sequence)
      -- rdfs.label ->- "The editorial sequence"
  ) aggregates canvases

  val manifest = (
    URI(base + "Manifest")
      .a(sc.Manifest)
      .a(dms.Manifest)
      .a(ore.Aggregation)
      -- dc.title ->- title
      -- rdfs.label ->- title
      -- tei.idno ->- id
      -- ore.aggregates ->- sequence
      -- ore.aggregates ->- imageAnnotations
      -- ore.aggregates ->- zoneAnnotations
      -- ore.aggregates ->- textAnnotations
  )

  def jsonGraph = jsonResourceMap(base + "Manifest", manifest).graph
  def jsonldGraph = jsonldResourceMap(base + "Manifest", manifest).graph
  def xmlGraph = xmlResourceMap(base + "Manifest", manifest).graph
}

object ManifestCreator extends App {
  import java.io.File

  val dir = args(0)
  val name = args(1)
  val description = args(2)

  val surfaces = new File(dir).listFiles.filter(
    _.getName.contains(name)
  ).sorted.map(XML.loadFile)

  val base = "http://sga.mith.org/sc-demo/" + name + "/"

  /*
  import org.w3.banana.sesame._

  import org.apache.marmotta.commons.sesame.rio.jsonld._
  import org.openrdf.rio.{ RDFFormat, Rio }

  val mc = new ManifestCreator[Sesame](base, name, description, surfaces)

  val stream = new java.io.FileOutputStream("Manifest.jsonld")
  val writer = Rio.createWriter(RDFFormat.JSONLD, stream)

  import scala.collection.JavaConverters._

  writer.startRDF()
  mc.jsonldGraph.asScala.foreach(writer.handleStatement)
  writer.endRDF()

  stream.close()
  */

  import org.w3.banana.jena._
  import com.hp.hpl.jena.rdf.model.ModelFactory

  val mc = new ManifestCreator[Jena](base, name, description, surfaces)

  val jsonModel = ModelFactory.createModelForGraph(mc.jsonGraph.jenaGraph)
  val jsonldModel = ModelFactory.createModelForGraph(mc.jsonldGraph.jenaGraph)
  val xmlModel = ModelFactory.createModelForGraph(mc.xmlGraph.jenaGraph)

  val xmlWriter = new java.io.PrintWriter("Manifest.xml")
  xmlModel write xmlWriter
  xmlWriter.close()

  val jsonWriter = new java.io.PrintWriter("Manifest.json")
  new org.openjena.riot.system.JenaWriterRdfJson().write(jsonModel, jsonWriter, null)
  jsonWriter.close()

  import scala.collection.JavaConverters._
  import com.github.jsonldjava.core.JSONLD
  import com.github.jsonldjava.utils.JSONUtils
  import com.github.jsonldjava.impl.JenaRDFParser

  val ctx = Map(
    "cnt"  -> "http://www.w3.org/2011/content#",
    "dc"   -> "http://purl.org/dc/elements/1.1/",
    "dct"  -> "http://purl.org/dc/dcmitype/",
    "dms"  -> "http://dms.stanford.edu/ns/",
    "exif" -> "http://www.w3.org/2003/12/exif/ns#",
    "oa"   -> "http://www.w3.org/ns/openannotation/core/",
    "oax"  -> "http://www.w3.org/ns/openannotation/extension/",
    "ore"  -> "http://www.openarchives.org/ore/terms/",
    "rdf"  -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#",
    "sc"   -> "http://www.shared-canvas.org/ns/",
    "sga"  -> "http://www.shelleygodwinarchive.org/ns1#",
    "tei"  -> "http://www.tei-c.org/ns/1.0/"
  )

  val javaCtx = new java.util.HashMap[String, Object]()
  ctx.foreach { case (k, v) => javaCtx.put(k, v) }

  val parser = new JenaRDFParser()
  val jsonld = JSONLD.compact(JSONLD.fromRDF(jsonldModel, parser), javaCtx)

  val jsonldWriter = new java.io.PrintWriter("Manifest.jsonld")
  JSONUtils.writePrettyPrint(jsonldWriter, jsonld)
  jsonldWriter.close()
}


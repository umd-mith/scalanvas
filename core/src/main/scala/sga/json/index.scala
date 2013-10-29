package edu.umd.mith.sga.json

import argonaut._, Argonaut._
import edu.umd.mith.scalanvas.model._
import edu.umd.mith.sga.model._

class IndexManifest(manifest: SgaManifest) {
  private val relatedServiceString = manifest.service.fold(
    throw new RuntimeException("No related service!")
  )(_.uri.toString)
  
  def toJsonLd: Json = Json(
    "@context" := "http://www.shared-canvas.org/ns/context.json",
    "@id" := manifest.itemBasePlus("/Manifest-index.jsonld").toString,
    "@type" := "sc:Manifest",
    "attribution" := manifest.attribution,
    "dc:title" := manifest.title, 
    "label" := manifest.label,
    "service" := relatedServiceString,
    "metadata" := List(
      Json(
        "label" := "author",
        "value" := manifest.agent.get
      )
    ),
    "seeAlso" := "http://www.shelleygodwinarchive.org/",
    "canvases" := createCanvasList,
    "sequences" := List(createSequence),
    "images" := createImageList,
    "structures" := createStructureList
  )

  def createImageList: List[Json] = manifest.sequence.canvases.flatMap { canvas =>
    canvas.images.map {
      case ImageForPainting(uri, width, height, format, service) => Json(
        // The next line is an awful hack.
        "@id" := manifest.itemBasePlus("/image-annotations/" + canvas.seq).toString,
        "@type" := "oa:Annotation",
        "motivation" := "sc:painting",
        "resource" := Json(
          "@id" := uri.toString,
          "@type" := "dctypes:Image",
          "width" := width,
          "height" := height,
          "format" := format,
          "service" := service.map {
            case Service(uri, profile) => Json( 
              "@id" := uri.toString,
              "profile" := profile.map(_.toString)
            )
          }
        ),
        "on" := canvas.uri.toString
      )
    }
  }

  private def createCanvasList = manifest.sequence.canvases.zipWithIndex.map {
    case (canvas, i) => Json(
      "@id" := canvas.uri.toString,
      "@type" := "sc:Canvas",
      "label" := canvas.label,
      "width" := canvas.width,
      "height" := canvas.height,
      "sga:hasTeiSource" := canvas.source.uri.toString,
      "service" := "%s#n=%d".format(relatedServiceString, i + 1)
    )
  }

  private def createSequence = manifest.sequence match {
    case Sequence(Some(uri), label, canvases) => Json(
      "@id" := uri.toString,
      "@type" := "sc:Sequence",
      "label" := label,
      "canvases" := canvases.map(_.uri.toString),
      "viewingDirection" := "left-to-right",
      "viewingHint" := "paged"
    )
  }

  private def createStructureList = manifest.ranges.map {
    case Range(uri, label, canvases) => Json(
      "@id" := uri.toString,
      "@type" := "sc:Range",
      "label" := label,
      "canvases" := canvases.map(_.uri.toString)
    )
  }

}


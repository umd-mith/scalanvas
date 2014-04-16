package edu.umd.mith.scalanvas

import java.net.URI
import scales.xml.XmlPath

case class Service(
  uri: URI,
  profile: Option[URI] = None
) extends LocatedResource

case class Link(
  uri: URI,
  format: String
) extends LocatedResource with Formatted

trait Canvas extends LocatedResource
  with Rect
  with Labeled
  with MetadataLabeled
  with HasRelatedService {
  def uri: URI
  def label: String
  def width: Int
  def height: Int
  def images: List[Image]
  def transcription: Option[(CollectionDoc, XmlPath)]
  def reading: Option[Link]
  def source: Option[Link]
}

sealed trait Image extends LocatedResource
  with Rect
  with Formatted
  with HasRelatedService
  with Motivated

case class ImageForPainting(
  uri: URI,
  width: Int,
  height: Int,
  format: String,
  service: Option[Service]
) extends Image {
  val motivation = Some(Painting)
}

case class Sequence[C <: Canvas](
  muri: Option[URI],
  label: String,
  canvases: List[C]
) extends Resource with Labeled

case class Range[C <: Canvas](
  uri: URI,
  label: String,
  canvases: List[C]
) extends LocatedResource with Labeled

case class TextSelection[A](
  source: A,
  begin: Int,
  end: Int
) extends Resource {
  val muri = None
}

trait ResourceMap[A] extends LocatedResource with Formatted {
  def described: A
}

trait Manifest[C <: Canvas, M <: Manifest[C, M]]
  extends LocatedResource
  with Labeled
  with MetadataLabeled
  with HasRelatedService { manifest: M =>
  def base: URI
  def id: String
  def title: String
  def sequence: Sequence[C]
  def ranges: List[Range[C]]

  def uri = itemBasePlus("/Manifest")

  private def resourceMap(mimeType: String, extension: String) =
    new ResourceMap[M] {
      val uri = new URI(manifest.uri.toString + extension)
      val format = mimeType
      val described = manifest
    }

  def xmlResource = resourceMap("application/rdf+xml", ".xml")
  def jsonResource = resourceMap("application/rdf+json", ".json")
  def jsonldResource = resourceMap("application/ld+json", ".jsonld")

  def itemBasePlus(path: String) = basePlus("/%s%s".format(id, path))

  def basePlus(path: String) = new URI(
    base.getScheme,
    base.getUserInfo,
    base.getHost,
    base.getPort,
    base.getPath + path,
    base.getQuery,
    base.getFragment
  )
}


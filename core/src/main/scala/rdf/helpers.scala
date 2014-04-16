package edu.umd.mith.scalanvas.rdf

import org.w3.banana._
import org.w3.banana.binder._
import org.w3.banana.diesel._
import org.w3.banana.syntax._

trait Helpers { this: RDFOpsModule with ScalanvasPrefixes =>
  trait SpecificResourceHelper {

    def base[A](source: A)(selector: PointedGraph[Rdf])(implicit aToPG: ToPG[Rdf, A]) = ( 
      Ops.bnode().a(oa.SpecificResource)
        -- oa.hasSource ->- source
        -- oa.hasSelector ->- selector
    )

    def textOffsetSelection[A](source: A, begin: Int, end: Int)(implicit aToPG: ToPG[Rdf, A]) =
      base(source)(
        Ops.bnode().a(oax.TextOffsetSelector)
          -- oax.begin ->- begin
          -- oax.end ->- end
      )
  
    def fragmentSelection[A](source: A, value: String)(implicit aToPG: ToPG[Rdf, A]) =
      base(source)(
        Ops.bnode().a(oa.FragmentSelector) -- rdf.value ->- value
      )
  }

  trait AnnotationHelper {
    private def base() = Ops.bnode().a(oa.Annotation)

    def contentAnnotation[A, B](body: A, target: B)(implicit
      aToPG: ToPG[Rdf, A],
      bToPG: ToPG[Rdf, B]
    ) = (
      base()
        .a(sc.ContentAnnotation)
        -- oa.hasBody ->- body
        -- oa.hasTarget ->- target
    )
  }

  trait CssHelper {
    def addCssStyle(g: PointedGraph[Rdf], css: String) = g -- oa.hasStyle ->- (
      Ops.bnode().a(cnt.ContentAsText)
        -- dc.format ->- "text/css"
        -- cnt.chars ->- css
    )
  }

  trait OreHelper {
    implicit class Aggregates(g: PointedGraph[Rdf]) {
      def aggregates[A](aggregated: List[A])(implicit
        aToPG: ToPG[Rdf, A]
      ) = aggregated match {
        case h :: t => (
          //aggregated.foldLeft(
            g.a(rdf.List) //.a(ore.Aggregation)
              -- rdf.first ->- h
              -- rdf.rest ->- t
        )
          //  ) {
          //    case (acc, item) => acc -- ore.aggregates ->- item 
          //}
        case Nil => g
      }
    }
  }
}


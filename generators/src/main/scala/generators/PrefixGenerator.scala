package edu.umd.mith.scalanvas.generators

import edu.umd.mith.scalanvas.generators.utils.ReflectionUtils
import org.w3.banana._
import scala.language.experimental.macros
import scala.reflect.macros.Context
import scalax.io.Resource

object PrefixGenerator extends SchemaParser[Rdf] with ReflectionUtils {
  def fromSchema[Rdf <: RDF](
    uri: String,
    prefix: String,
    path: String
  )(implicit ops: RDFOps[Rdf]) = macro fromSchema_impl[Rdf]

  def fromSchema_impl[Rdf <: RDF: c.WeakTypeTag](c: Context)(
    uri: c.Expr[String],
    prefix: c.Expr[String],
    path: c.Expr[String]
  )(ops: c.Expr[RDFOps[Rdf]]) = {
    import c.universe._
 
    val uriLit = uri.tree match {
      case Literal(Constant(s: String)) => s
      case _ => c.abort(
        c.enclosingPosition,
        "You must provide a literal URI!"
      )
    }

    val prefixLit = prefix.tree match {
      case Literal(Constant(s: String)) => s
      case _ => c.abort(
        c.enclosingPosition,
        "You must provide a literal prefix!"
      )
    }

    val stream = path.tree match {
      case Literal(Constant(s: String)) =>
        Option(this.getClass.getResourceAsStream(s)).getOrElse(
          c.abort(c.enclosingPosition, "Invalid resource path!")
        )
      case _ => c.abort(
        c.enclosingPosition,
        "You must provide a literal resource path for schema parsing!"
      )
    }

    parseSchema(uriLit, prefixLit, Resource.fromInputStream(stream)).fold(
      t => c.abort(c.enclosingPosition, "Invalid schema!" + t)
    , { case (properties, classes) =>
      val anon = newTypeName(c.fresh())
      val wrapper = newTypeName(c.fresh())
      val applier = Select(This(anon), newTermName("apply"))

      val defs = (properties ++ classes).map { name =>
        ValDef(
          Modifiers(),
          newTermName(name),
          TypeTree(),
          Apply(applier, c.literal(name).tree :: Nil)
        )
      }

      c.Expr[PrefixBuilder[Rdf]](
        Block(
          List(
            ClassDef(
              Modifiers(),
              anon,
              Nil,
              Template(
                Ident(weakTypeOf[PrefixBuilder[Rdf]].typeSymbol) :: Nil,
                emptyValDef,
                DefDef(
                  Modifiers(),
                  nme.CONSTRUCTOR,
                  Nil,
                  Nil :: Nil,
                  TypeTree(),
                  Block(
                    Apply(
                      Apply(
                        Select(
                          Super(This(tpnme.EMPTY), tpnme.EMPTY),
                          nme.CONSTRUCTOR
                        ),
                        List(
                          c.literal(prefixLit).tree,
                          c.literal(uriLit).tree  
                        )
                      ),
                      c.resetAllAttrs(ops.tree) :: Nil
                    ) :: Nil,
                    c.literalUnit.tree
                  )
                ) :: defs
              )
            ),
            ClassDef(
              Modifiers(Flag.FINAL),
              wrapper,
              Nil,
              Template(
                Ident(anon) :: Nil,
                emptyValDef,
                constructor(c.universe) :: Nil
              )
            )
          ),
          Apply(Select(New(Ident(wrapper)), nme.CONSTRUCTOR), Nil)
        )
      )
    })
  }
}


package edu.umd.mith.scalanvas.generators.utils

trait ReflectionUtils {
  def constructor(u: scala.reflect.api.Universe) = {
    import u._
 
    DefDef(
      Modifiers(),
      nme.CONSTRUCTOR,
      Nil,
      Nil :: Nil,
      TypeTree(),
      Block(
        Apply(
          Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR),
          Nil
        ) :: Nil,
        Literal(Constant(()))
      )
    )
  }
}


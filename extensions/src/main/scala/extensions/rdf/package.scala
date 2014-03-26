package edu.umd.mith.scalanvas.extensions

import edu.umd.mith.scalanvas.rdf.{ Helpers, ObjectBinders, PropertyBinders }

package object rdf extends Helpers
  with MithObjectBinders with MithPropertyBinders
  with ObjectBinders with PropertyBinders


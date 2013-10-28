package edu.umd.mith.sga

import edu.umd.mith.scalanvas.rdf.{
  Helpers,
  ObjectBinders => ScalanvasObjectBinders,
  PropertyBinders => ScalanvasPropertyBinders
}

package object rdf extends Helpers
  with ObjectBinders with PropertyBinders
  with ScalanvasObjectBinders with ScalanvasPropertyBinders


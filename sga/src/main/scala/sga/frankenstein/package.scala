package edu.umd.mith.sga

import edu.umd.mith.banana.jena.DefaultGraphJenaModule
import org.w3.banana.jena.Jena

package object frankenstein extends DefaultGraphJenaModule {
  type Rdf = Jena
}

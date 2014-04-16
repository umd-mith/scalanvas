package edu.umd.mith.util.xml

import javax.xml.parsers.{ SAXParser, SAXParserFactory }
import scales.xml.parser.sax.DefaultSaxSupport
import scales.utils.resources.{ Loaner, SimpleUnboundedPool }

object XIncludeSAXParserFactoryPool extends SimpleUnboundedPool[SAXParserFactory] { pool =>
  def create = {
    val parserFactory = SAXParserFactory.newInstance()
    parserFactory.setNamespaceAware(true)
    parserFactory.setFeature("http://xml.org/sax/features/namespaces", true)
    parserFactory.setXIncludeAware(true)
    parserFactory.setValidating(false)
    parserFactory
  }

  val parsers = new Loaner[SAXParser] with DefaultSaxSupport {
    def loan[X](tThunk: SAXParser => X): X = pool.loan(x => tThunk(x.newSAXParser))
  }
}


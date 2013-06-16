Scalanvas
=========

Scalanvas is a Scala library for creating
[Shared Canvas](http://www.shared-canvas.org/)
manifests from other document representations (TEI, etc.).

It's built on the [W3C](http://www.w3.org/)'s
[banana-rdf](https://github.com/w3c/banana-rdf) library,
and uses [jsonld-java](https://github.com/jsonld-java/jsonld-java)
for [JSON-LD](http://json-ld.org/) serialization.

It includes (among other things)
[a demonstration](https://github.com/umd-mith/scalanvas/blob/master/generators/src/main/scala/generators/PrefixGenerator.scala)
of how Scala 2.10's
macro system can be used to parse RDF schemas at compile time to produce
Scala bindings. So, for example, instead of writing something like this:

``` scala
class OREPrefix[Rdf <: RDF](ops: RDFOps[Rdf])
  extends PrefixBuilder("ore", "http://www.openarchives.org/ore/terms/")(ops) {
  val aggregates = apply("aggregates")
  val describes = apply("describes")
  val isDescribedBy = apply("isDescribedBy")
  val Aggregation = apply("Aggregation")
  val ResourceMap = apply("ResourceMap")
  // And so on...
}

object OREPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new OREPrefix[Rdf](ops)
}
```

And then eventually:

``` scala
val ore = OREPrefix[Rdf]
```

We can just write the following:

``` scala
val ore = PrefixGenerator.fromSchema(
  "http://www.openarchives.org/ore/terms/",
  "ore",
  "/edu/umd/mith/scalanvas/schemas/oreterms.rdf"
)
```

Now the RDF schema at the indicated resource path will be parsed at
compile time, and we'll have an anonymous subclass of `Prefix` with
all of the members of our `OREPrefix` above added for us. This approach
is less tedious, less error-prone, and much easier to maintain in the
case of changing schemas.


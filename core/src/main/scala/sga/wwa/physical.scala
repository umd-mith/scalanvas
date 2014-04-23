package edu.umd.mith.sga.wwa

import edu.umd.mith.scalanvas.model.Sequence
import edu.umd.mith.sga.model.SgaCanvas

trait PhysicalManifest extends WwaManifest {
  this: WwaConfiguration =>
  val ranges = Nil

  def firstIndex: Int
  def pageCount: Int

  private lazy val pages = shelfmarkMap.drop(firstIndex).take(pageCount)

  private lazy val canvases = pages.map {
    case (fileId, (shelfmark, folio)) => parseTeiFile(fileId, shelfmark, folio)
  }

  lazy val sequence = Sequence[SgaCanvas](
    Some(itemBasePlus("/physical-sequence")),
    "Physical sequence",
    canvases
  )
}

trait Duk00055Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00055"
  val firstIndex = 0
  val pageCount = 6
}

trait Duk00086Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00086"
  val firstIndex = 6
  val pageCount = 1
}

trait Duk00106Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00106"
  val firstIndex = 7
  val pageCount = 1
}

trait Duk00107Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00107"
  val firstIndex = 8
  val pageCount = 1
}

trait Duk00109Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00109"
  val firstIndex = 9
  val pageCount = 2
}

trait Duk00110Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00110"
  val firstIndex = 11
  val pageCount = 1
}

trait Duk00111Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00111"
  val firstIndex = 12
  val pageCount = 1
}

trait Duk00144Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00144"
  val firstIndex = 13
  val pageCount = 5
}

trait Duk00169Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00169"
  val firstIndex = 18
  val pageCount = 3
}

trait Duk00171Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00171"
  val firstIndex = 21
  val pageCount = 2
}

trait Duk00172Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00172"
  val firstIndex = 23
  val pageCount = 1
}

trait Duk00175Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00175"
  val firstIndex = 24
  val pageCount = 2
}

trait Duk00176Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00176"
  val firstIndex = 26
  val pageCount = 2
}

trait Duk00177Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00177"
  val firstIndex = 28
  val pageCount = 3
}

trait Duk00178Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00178"
  val firstIndex = 31
  val pageCount = 3
}

trait Duk00179Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00179"
  val firstIndex = 34
  val pageCount = 1
}

trait Duk00180Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00180"
  val firstIndex = 35
  val pageCount = 1
}

trait Duk00184Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00184"
  val firstIndex = 36
  val pageCount = 4
}

trait Duk00185Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00185"
  val firstIndex = 40
  val pageCount = 2
}

trait Duk00188Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00188"
  val firstIndex = 42
  val pageCount = 11
}

trait Duk00189Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00189"
  val firstIndex = 53
  val pageCount = 1
}

trait Duk00191Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00191"
  val firstIndex = 54
  val pageCount = 5
}

trait Duk00192Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00192"
  val firstIndex = 59
  val pageCount = 1
}

trait Duk00195Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00195"
  val firstIndex = 60
  val pageCount = 50
}

trait Duk00196Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00196"
  val firstIndex = 110
  val pageCount = 2
}

trait Duk00197Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00197"
  val firstIndex = 112
  val pageCount = 2
}

trait Duk00198Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00198"
  val firstIndex = 114
  val pageCount = 1
}

trait Duk00200Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00200"
  val firstIndex = 115
  val pageCount = 2
}

trait Duk00201Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00201"
  val firstIndex = 117
  val pageCount = 2
}

trait Duk00673Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00673"
  val firstIndex = 119
  val pageCount = 1
}

trait Duk00674Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00674"
  val firstIndex = 120
  val pageCount = 2
}

trait Duk00677Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00677"
  val firstIndex = 122
  val pageCount = 2
}

trait Duk00678Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00678"
  val firstIndex = 124
  val pageCount = 2
}

trait Duk00679Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00679"
  val firstIndex = 126
  val pageCount = 1
}

trait Duk00681Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00681"
  val firstIndex = 127
  val pageCount = 1
}

trait Duk00682Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00682"
  val firstIndex = 128
  val pageCount = 1
}

trait Duk00683Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00683"
  val firstIndex = 129
  val pageCount = 1
}

trait Duk00684Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00684"
  val firstIndex = 130
  val pageCount = 1
}

trait Duk00685Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00685"
  val firstIndex = 131
  val pageCount = 1
}

trait Duk00686Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00686"
  val firstIndex = 132
  val pageCount = 2
}

trait Duk00687Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00687"
  val firstIndex = 134
  val pageCount = 1
}

trait Duk00692Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00692"
  val firstIndex = 135
  val pageCount = 15
}

trait Duk00705Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00705"
  val firstIndex = 150
  val pageCount = 1
}

trait Duk00707Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.00707"
  val firstIndex = 151
  val pageCount = 28
}

trait Duk03782Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "duk.03782"
  val firstIndex = 179
  val pageCount = 37
}

trait Loc03399Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03399"
  val firstIndex = 216
  val pageCount = 4
}

trait Loc03400Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03400"
  val firstIndex = 220
  val pageCount = 2
}

trait Loc03401Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03401"
  val firstIndex = 222
  val pageCount = 2
}

trait Loc03402Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03402"
  val firstIndex = 224
  val pageCount = 1
}

trait Loc03403Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03403"
  val firstIndex = 225
  val pageCount = 2
}

trait Loc03404Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03404"
  val firstIndex = 227
  val pageCount = 8
}

trait Loc03405Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03405"
  val firstIndex = 235
  val pageCount = 2
}

trait Loc03408Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03408"
  val firstIndex = 237
  val pageCount = 2
}

trait Loc03409Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03409"
  val firstIndex = 239
  val pageCount = 2
}

trait Loc03410Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03410"
  val firstIndex = 241
  val pageCount = 2
}

trait Loc03411Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03411"
  val firstIndex = 243
  val pageCount = 2
}

trait Loc03412Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03412"
  val firstIndex = 245
  val pageCount = 2
}

trait Loc03413Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03413"
  val firstIndex = 247
  val pageCount = 12
}

trait Loc03415Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03415"
  val firstIndex = 259
  val pageCount = 3
}

trait Loc03418Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03418"
  val firstIndex = 262
  val pageCount = 2
}

trait Loc03419Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03419"
  val firstIndex = 264
  val pageCount = 2
}

trait Loc03420Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03420"
  val firstIndex = 266
  val pageCount = 2
}

trait Loc03421Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03421"
  val firstIndex = 268
  val pageCount = 2
}

trait Loc03423Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03423"
  val firstIndex = 270
  val pageCount = 2
}

trait Loc03424Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03424"
  val firstIndex = 272
  val pageCount = 6
}

trait Loc03425Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03425"
  val firstIndex = 278
  val pageCount = 4
}

trait Loc03426Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03426"
  val firstIndex = 282
  val pageCount = 2
}

trait Loc03428Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03428"
  val firstIndex = 284
  val pageCount = 10
}

trait Loc03429Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03429"
  val firstIndex = 294
  val pageCount = 2
}

trait Loc03430Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03430"
  val firstIndex = 296
  val pageCount = 1
}

trait Loc03434Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03434"
  val firstIndex = 297
  val pageCount = 2
}

trait Loc03436Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03436"
  val firstIndex = 299
  val pageCount = 2
}

trait Loc03437Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03437"
  val firstIndex = 301
  val pageCount = 2
}

trait Loc03440Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03440"
  val firstIndex = 303
  val pageCount = 2
}

trait Loc03441Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03441"
  val firstIndex = 305
  val pageCount = 2
}

trait Loc03442Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03442"
  val firstIndex = 307
  val pageCount = 4
}

trait Loc03445Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03445"
  val firstIndex = 311
  val pageCount = 6
}

trait Loc03450Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03450"
  val firstIndex = 317
  val pageCount = 1
}

trait Loc03456Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03456"
  val firstIndex = 318
  val pageCount = 7
}

trait Loc03784Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03784"
  val firstIndex = 325
  val pageCount = 6
}

trait Loc03801Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "loc.03801"
  val firstIndex = 331
  val pageCount = 14
}

trait Nyp00027Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "nyp.00027"
  val firstIndex = 345
  val pageCount = 1
}

trait Nyp00030Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "nyp.00030"
  val firstIndex = 346
  val pageCount = 1
}

trait Nyp00031Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "nyp.00031"
  val firstIndex = 347
  val pageCount = 1
}

trait Nyp00035Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "nyp.00035"
  val firstIndex = 348
  val pageCount = 1
}

trait Nyp00392Manifest extends PhysicalManifest {
  this: WwaConfiguration =>
  val id = "nyp.00392"
  val firstIndex = 349
  val pageCount = 1
}


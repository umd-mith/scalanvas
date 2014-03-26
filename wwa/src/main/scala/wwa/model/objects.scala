package edu.umd.mith.wwa.model

import edu.umd.mith.scalanvas.model.{ Canvas, Manifest }

trait WwaCanvas extends MithCanvas with WwaMetadataLabeled

trait WwaManifest extends Manifest[WwaCanvas, WwaManifest] with WwaMetadataLabeled


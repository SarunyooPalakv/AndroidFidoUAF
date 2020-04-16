package com.project.fidouafetda.model

class DisplayPNGCharacteristicsDescriptor {
    var width: Long = 0
    var height: Long = 0
    var bitDepth: String? = null
    var colorType: String? = null
    var compression: String? = null
    var filter: String? = null
    var interlace: String? = null
    var plte: Array<rgbPalletteEntry>? = null
}

package com.project.fidouafetda.model

class MatchCriteria {

    var aaid: Array<String>? = null
    var userVerification:Int? = null
    var authenticatorVersion:Int? = null
    var keyIDs: Array<String>? = null
    var assertionSchemes:Array<String>? = null
    var vendorID:Array<String>? = null
    var keyProtection:Int? = null
    var matcherProtection:Int? = null
    var attachmentHint:Int? = null
    var tcDisplay:Int? = null
    var authenticationAlgorithms:Array<Int>? = null
    var attestationTypes:Array<String>? = null
    var exts:Array<Extension>? = null
}
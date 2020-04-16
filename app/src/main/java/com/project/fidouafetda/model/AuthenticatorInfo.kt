package com.project.fidouafetda.model

class AuthenticatorInfo {

    var aaid: Array<String>? = null
    var userVerification:Int? = null
    var authenticatorVersion:Int? = null
    var vendorID:Array<String>? = null
    var keyIDs: Array<String>? = null
    var keyProtection:Int? = null
    var matcherProtection:Int? = null
    var attachmentHint:Int? = null
    var tcDisplay:Int? = null
    var authenticationAlgorithm:Array<Int>? = null
    var assertionScheme:String? = null
    var attestationTypes:Array<String>? = null
    var exts:Array<Extension>? = null
}
package com.project.fidouafetda.model

class AuthenticationResponse {

    var header: OperationHeader? = null
    var fcParams: String? = null
    var assertions: Array<AuthenticatorSignAssertion?>? = null

}
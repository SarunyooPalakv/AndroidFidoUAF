package com.project.fidouafetda.callback


import com.project.fidouafetda.model.*


interface RPClientListener {

    fun callBackRegRequest(response: List<RegistrationRequest>)

    fun callBackRegResponse(response: List<LoginResponse>)

    fun callBackAuthRequest(response: List<AuthenticationRequest>)

    fun callBackAuthResponse(response: List<PurchaseResponse>)

    fun callBackDeregRequest(response: List<DeregistrationRequest>)

    fun onStatusError(response: String)

    fun onBodyError(response: String)

    fun onFailure(t: Throwable)

    fun onException(ex:Exception)

}

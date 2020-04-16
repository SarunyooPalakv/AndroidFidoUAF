package com.project.fidouafetda.callback

import com.project.fidouafetda.model.RegistrationRequest


interface CallbackListener {

    fun callBackRegRequest(response: List<RegistrationRequest>)

    fun callTransFactsList(response: String)

    fun onStatusError(response: String)

    fun onBodyError(response: String)

    fun onFailure(t: Throwable)

    fun onException(ex:Exception)

}

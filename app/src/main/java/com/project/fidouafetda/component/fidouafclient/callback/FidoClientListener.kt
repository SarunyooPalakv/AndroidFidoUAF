package com.project.fidouafetda.callback



interface FidoClientListener {

    fun callTransFactsList(response: String)

    fun onStatusError(response: String)

    fun onBodyError(response: String)

    fun onFailure(t: Throwable)

    fun onException(ex:Exception)

}

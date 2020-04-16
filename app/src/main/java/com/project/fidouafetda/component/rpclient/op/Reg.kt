package com.project.fidouafetda.component.rpclient.op

import com.project.fidouafetda.callback.RPClientListener
import com.project.fidouafetda.curl.Curl
import com.project.fidouafetda.model.LoginRequest
import com.project.fidouafetda.model.LoginResponse
import com.project.fidouafetda.model.RegistrationRequest
import com.project.fidouafetda.model.RegistrationResponse


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class Reg {

    private val curl = Curl()
    private val login = LoginRequest()

    fun getUafMsgRegRequest(username: String, password: String, listener: RPClientListener) {

        try {

            login.username = username
            login.password = password

            val serverResponse = curl.httpRequest().regRequest(login)
            serverResponse.enqueue(object : Callback<List<RegistrationRequest>> {
                override fun onResponse(call: Call<List<RegistrationRequest>>, response: Response<List<RegistrationRequest>>) {

                    val respBody = response.body()
                    val respError = response.errorBody()

                    if(response.code() != 200 )
                    {
                        listener.onStatusError("Msg RegRequest Error Status code: ${response.code()}->${respError!!.string()}")
                    }else{
                        if(respBody == null|| respBody.isEmpty()){
                            listener.onBodyError("Msg RegRequest Error Status code: ${response.code()}->${respError!!.string()}")
                        }else{
                            listener.callBackRegRequest(respBody)
                        }
                    }
                }
                override fun onFailure(call: Call<List<RegistrationRequest>>, t: Throwable) {
                    listener.onFailure(t)
                }
            })
        }catch(ex: Exception){
            listener.onException(ex)
        }
    }

    fun getUafMsgRegResponse(msgResponse: ArrayList<RegistrationResponse>, listener: RPClientListener) {

        try {

            val serverResponse = curl.httpRequest().regResponse(msgResponse)
            serverResponse.enqueue(object : Callback<List<LoginResponse>> {
                override fun onResponse(call: Call<List<LoginResponse>>, response: Response<List<LoginResponse>>) {

                    val respBody = response.body()
                    val respError = response.errorBody()

                    if(response.code() != 200 )
                    {
                        listener.onStatusError("Msg RegResponse Error Status code: ${response.code()}->${respError!!.string()}")
                    }else{
                        if(respBody == null|| respBody.isEmpty()){
                            listener.onBodyError("Msg RegResponse Error Status code: ${response.code()}->${respError!!.string()}")
                        }else{
                            listener.callBackRegResponse(respBody)
                        }
                    }
                }
                override fun onFailure(call: Call<List<LoginResponse>>, t: Throwable) {
                    listener.onFailure(t)
                }
            })
        }catch(ex: Exception){
            listener.onException(ex)
        }
    }
}
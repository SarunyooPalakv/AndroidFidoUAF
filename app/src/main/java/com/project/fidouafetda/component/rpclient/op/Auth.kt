package com.project.fidouafetda.component.rpclient.op

import com.project.fidouafetda.callback.RPClientListener
import com.project.fidouafetda.curl.Curl
import com.project.fidouafetda.model.AuthenticationRequest
import com.project.fidouafetda.model.AuthenticationResponse
import com.project.fidouafetda.model.PurchaseRequest
import com.project.fidouafetda.model.PurchaseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class Auth {

    private val curl = Curl()
    private val purchase = PurchaseRequest()

    fun getUafMsgAuthRequest(username: String, listener: RPClientListener) {

        try {

            purchase.username = username

            val serverResponse = curl.httpRequest().authRequest(purchase)
            serverResponse.enqueue(object : Callback<List<AuthenticationRequest>> {
                override fun onResponse(call: Call<List<AuthenticationRequest>>, response: Response<List<AuthenticationRequest>>) {

                    val respBody = response.body()
                    val respError = response.errorBody()

                    if(response.code() != 200 )
                    {
                        listener.onStatusError("Msg AuthRequest Error Status code: ${response.code()}->${respError!!.string()}")
                    }else{
                        if(respBody == null|| respBody.isEmpty()){
                            listener.onBodyError("Msg AuthRequest Error Status code: ${response.code()}->${respError!!.string()}")
                        }else{
                            listener.callBackAuthRequest(respBody)
                        }
                    }
                }
                override fun onFailure(call: Call<List<AuthenticationRequest>>, t: Throwable) {
                    listener.onFailure(t)
                }
            })
        }catch(ex: Exception){
            listener.onException(ex)
        }
    }

    fun getUafMsgAuthResponse(msgResponse: ArrayList<AuthenticationResponse>, listener: RPClientListener) {

        try {

            val serverResponse = curl.httpRequest().authResponse(msgResponse)
            serverResponse.enqueue(object : Callback<List<PurchaseResponse>> {
                override fun onResponse(call: Call<List<PurchaseResponse>>, response: Response<List<PurchaseResponse>>) {

                    val respBody = response.body()
                    val respError = response.errorBody()

                    if(response.code() != 200 )
                    {
                        listener.onStatusError("Msg AuthResponse Error Status code: ${response.code()}->${respError!!.string()}")
                    }else{
                        if(respBody == null|| respBody.isEmpty()){
                            listener.onBodyError("Msg AuthResponse Error Status code: ${response.code()}->${respError!!.string()}")
                        }else{
                            listener.callBackAuthResponse(respBody)
                        }
                    }
                }
                override fun onFailure(call: Call<List<PurchaseResponse>>, t: Throwable) {
                    listener.onFailure(t)
                }
            })
        }catch(ex: Exception){
            listener.onException(ex)
        }
    }
}
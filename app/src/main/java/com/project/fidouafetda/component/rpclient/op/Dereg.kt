package com.project.fidouafetda.component.rpclient.op

import com.project.fidouafetda.callback.RPClientListener
import com.project.fidouafetda.curl.Curl
import com.project.fidouafetda.model.Deregister
import com.project.fidouafetda.model.DeregistrationRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class Dereg {

    private val curl = Curl()
    private val dereg = Deregister()

    fun getUafMsgDereqRequest(username: String, listener: RPClientListener) {

        try {

            dereg.username = username

            val serverResponse = curl.httpRequest().deregRequest(dereg)
            serverResponse.enqueue(object : Callback<List<DeregistrationRequest>> {
                override fun onResponse(call: Call<List<DeregistrationRequest>>, response: Response<List<DeregistrationRequest>>) {

                    val respBody = response.body()
                    val respError = response.errorBody()

                    if(response.code() != 200 )
                    {
                        listener.onStatusError("Msg DeregRequest Error Status code: ${response.code()}->${respError!!.string()}")
                    }else{
                        if(respBody == null|| respBody.isEmpty()){
                            listener.onBodyError("Msg DeregRequest Error Status code: ${response.code()}->${respError!!.string()}")
                        }else{
                            listener.callBackDeregRequest(respBody)
                        }
                    }
                }
                override fun onFailure(call: Call<List<DeregistrationRequest>>, t: Throwable) {
                    listener.onFailure(t)
                }
            })
        }catch(ex: Exception){
            listener.onException(ex)
        }
    }
}
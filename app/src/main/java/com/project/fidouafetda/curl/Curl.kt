package com.project.fidouafetda.curl

import com.project.fidouafetda.network.Constants.URL_SERVER
import com.project.fidouafetda.network.FidoService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class Curl {

    fun httpRequest():FidoService{

        try {

            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(URL_SERVER)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(FidoService::class.java)
        }catch (ex: Exception){
            throw ex
        }
    }
}
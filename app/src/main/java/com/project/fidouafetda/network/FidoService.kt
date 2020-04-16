package com.project.fidouafetda.network


import com.project.fidouafetda.model.*
import retrofit2.Call
import retrofit2.http.*


interface FidoService {

    @POST("regRequest")
    fun regRequest(@Body regGetRequest: LoginRequest): Call<List<RegistrationRequest>>

    @POST("regResponse")
    fun regResponse(@Body regGetResponse: ArrayList<RegistrationResponse>): Call<List<LoginResponse>>

    @POST("authRequest")
    fun authRequest(@Body authGetRequest: PurchaseRequest): Call<List<AuthenticationRequest>>

    @POST("authResponse")
    fun authResponse(@Body authGetRequest: ArrayList<AuthenticationResponse>): Call<List<PurchaseResponse>>

    @POST("deregRequest")
    fun deregRequest(@Body authGetRequest: Deregister): Call<List<DeregistrationRequest>>

    @GET
    fun facetListRequest(@Url url: String):Call<TrustedFacetsList>


}
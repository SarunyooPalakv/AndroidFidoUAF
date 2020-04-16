package com.project.fidouafetda.component.fidouafclient.op

import com.google.gson.GsonBuilder
import com.project.fidouafetda.model.AuthenticationRequest
import com.project.fidouafetda.model.FinalChallengeParams
import com.project.fidouafetda.model.RegistrationRequest
import java.util.*

class FinalChallenge {

    private val gson = GsonBuilder().disableHtmlEscaping().create()

    fun getFinalChallenge(msgRequest: String, signapk: String): String {

        val fcParams = FinalChallengeParams()

        if(msgRequest.contains("Reg")){

            val objRegRequest =
                gson.fromJson(msgRequest, Array<RegistrationRequest>::class.java)[0]

            fcParams.appID = objRegRequest.header!!.appID
            fcParams.facetID = signapk
            fcParams.challenge = objRegRequest.challenge

            fcParams.channelBinding?.cid_pubkey = ""
            fcParams.channelBinding?.serverEndPoint = ""
            fcParams.channelBinding?.tlsServerCertificate = ""
            fcParams.channelBinding?.tlsUnique = ""
        }
        if(msgRequest.contains("Auth")){

            val objAuthRequest =
                gson.fromJson(msgRequest, Array<AuthenticationRequest>::class.java)[0]
            fcParams.appID = objAuthRequest.header!!.appID
            fcParams.facetID = signapk
            fcParams.challenge = objAuthRequest.challenge

            fcParams.channelBinding?.cid_pubkey = ""
            fcParams.channelBinding?.serverEndPoint = ""
            fcParams.channelBinding?.tlsServerCertificate = ""
            fcParams.channelBinding?.tlsUnique = ""
        }

        return Base64.getUrlEncoder().encodeToString(
            gson.toJson(fcParams).toByteArray(charset("UTF-8"))
        )
    }
}
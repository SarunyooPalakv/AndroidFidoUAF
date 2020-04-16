package com.project.fidouafetda.component.fidouafclient.op

import com.google.gson.Gson
import com.project.fidouafetda.component.fidouafclient.asm.ConsAuthInfo
import com.project.fidouafetda.model.AuthenticationRequest
import com.project.fidouafetda.model.AuthenticationResponse
import com.project.fidouafetda.model.AuthenticatorInfo
import com.project.fidouafetda.model.AuthenticatorSignAssertion
import org.json.JSONObject

class Auth {

    private val gson = Gson()
    private val fcp = FinalChallenge()
    private val assn = AuthAssertion()

    fun authen(authMsgRequest: String, signapk: String, authKeyID: String): String {
        try {
            val getInfo =
                JSONObject(ConsAuthInfo.getInfoJson).getJSONArray("Authenticators")[0]
            val authInfo =
                gson.fromJson(getInfo.toString(), AuthenticatorInfo::class.java)

            val authRequest =
                gson.fromJson(authMsgRequest, Array<AuthenticationRequest>::class.java)[0]

            val finalChallenge = fcp.getFinalChallenge(authMsgRequest, signapk)
            val assertion = assn.authAssertion(finalChallenge, authKeyID)
            val assertionScheme = authInfo.assertionScheme

            val authResponse = AuthenticationResponse()
            authResponse.assertions = arrayOfNulls(1)
            authResponse.assertions!![0] = AuthenticatorSignAssertion()
            authResponse.assertions!![0]?.assertion = assertion
            authResponse.assertions!![0]?.assertionScheme = assertionScheme
            authResponse.header = authRequest.header
            authResponse.fcParams = finalChallenge

            return gson.toJson(authResponse)

        } catch (e: Exception) {
            throw e
        }
    }

}
package com.project.fidouafetda.component.fidouafclient.op


import com.google.gson.Gson
import com.project.fidouafetda.component.fidouafclient.asm.ConsAuthInfo
import com.project.fidouafetda.model.*
import org.json.JSONObject


class Reg {

    private val assn = RegAssertion()
    private val fcp = FinalChallenge()
    private val gson = Gson()

    fun register(regMsgRequest: String, signapk: String): String{
        try {

            val getInfo =
                JSONObject(ConsAuthInfo.getInfoJson).getJSONArray("Authenticators")[0]
            val authInfo =
                gson.fromJson(getInfo.toString(), AuthenticatorInfo::class.java)

            val regRequest =
                gson.fromJson(regMsgRequest, Array<RegistrationRequest>::class.java)[0]

            val finalChallenge = fcp.getFinalChallenge(regMsgRequest, signapk)
            val assertion = assn.regAssertion(regRequest, finalChallenge)
            val assertionScheme = authInfo.assertionScheme


            //Register response
            val regResponse = RegistrationResponse()
            regResponse.assertions = arrayOfNulls(1)
            regResponse.assertions!![0] = AuthenticatorRegistrationAssertion()
            regResponse.assertions!![0]?.assertion = assertion
            regResponse.assertions!![0]?.assertionScheme = assertionScheme
            regResponse.header = regRequest.header
            regResponse.fcParams = finalChallenge

            return gson.toJson(regResponse)


        }catch (e: Exception){
            throw e
        }
    }
}
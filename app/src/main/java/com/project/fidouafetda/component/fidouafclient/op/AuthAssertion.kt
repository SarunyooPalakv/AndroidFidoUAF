package com.project.fidouafetda.component.fidouafclient.op

import com.google.gson.Gson
import com.project.fidouafetda.component.fidouafclient.asm.ConsAuthInfo
import com.project.fidouafetda.component.fidouafclient.crypto.Keygen
import com.project.fidouafetda.component.fidouafclient.tlv.Bytes
import com.project.fidouafetda.component.fidouafclient.tlv.Hex
import com.project.fidouafetda.component.fidouafclient.tlv.TLV
import com.project.fidouafetda.component.fidouafclient.tlv.Tag
import com.project.fidouafetda.model.AuthenticationRequest
import com.project.fidouafetda.model.AuthenticatorInfo
import org.json.JSONObject
import java.lang.StringBuilder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class AuthAssertion {

    private val tag = Tag()
    private val tlv = TLV()
    private val sethex = Hex()
    private val gson = Gson()
    private val keygen = Keygen()
    private val setbytes = Bytes()
    private lateinit var finalChallengeParams: String
    private lateinit var KeyID: String
    private var md = MessageDigest.getInstance("SHA-256")


    fun authAssertion(finalChallenge: String, authKeyID: String): String{

        finalChallengeParams = finalChallenge
        KeyID = authKeyID
        return getTagAssertion()
    }

    private fun getTagAssertion(): String {

        val tag_finalChallengeHash = getTagfCHash()
        val tag_counters = getTagCounter()
        val tag_keyID = getTagkeyID()
        val tag_aaid = getTagAaid()
        val tag_assertionInfo = getTagAssnInfo()
        val tag_assertionNonce = getTagAssnNonce()
        val tag_transContentHash = getTransCH()
        val tag_signData = getSignData(
            tag_aaid,
            tag_assertionInfo,
            tag_assertionNonce,
            tag_finalChallengeHash,
            tag_transContentHash,
            tag_keyID,
            tag_counters
        )

        val tag_signature = getSignature(tag_signData)
        val tag_authAssn = getTagAuthAssn(tag_signData,tag_signature)

        return Base64.getUrlEncoder()
            .encodeToString(setbytes.hexToBytes(tag_authAssn))
    }

    private fun getTagAuthAssn(tagSigndata: String, tagSignature: String): String{

        val authAssnMsg = StringBuilder()
        authAssnMsg.append(tagSigndata)
        authAssnMsg.append(tagSignature)

        val tagAuthAssn = tag.taG_UAFV1_AUTH_ASSERTION
        val valueAuthAssn = authAssnMsg.toString()
        val lenAuthAssn = setbytes.hexToBytes(valueAuthAssn).size

        return tlv.setTLV(tagAuthAssn, lenAuthAssn, valueAuthAssn)
    }

    private fun getSignature(tag_signData: String): String{

        val tagSignature = tag.taG_SIGNATURE
        val valueSignature = keygen.sign(KeyID, tag_signData)
        val lenSignature = setbytes.hexToBytes(valueSignature).size

        return tlv.setTLV(tagSignature, lenSignature, valueSignature)
    }

    private fun getSignData(
        tag_aaid: String, tag_assertionInfo: String, tag_assertionNonce: String,
        tag_finalChallengeHash: String, tag_transContentHash: String, tag_keyID: String,
        tag_counters: String
    ): String {

        val SignDataMsg = StringBuilder()
        SignDataMsg.append(tag_aaid)
        SignDataMsg.append(tag_assertionInfo)
        SignDataMsg.append(tag_assertionNonce)
        SignDataMsg.append(tag_finalChallengeHash)
        SignDataMsg.append(tag_transContentHash)
        SignDataMsg.append(tag_keyID)
        SignDataMsg.append(tag_counters)

        val tagSignData = tag.taG_UAFV1_SIGNED_DATA
        val valueSignData = SignDataMsg.toString()
        val lenSignData = setbytes.hexToBytes(valueSignData).size

        return tlv.setTLV(tagSignData, lenSignData, valueSignData)

    }

    private fun getTransCH(): String{

        val tagTransContentHash = tag.taG_TRANSACTION_CONTENT_HASH
        val lenTransContentHash = 0
        val valueTransContentHash = ""

        return tlv.setTLV(tagTransContentHash, lenTransContentHash, valueTransContentHash)

    }

    private fun getTagAssnNonce(): String{

        val sr = SecureRandom()
        val rndBytes = ByteArray(10)
        sr.nextBytes(rndBytes)

        val tagAssertionNonce = tag.taG_AUTHENTICATOR_NONCE
        val valueAssertionNonce = setbytes.bytesToHex(rndBytes)
        val lenAssertionNonce = rndBytes.size

        return tlv.setTLV(tagAssertionNonce, lenAssertionNonce, valueAssertionNonce)
    }

    private fun getTagAssnInfo(): String{

        //Authenticator Info
        val getInfo =
            JSONObject(ConsAuthInfo.getInfoJson).getJSONArray("Authenticators")[0]
        val authInfo =
            gson.fromJson(getInfo.toString(), AuthenticatorInfo::class.java)

        val hexAuthenticatorversion =
            sethex.hex16(Integer.toHexString(authInfo.authenticatorVersion as Int))
        val hexAuthenticationmode = sethex.hex8(Integer.toHexString(0x01))
        val hexSignaturealgandencoding =
            sethex.hex16(Integer.toHexString(authInfo.authenticationAlgorithm!![0]))

        val assnInfoMsg = StringBuilder()
        assnInfoMsg.append(hexAuthenticatorversion)
        assnInfoMsg.append(hexAuthenticationmode)
        assnInfoMsg.append(hexSignaturealgandencoding)

        val tagAssertionInfo = tag.taG_ASSERTION_INFO
        val valueAssertionInfo = assnInfoMsg.toString()
        val lenAssertionInfo = setbytes.hexToBytes(valueAssertionInfo).size

        return tlv.setTLV(tagAssertionInfo, lenAssertionInfo, valueAssertionInfo)
    }

    private fun getTagAaid(): String{

        //Authenticator Info
        val getInfo =
            JSONObject(ConsAuthInfo.getInfoJson).getJSONArray("Authenticators")[0]
        val authInfo =
            gson.fromJson(getInfo.toString(), AuthenticatorInfo::class.java)

        //region TAG_AAID
        val aaidInfo = authInfo.aaid!![0].toByteArray(charset("UTF-8"))

        val tagAaid = tag.taG_AAID
        val lenAaid = aaidInfo.size
        val valueAaid = setbytes.bytesToHex(aaidInfo)

        return tlv.setTLV(tagAaid, lenAaid, valueAaid)
    }

    private fun getTagkeyID(): String{

        val tagKeyid = tag.taG_KEYID
        val lenKeyid = KeyID.toByteArray(charset("UTF-8")).size
        val valueKeyid = setbytes.bytesToHex(KeyID.toByteArray(charset("UTF-8")))

        return tlv.setTLV(tagKeyid, lenKeyid, valueKeyid)
    }

    private fun getTagCounter(): String{

        val tagCounters = tag.taG_COUNTERS
        val valueCounters = sethex.hex32(Integer.toHexString(0))
        val lenCounters = setbytes.hexToBytes(valueCounters).size

        return tlv.setTLV(tagCounters, lenCounters, valueCounters)

    }

    private fun getTagfCHash(): String{

        val finalChallengeHash = md.digest(finalChallengeParams.toByteArray(charset("UTF-8")))

        val tagFinalChallengeHash = tag.taG_FINAL_CHALLENGE_HASH
        val lenFinalChallengeHash = finalChallengeHash.size
        val valueFinalChallengeHash = setbytes.bytesToHex(finalChallengeHash)

        return tlv.setTLV(tagFinalChallengeHash, lenFinalChallengeHash, valueFinalChallengeHash)
    }
}
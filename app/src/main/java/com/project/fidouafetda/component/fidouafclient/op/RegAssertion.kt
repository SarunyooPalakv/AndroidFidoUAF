package com.project.fidouafetda.component.fidouafclient.op

import com.google.gson.Gson
import com.project.fidouafetda.component.fidouafclient.asm.ConsAuthInfo
import com.project.fidouafetda.component.fidouafclient.crypto.Keygen
import com.project.fidouafetda.component.fidouafclient.tlv.Bytes
import com.project.fidouafetda.component.fidouafclient.tlv.Hex
import com.project.fidouafetda.component.fidouafclient.tlv.TLV
import com.project.fidouafetda.component.fidouafclient.tlv.Tag
import com.project.fidouafetda.model.AuthenticatorInfo
import com.project.fidouafetda.model.RegistrationRequest
import org.json.JSONObject
import java.lang.StringBuilder
import java.security.MessageDigest
import java.util.*

class RegAssertion {

    private val tag = Tag()
    private val tlv = TLV()
    private val setbytes = Bytes()
    private val sethex = Hex()
    private val keygen = Keygen()
    private val gson = Gson()
    private var md = MessageDigest.getInstance("SHA-256")
    private lateinit var username: String
    private lateinit var challenge: String
    private lateinit var keyID: String
    private lateinit var finalChallengeParams: String


    fun regAssertion(regRequest: RegistrationRequest, finalChallenge: String): String {

        username = regRequest.username as String
        challenge = regRequest.challenge as String
        keyID = keyIDgen(username)
        finalChallengeParams = finalChallenge

        return getTagAssertion()
    }

    private fun getTagAssertion(): String {

        val tag_PubKey = getTagPubkey()
        val tag_finalChallengeHash = getTagfCHash()
        val tag_keyID = getTagkeyID()
        val tag_counters = getTagCounter()
        val tag_aaid = getTagAaid()
        val tag_assertionInfo = getTagAssnInfo()
        val tag_krd = getTagKrd(
            tag_aaid,
            tag_assertionInfo,
            tag_finalChallengeHash,
            tag_keyID,
            tag_counters,
            tag_PubKey
        )

        val tag_signature = getSignature(tag_krd)
        val tag_attestationCert = getAttestationCert()
        //val tag_attesnBSG = getTagAttesnBSG(tag_signature)
        val tag_attesnBF = getTagAttesnBF(tag_signature, tag_attestationCert)
        val tag_regAssn = getTagRegAssn(tag_krd, tag_attesnBF)

        return Base64.getUrlEncoder()
            .encodeToString(setbytes.hexToBytes(tag_regAssn))
    }


    private fun getTagRegAssn(tag_Krd: String, tag_attesnBF: String): String {

        val regAssnMsg = StringBuilder()
        regAssnMsg.append(tag_Krd)
        regAssnMsg.append(tag_attesnBF)

        val tagRegAssn = tag.taG_UAFV1_REG_ASSERTION
        val valueRegAssn = regAssnMsg.toString()
        val lenRegAssn = setbytes.hexToBytes(valueRegAssn).size

        return tlv.setTLV(tagRegAssn, lenRegAssn, valueRegAssn)
    }

    private fun getTagAttesnBF(tagSignature: String, tag_attestationCert: String): String {

        val attesnBFMsg = StringBuilder()
        attesnBFMsg.append(tagSignature)
        attesnBFMsg.append(tag_attestationCert)

        val tagAttesnBF = tag.taG_ATTESTATION_BASIC_FULL
        val valueAttesnBF = attesnBFMsg.toString()
        val lenAttesnBF = setbytes.hexToBytes(valueAttesnBF).size

        return tlv.setTLV(tagAttesnBF, lenAttesnBF, valueAttesnBF)
    }

    /*private fun getTagAttesnBSG(tag_Signature: String): String {

        val tagSignature = tag.taG_ATTESTATION_BASIC_SURROGATE
        val lenSignature = setbytes.hexToBytes(tag_Signature).size

        return tlv.setTLV(tagSignature, lenSignature, tag_Signature)
    }*/

    private fun getAttestationCert(): String {

        val tagAttestationCert = tag.taG_ATTESTATION_CERT
        val valueAttestationCert= keygen.getCertificateChain(keyID)
        val lenSignature = setbytes.hexToBytes(valueAttestationCert).size

        return tlv.setTLV(tagAttestationCert, lenSignature, valueAttestationCert)
    }

    private fun getSignature(tag_krd: String): String {

        val tagSignature = tag.taG_SIGNATURE
        val valueSignature = keygen.sign(keyID, tag_krd)
        val lenSignature = setbytes.hexToBytes(valueSignature).size

        return tlv.setTLV(tagSignature, lenSignature, valueSignature)
    }

    private fun getTagKrd(
        tagAaid: String, tagAssertioninfo: String, tagFinalchallengehash: String,
        tagKeyid: String, tagCounters: String, tagPubkey: String
    ): String {

        val KrdMsg = StringBuilder()
        KrdMsg.append(tagAaid)
        KrdMsg.append(tagAssertioninfo)
        KrdMsg.append(tagFinalchallengehash)
        KrdMsg.append(tagKeyid)
        KrdMsg.append(tagCounters)
        KrdMsg.append(tagPubkey)

        val tagKrd = tag.taG_UAFV1_KRD
        val valueKrd = KrdMsg.toString()
        val lenKrd = setbytes.hexToBytes(valueKrd).size

        return tlv.setTLV(tagKrd, lenKrd, valueKrd)

    }

    //region TAG_ASSERTION_INFO
    private fun getTagAssnInfo(): String {

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
        val hexPublickeyalgandencoding = sethex.hex16(Integer.toHexString(0x01))

        val assnInfoMsg = StringBuilder()
        assnInfoMsg.append(hexAuthenticatorversion)
        assnInfoMsg.append(hexAuthenticationmode)
        assnInfoMsg.append(hexSignaturealgandencoding)
        assnInfoMsg.append(hexPublickeyalgandencoding)

        val tagAssertionInfo = tag.taG_ASSERTION_INFO
        val valueAssertionInfo = assnInfoMsg.toString()
        val lenAssertionInfo = setbytes.hexToBytes(valueAssertionInfo).size

        return tlv.setTLV(tagAssertionInfo, lenAssertionInfo, valueAssertionInfo)

    }

    //region TAG_AAID
    private fun getTagAaid(): String {

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

    //TAG_KEYID
    private fun getTagkeyID(): String {

        val tagKeyid = tag.taG_KEYID
        val lenKeyid = keyID.toByteArray(charset("UTF-8")).size
        val valueKeyid = setbytes.bytesToHex(keyID.toByteArray(charset("UTF-8")))

        return tlv.setTLV(tagKeyid, lenKeyid, valueKeyid)
    }

    //TAG_FINAL_CHALLENGE_HASH
    private fun getTagfCHash(): String {

        val finalChallengeHash = md.digest(finalChallengeParams.toByteArray(charset("UTF-8")))

        val tagFinalChallengeHash = tag.taG_FINAL_CHALLENGE_HASH
        val lenFinalChallengeHash = finalChallengeHash.size
        val valueFinalChallengeHash = setbytes.bytesToHex(finalChallengeHash)

        return tlv.setTLV(tagFinalChallengeHash, lenFinalChallengeHash, valueFinalChallengeHash)
    }

    //TAG_PUBLICKEY
    private fun getTagPubkey(): String {

        val publicKey = keygen.keypair(keyID, challenge.toByteArray(charset("UTF-8")))

        val tagPubKey = tag.taG_PUB_KEY
        val lenPubKey = keygen.keypair(keyID, challenge.toByteArray(charset("UTF-8"))).encoded.size
        val valuePubKey = setbytes.bytesToHex(publicKey.encoded)

        return tlv.setTLV(tagPubKey, lenPubKey, valuePubKey)

    }

    private fun getTagCounter(): String {

        val hex32Sgn = sethex.hex32(Integer.toHexString(0))
        val hex32Reg = sethex.hex32(Integer.toHexString(0))

        val tagCounters = tag.taG_COUNTERS
        val valueCounters = hex32Sgn + hex32Reg
        val lenCounters = setbytes.hexToBytes(valueCounters).size

        return tlv.setTLV(tagCounters, lenCounters, valueCounters)
    }


    private fun keyIDgen(username: String): String {

        /*val unixTime = System.currentTimeMillis() / 1000L
        val random = String(SecureRandom().generateSeed(20))
        val randMsg = StringBuilder()
        randMsg.append(username)
        randMsg.append(random)
        randMsg.append(unixTime)
        val keyrand = MessageDigest.getInstance("SHA-256")
            .digest((randMsg.toString()).toByteArray(charset("UTF-8")))*/

        //return Base64.getUrlEncoder().encodeToString(keyrand)
        return "hsf4z5PCOhnZExMeVloZZmK0hxaSi10tkY_c4xxczxc"
    }
}
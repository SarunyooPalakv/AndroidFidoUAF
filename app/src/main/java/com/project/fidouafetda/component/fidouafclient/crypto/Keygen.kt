package com.project.fidouafetda.component.fidouafclient.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import com.project.fidouafetda.component.fidouafclient.tlv.Bytes
import java.security.*
import java.security.KeyStore.getInstance
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec


class Keygen {

    private val setbytes = Bytes()
    private val ks: KeyStore = getInstance("AndroidKeyStore").apply { load(null) }

    fun keypair(KeyAlias: String, challengeBytes: ByteArray): PublicKey {

        try {
            val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            kpg.initialize(KeyGenParameterSpec.Builder(KeyAlias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                    .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setAttestationChallenge(challengeBytes).build())

            // Check that private key is inside secure hardware
            val keyPair = kpg.genKeyPair()
            val factory = KeyFactory.getInstance(keyPair.private.algorithm, "AndroidKeyStore")
            val keyInfo = factory.getKeySpec(keyPair.private, KeyInfo::class.java)

            if(keyInfo.isInsideSecureHardware){
                //Publickey
                return keyPair.public
            }else{
                throw Exception("Inside Secure Hardware  Invalid")
            }

        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }

    fun sign(KeyAlias: String, tag_krd: String): String {

        try {

            //Alias
            val entry = ks.getEntry(KeyAlias, null) as? KeyStore.PrivateKeyEntry ?: throw Exception("Privatekey Invalid")

            //signChallenge
            val ba64Sign = setbytes.hexToBytes(tag_krd)
            val signature: ByteArray = Signature.getInstance("SHA256withECDSA").run {
                initSign(entry.privateKey)
                update(ba64Sign)
                sign()
            }
            getCertificateChain(KeyAlias)
            return setbytes.bytesToHex(signature)

        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }

    fun deleteKey(KeyAlias: String): Boolean {

        return try {

            ks.deleteEntry(KeyAlias)
            true

        } catch (ex: Exception) {
            false
        }
    }


    fun getCertificateChain(KeyAlias: String): String{

        try {

            val attesCert = ks.getCertificateChain(KeyAlias)[1] as X509Certificate
            val str_attesCert = attesCert.toString()
            val byte_attesCert = str_attesCert.toByteArray(charset("UTF-8"))

            return setbytes.bytesToHex(byte_attesCert)

        } catch (ex: Exception) {
            throw ex
        }
    }
}




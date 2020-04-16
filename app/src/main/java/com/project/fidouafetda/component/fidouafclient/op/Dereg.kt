package com.project.fidouafetda.component.fidouafclient.op



import com.project.fidouafetda.component.fidouafclient.crypto.Keygen

class Dereg {

    private val key = Keygen()
    private val SUCCESS = "SUCCESS"
    private val FAIL = "FAIL"

    fun dereg(deregkeyID: String): String{

        try {
            val deletedKey = key.deleteKey(deregkeyID)

            return if (deletedKey) {
                SUCCESS
            } else {
                FAIL
            }

        }catch (e: Exception){
            throw e
        }
    }
}
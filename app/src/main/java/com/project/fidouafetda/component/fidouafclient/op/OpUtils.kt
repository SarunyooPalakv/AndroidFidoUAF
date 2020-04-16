package com.project.fidouafetda.component.fidouafclient.op

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.project.fidouafetda.callback.FidoClientListener
import com.project.fidouafetda.callback.RPClientListener
import com.project.fidouafetda.curl.Curl
import com.project.fidouafetda.model.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


@Suppress("DEPRECATION", "SENSELESS_COMPARISON")
class OpUtils {


    private val curl = Curl()
    private val gson = GsonBuilder().disableHtmlEscaping().create()

    fun getTrustedFacetsLists(inputMsgReq: String,listener: FidoClientListener) {

        try {

            val uafProMsgArray = JSONArray(inputMsgReq)
            val appID = (uafProMsgArray.get(0) as JSONObject).getJSONObject("header").getString("appID")

            if (appID.contains("http")) {

                val facetListResponse = curl.httpRequest().facetListRequest(appID)
                facetListResponse.enqueue(object : Callback<TrustedFacetsList> {
                    override fun onResponse(
                        call: Call<TrustedFacetsList>,
                        response: Response<TrustedFacetsList>
                    ) {

                        val respBody = response.body()
                        val respError = response.errorBody()
                        if (response.code() != 200) {
                            listener.onStatusError("Msg RegRequest Error Status code: ${response.code()}->${respError!!.string()}")

                        } else {
                            if (respBody == null || respBody.toString().isEmpty()) {
                                listener.onBodyError("Msg RegRequest Error Status code: ${response.code()}->${respError!!.string()}")
                            } else {
                                listener.callTransFactsList(gson.toJson(respBody))
                            }
                        }
                    }
                    override fun onFailure(call: Call<TrustedFacetsList>, t: Throwable) {
                        listener.onFailure(t)
                    }
                })
            }else{
                listener.callTransFactsList(gson.toJson(null))
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun getRuleFacetsList(inputMsgReq: String, signapk: String, transFactListJson: String?,
                              context: Context, isTrx: Boolean): String {
        try {

            val uafProMsgArray = JSONArray(inputMsgReq)
            val appID = (uafProMsgArray.get(0) as JSONObject).getJSONObject("header").getString("appID")
            val version = Gson().fromJson((uafProMsgArray.get(0) as JSONObject).getJSONObject("header").getString("upv"), Version::class.java)

            // If the AppID is null or empty, the client MUST set the AppID to be the FacetID of
            // the caller, and the operation may proceed without additional processing.

            if (appID == null || appID.isEmpty()) {
                if (checkAppSignature(signapk, context)) {
                    (uafProMsgArray.get(0) as JSONObject).getJSONObject("header").put("appID", signapk)
                }
            } else {
                //If the AppID is not an HTTPS URL, and matches the FacetID of the caller, no additional
                // processing is necessary and the operation may proceed.
                if (appID.contains("http")) {
                    // Begin to fetch the Trusted Facet List using the HTTP GET method
                    // val trustedFacetsJson = getTrustedFacets(appID,listener)
                    val trustedFacet = gson.fromJson(transFactListJson, TrustedFacetsList::class.java)

                    // After processing the trustedFacets entry of the correct version and removing
                    // any invalid entries, if the caller's FacetID matches one listed in ids,
                    // the operation is allowed.

                    val facetFound = processTrustedFacetsList(trustedFacet, version, signapk)
                    if (!facetFound || !checkAppSignature(signapk, context)) {
                        throw Exception("Not Found RP's FacetId from TransFacetsList")
                    }
                } else {
                    if(signapk != appID){
                        throw Exception("appID not equals as facetID")
                    }else {
                        if (!checkAppSignature(signapk, context)) {
                            throw Exception("Signature executed on runtime Not matches to User agent")
                        }
                    }
                }
            }
            if (isTrx) {

            }
             return uafProMsgArray.toString()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * A double check about app signature that was passed by MainActivity as facetID.
     * @param facetId a string value composed by app hash. I.e. android:apk-key-hash:Lir5oIjf552K/XN4bTul0VS3GfM
     * @param context Application Context
     * @return true if the signature executed on runtime matches if signature sent by MainActivity
     */

    @SuppressLint("PackageManagerGetSignatures", "DefaultLocale")
    private fun checkAppSignature(facetId: String, context: Context): Boolean {
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (sign in packageInfo.signatures) {
                val messageDigest = MessageDigest.getInstance("SHA1")
                messageDigest.update(sign.toByteArray())
                val currentSignature = Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT)
                if (currentSignature.toLowerCase().contains(facetId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2].toLowerCase())) {
                    return true
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            throw e
        } catch (e: NoSuchAlgorithmException) {
            throw e
        }

        return false
    }

    /**
     * From among the objects in the trustedFacet array, select the one with the version matching
     * that of the protocol message version. The scheme of URLs in ids MUST identify either an
     * application identity (e.g. using the apk:, ios: or similar scheme) or an https: Web Origin [RFC6454].
     * Entries in ids using the https:// scheme MUST contain only scheme, host and port components,
     * with an optional trailing /. Any path, query string, username/password, or fragment information
     * MUST be discarded.
     * @param trustedFacetsList
     * @param version
     * @param facetId
     * @return true if appID list contains facetId (current Android application's signature).
     */
    private fun processTrustedFacetsList(
        trustedFacetsList: TrustedFacetsList, version: Version, facetId: String
    ): Boolean {
        for (trustedFacets in trustedFacetsList.getTrustedFacets()!!) {
            // select the one with the version matching that of the protocol message version
            if (trustedFacets.getVersion()!!.minor >= version.minor && trustedFacets.getVersion()!!.major <= version.major) {
                //The scheme of URLs in ids MUST identify either an application identity
                // (e.g. using the apk:, ios: or similar scheme) or an https: Web Origin [RFC6454].
                for (id in trustedFacets.getIds()!!) {
                    if (id == facetId) {
                        return true
                    }
                }
            }
        }
        return false
    }
}






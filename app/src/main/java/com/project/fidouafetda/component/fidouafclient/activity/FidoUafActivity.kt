package com.project.fidouafetda.component.fidouafclient.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.project.fidouafetda.R
import com.project.fidouafetda.callback.FidoClientListener
import com.project.fidouafetda.component.fidouafclient.asm.ConsAuthInfo.getInfoJson
import com.project.fidouafetda.component.fidouafclient.op.Auth
import com.project.fidouafetda.component.fidouafclient.op.Dereg
import com.project.fidouafetda.component.fidouafclient.op.OpUtils
import com.project.fidouafetda.component.fidouafclient.op.Reg
import com.project.fidouafetda.model.*
import kotlinx.android.synthetic.main.activity_fidoclient.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory


@Suppress("DEPRECATION")
@SuppressLint("PackageManagerGetSignatures")
class FidoUafActivity : AppCompatActivity() {


    private val opUtils = OpUtils()
    private val gson = Gson()
    private val regOp = Reg()
    private val authOp = Auth()
    private val deregOp = Dereg()
    private var layoutfun: Int? = null
    private val REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1

    private lateinit var signapk: String
    private lateinit var authKeyID: String
    private lateinit var deregKeyID: String
    private lateinit var btn_startfido: Button
    private lateinit var inputFromRP: String
    private lateinit var msgRequest: String
    private lateinit var mKeyguardManager: KeyguardManager


    private var listener: FidoClientListener = object :
        FidoClientListener {

        //region Body Error
        override fun onStatusError(response: String) {
            returnFailedAndFinish(response)
        }

        override fun onBodyError(response: String) {
            returnFailedAndFinish(response)
        }

        override fun onException(ex: Exception) {
            returnFailedAndFinish(ex.message as String)
        }

        override fun onFailure(t: Throwable) {
            returnFailedAndFinish(t.message as String)
        }

        override fun callTransFactsList(response: String) {
            setmsgRequest(response)
        }


        //endregion
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fidoclient)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        layoutfun = this.intent.extras!!.getInt("layoutfunction")
        mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        when (layoutfun) {
            15 -> fidoClientLabel.text = "Click Start to Register Fido"
            30 -> fidoClientLabel.text = "Click Start to Authen Fido"
            40 -> fidoClientLabel.text = "Click Start to De-Rester Fido"
        }

        btn_startfido = findViewById(R.id.btn_strfido)

        //OnbuttonClick
        btn_startfido.setOnClickListener {
            showStart()
            init()
        }
    }



    private fun init() {
        try {
            val reqObj = JSONObject(this.intent.extras!!.getString("message") as String)
            inputFromRP = reqObj.get("uafProtocolMessage") as String

            //Sign apk of Application
            signapk = getFacetID(
                this@FidoUafActivity.packageManager.getPackageInfo(
                    this@FidoUafActivity.packageName,
                    PackageManager.GET_SIGNATURES
                )
            )

            checkUpv()
            parseMsgAndValidation()

            checkPolicy()
            getTransFacetsList()

        } catch (e: Exception) {
            listener.onException(e)
        }
    }


    private fun getFacetID(paramPackageInfo: PackageInfo): String {
        try {

            val byteArrayInputStream =
                ByteArrayInputStream(paramPackageInfo.signatures[0].toByteArray())
            val certificate =
                CertificateFactory.getInstance("X509").generateCertificate(byteArrayInputStream)
            val messageDigest = MessageDigest.getInstance("SHA1")
            return "android:apk-key-hash:" + Base64.encodeToString(
                (messageDigest as MessageDigest).digest(
                    certificate.encoded
                ), 3
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun getTransFacetsList() {
        try {
            opUtils.getTrustedFacetsLists(inputFromRP, listener)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun setmsgRequest(response: String){
        try {
            msgRequest = opUtils.getRuleFacetsList(
                inputFromRP,
                signapk,
                response,
                this@FidoUafActivity,
                false
            )
            userVerifyScreen()

        }catch (e: Exception){
            listener.onException(e)
        }
    }

    private fun processOP() {
        try {
            if (msgRequest.isNotEmpty()) {
                if (msgRequest.contains("Reg")){

                    val msgRegResponse = regOp.register(msgRequest, signapk)
                    returnResultAndFinish(msgRegResponse)
                }
                if(msgRequest.contains("Auth")){

                    val msgAuthResponse = authOp.authen(msgRequest,signapk,authKeyID)
                    returnResultAndFinish(msgAuthResponse)
                }
                if(msgRequest.contains("Dereg")){

                    val msgAuthResponse = deregOp.dereg(deregKeyID)
                    returnResultAndFinish(msgAuthResponse)
                }
            } else {
                throw Exception("Request message from user agent is empty")
            }
        } catch (e: Exception) {
            listener.onException(e)
        }
    }

    private fun checkPolicy() {

        try {

            //Authenticator Info
            val getInfo =
                JSONObject(getInfoJson).getJSONArray("Authenticators")[0]
            val authInfo =
                gson.fromJson(getInfo.toString(), AuthenticatorInfo::class.java)

            if (inputFromRP.contains("Reg")) {

                //Check disallowed
                val disallowed =
                    (JSONArray(inputFromRP).get(0) as JSONObject).getJSONObject("policy")
                        .getJSONArray("disallowed")
                val matchDisallowed =
                    gson.fromJson(disallowed.toString(), Array<MatchCriteria>::class.java)

                //Check accepted
                val accepted =
                    (JSONArray(inputFromRP).get(0) as JSONObject).getJSONObject("policy")
                        .getJSONArray("accepted")
                val matchAccepted =
                    gson.fromJson(accepted.toString(), Array<Array<MatchCriteria>>::class.java)

                for (i in 0 until matchDisallowed.size) {

                    if (matchDisallowed[i].aaid!![0] == authInfo.aaid!![0]) {
                        if (matchDisallowed[i].keyIDs!!.isNotEmpty()) {
                            throw Exception("Username already registered by the authenticator")
                        }
                    }
                }

                for (i in 0 until matchAccepted.size) {

                    //Check User Verification
                    if (matchAccepted[i][0].userVerification != authInfo.userVerification &&
                        matchAccepted[i][0].authenticatorVersion != authInfo.authenticatorVersion &&
                        matchAccepted[i][0].assertionSchemes!![0] != authInfo.assertionScheme
                    ) {

                        if (i == matchAccepted.size - 1) {
                            throw Exception("Authenticator Not Math Server's Policy")
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "The Authenticator can be used",
                            Toast.LENGTH_LONG
                        ).show()

                        break
                    }
                }
            }
            if(inputFromRP.contains("Auth")){

                //Check disallowed
                val disallowed =
                    (JSONArray(inputFromRP).get(0) as JSONObject).getJSONObject("policy")
                        .getJSONArray("disallowed")
                val matchDisallowed =
                    gson.fromJson(disallowed.toString(), Array<MatchCriteria>::class.java)

                //Check accepted
                val accepted =
                    (JSONArray(inputFromRP).get(0) as JSONObject).getJSONObject("policy")
                        .getJSONArray("accepted")
                val matchAccepted =
                    gson.fromJson(accepted.toString(), Array<Array<MatchCriteria>>::class.java)

                for (i in 0 until matchDisallowed.size) {

                    if (matchDisallowed[i].aaid!![0] == authInfo.aaid!![0]) {
                        throw Exception("The authenticator not available")
                    }
                }
                for (i in 0 until matchAccepted.size) {

                    //Check User Verification
                    if (matchAccepted[i][0].aaid!![0] == authInfo.aaid!![0]) {
                        if(matchAccepted[i][0].keyIDs!!.isNotEmpty()){
                            authKeyID = matchAccepted[i][0].keyIDs!![0]
                            Toast.makeText(
                                applicationContext,
                                "The Authenticator can be used",
                                Toast.LENGTH_LONG
                            ).show()
                            break
                        }
                    } else{
                        if (i == matchAccepted.size - 1) {
                            throw Exception("The username has been registerated by the Athenticator")
                        }
                    }
                }
            }
            if(inputFromRP.contains("Dereg")){

                val deregRequest =
                    gson.fromJson(inputFromRP, Array<DeregistrationRequest>::class.java)[0]

                val authenticators = deregRequest.authenticators
                for (i in 0 until authenticators!!.size) {
                    if (authenticators[i].aaid == authInfo.aaid!![0]) {
                        if (authenticators[i].keyID!!.isNotEmpty()) {
                            deregKeyID = authenticators[i].keyID as String
                            break
                        } else {
                            throw Exception("Can not delete all KeyID")
                        }
                    }
                    if (i == authenticators.size - 1) {
                        throw Exception("Not match AAID")
                    }
                }
            }

        }catch(e: Exception){
            throw e
        }
    }

    override fun onBackPressed() {
            //Not do anything
    }

    private fun checkUpv() {
        try {
            val upv  = (JSONArray(inputFromRP).get(0) as JSONObject).getJSONObject("header").getString("upv")
            val version = gson.fromJson(upv, Version::class.java)

            if (version.major != 1 && version.minor != 1) {
                throw Exception("UPV is invalid| Error major: ${version.major} minor: ${version.minor}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun parseMsgAndValidation() {

        try {

            val uafProMsgArray = JSONArray(inputFromRP).toString()

            if (inputFromRP.contains("Reg")) {

                val parseRegMsg =
                    gson.fromJson(uafProMsgArray, Array<RegistrationRequest>::class.java)[0] //pase Data

                when {
                    parseRegMsg.header == null || parseRegMsg.header !is OperationHeader
                        -> throw Exception("UAF message header is not present")
                    parseRegMsg.header!!.upv == null || parseRegMsg.header!!.upv !is Version
                        -> throw Exception("UAF message upv is not present")
                    parseRegMsg.header!!.op == null || parseRegMsg.header!!.op !is Operation
                        -> throw Exception("UAF message op is not present")
                    parseRegMsg.challenge == null || parseRegMsg.challenge !is String
                        -> throw Exception("UAF message challenge is not present")
                    parseRegMsg.username == null || parseRegMsg.username !is String
                        -> throw Exception("UAF message username is not present")
                    parseRegMsg.policy == null || parseRegMsg.policy !is Policy
                        -> throw Exception("UAF message policy is not present")
                    parseRegMsg.policy!!.accepted == null || parseRegMsg.policy!!.accepted !is Array<Array<MatchCriteria>>
                        -> throw Exception("UAF message policy is not present")
                }
            }
            if (inputFromRP.contains("Auth")) {

                val parseAuthMsg =
                    gson.fromJson(uafProMsgArray, Array<AuthenticationRequest>::class.java)[0] //pase Data

                when {
                    parseAuthMsg.header == null || parseAuthMsg.header !is OperationHeader
                        -> throw Exception("UAF message header is not present")
                    parseAuthMsg.header!!.upv == null || parseAuthMsg.header!!.upv !is Version
                        -> throw Exception("UAF message upv is not present")
                    parseAuthMsg.header!!.op == null || parseAuthMsg.header!!.op !is Operation
                        -> throw Exception("UAF message op is not present")
                    parseAuthMsg.challenge == null || parseAuthMsg.challenge !is String
                        -> throw Exception("UAF message challenge is not present")
                    parseAuthMsg.policy == null || parseAuthMsg.policy !is Policy
                        -> throw Exception("UAF message policy is not present")
                    parseAuthMsg.policy!!.accepted == null || parseAuthMsg.policy!!.accepted !is Array<Array<MatchCriteria>>
                        -> throw Exception("UAF message policy is not present")
                }
            }
            if (inputFromRP.contains("Dereg")) {

                val parseDeregMsg =
                    gson.fromJson(uafProMsgArray, Array<DeregistrationRequest>::class.java)[0] //pase Data

                when {
                    parseDeregMsg.header == null || parseDeregMsg.header !is OperationHeader
                    -> throw Exception("UAF message header is not present")
                    parseDeregMsg.header!!.upv == null || parseDeregMsg.header!!.upv !is Version
                    -> throw Exception("UAF message upv is not present")
                    parseDeregMsg.header!!.op == null || parseDeregMsg.header!!.op !is Operation
                    -> throw Exception("UAF message op is not present")
                    parseDeregMsg.authenticators == null || parseDeregMsg.authenticators !is Array<DeregisterAuthenticator>
                    -> throw Exception("UAF message authenticators is not present")
                    parseDeregMsg.authenticators!![0].aaid == null || parseDeregMsg.authenticators!![0].aaid !is String
                    -> throw Exception("UAF message aaid is not present")
                    parseDeregMsg.authenticators!![0].keyID == null || parseDeregMsg.authenticators!![0].keyID !is String
                    -> throw Exception("UAF message keyID is not present")
                }
            }

        } catch (e: Exception) {
            throw e
        }
    }

    private fun userVerifyScreen() {

        try {

            Handler().postDelayed({
                // Create the Confirm Credentials screen. You can customize the title and description. Or
                // we will provide a generic one for you if you leave it null
                val intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null)
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "The Authenticator has not been authorized",
                        Toast.LENGTH_LONG
                    ).show()
                    showReset()
                }
            }, 2000)
        }catch(e:Exception){
            listener.onException(e)
        }
    }


     override fun onActivityResult(requestCode: Int, resultCode: Int, transfactlist: Intent?) {
         super.onActivityResult(requestCode, resultCode, transfactlist)
         try {
             if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
                 // Challenge completed, proceed with using cipher
                 if (resultCode == Activity.RESULT_OK) {
                     Toast.makeText(
                         applicationContext,
                         "User Verified",
                         Toast.LENGTH_SHORT
                     ).show()
                     processOP()
                 } else {
                     // The user canceled or didn’t complete the lock screen
                     // operation. Go to error/cancellation flow.
                     Toast.makeText(
                         applicationContext,
                         "User Verification failed",
                         Toast.LENGTH_LONG
                     ).show()
                     showReset()
                 }
             }
         }catch(e:Exception){
             listener.onException(e)
         }
    }

    private fun returnResultAndFinish(msg: String) {
        Handler().postDelayed({
            val data = Bundle()
            data.putString("message", msg)
            val intent = Intent()
            intent.putExtras(data)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }, 2000)
    }
    private fun returnFailedAndFinish(msgError: String) {
        Handler().postDelayed({
            val data = Bundle()
            data.putString("error", msgError)
            val intent = Intent()
            intent.putExtras(data)
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }, 2000)
    }

    private fun showStart() {
        loading.visibility = View.VISIBLE
        btn_startfido.isEnabled = false
    }

    private fun showReset() {
        loading.visibility = View.GONE
        btn_startfido.isEnabled = true
    }
}


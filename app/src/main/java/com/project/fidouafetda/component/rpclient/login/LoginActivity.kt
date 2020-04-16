package com.project.fidouafetda.component.rpclient.login

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.google.gson.GsonBuilder


import com.project.fidouafetda.R
import com.project.fidouafetda.callback.RPClientListener
import com.project.fidouafetda.component.rpclient.op.Auth
import com.project.fidouafetda.component.rpclient.op.Dereg
import com.project.fidouafetda.component.rpclient.op.Reg
import com.project.fidouafetda.model.*
import kotlinx.android.synthetic.main.activity_deregister.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_purchase.*
import org.json.JSONObject



@Suppress("DEPRECATION", "SENSELESS_COMPARISON")
class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private val reg = Reg()
    private  val auth = Auth()
    private val deregister = Dereg()

    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private lateinit var mKeyguardManager: KeyguardManager
    private lateinit var username: EditText
    private lateinit var password: EditText
    private var doubleBackToExitPressedOnce = false
    private lateinit var login: Button
    private lateinit var purchase: Button
    private lateinit var dereg: Button

    private val REG_ACTIVITY_RES = 15
    private val AUTH_ACTIVITY_RES = 30
    private val DEREG_ACTIVITY_RES = 40

    private var listener: RPClientListener = object :
        RPClientListener {

        //region Body Error
        override fun onStatusError(response: String) {
            Toast.makeText(applicationContext, response, Toast.LENGTH_LONG).show()
            showFailed()
        }

        override fun onBodyError(response: String) {
            Toast.makeText(applicationContext, response, Toast.LENGTH_LONG).show()
            showFailed()
        }

        override fun onException(ex: Exception) {
            Toast.makeText(applicationContext, "${ex.message}", Toast.LENGTH_LONG).show()
            showFailed()
        }

        override fun onFailure(t: Throwable) {
            //fail any course
            Toast.makeText(applicationContext, "$t", Toast.LENGTH_LONG).show()
            showFailed()
        }

        override fun callBackRegRequest(response: List<RegistrationRequest>) {
            regfidoClientIntent(response)
            //showReset()
        }

        override fun callBackRegResponse(response: List<LoginResponse>) {
            regResponse(response)
        }

        override fun callBackAuthRequest(response: List<AuthenticationRequest>) {
            authfidoClientIntent(response)

        }

        override fun callBackAuthResponse(response: List<PurchaseResponse>) {
            authResponse(response)
        }

        override fun callBackDeregRequest(response: List<DeregistrationRequest>) {
            deregfidoClientIntent(response)
        }
        //endregion
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

        //Reg
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)

        //Auth
        purchase = findViewById(R.id.purchase)

        //Dereg
        dereg = findViewById(R.id.dereg)

        //region Check Permission
        mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!supportsFingerprintAuth()) {
            // Show a message that the user hasn't set up a lock screen.
            Toast.makeText(
                this,
                "Please set FingerPrint for Lock screen",
                Toast.LENGTH_LONG
            ).show()
            username.isEnabled = false
            password.isEnabled = false
            purchase.isEnabled = false
        }

        //region login check
        loginViewModel =
            ViewModelProviders.of(this, LoginViewModelFactory()).get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                setOnClickRegFido()
            }

            purchase.setOnClickListener {
                setContentView(R.layout.activity_purchase)
                setOnClickAuthFido()
            }

            dereg.setOnClickListener {
                setContentView(R.layout.activity_deregister)
                setOnClickDeregFido()
            }
        }
        //endregion
    }

    private fun regRequest(username: String, password: String) {
        try {
            reg.getUafMsgRegRequest(username, password, listener)
        } catch (e: PackageManager.NameNotFoundException) {
            listener.onException(e)
        }
    }
    private fun authRequest(authusername: String){

        try {
            auth.getUafMsgAuthRequest(authusername,listener)
        } catch (e: PackageManager.NameNotFoundException) {
            listener.onException(e)
        }
    }

    private fun deregRequest(deregusername: String){
        try {
            deregister.getUafMsgDereqRequest(deregusername,listener)
        } catch (e: PackageManager.NameNotFoundException) {
            listener.onException(e)
        }
    }

    private fun regResponse(response: List<LoginResponse>) {
        try {
            if(response[0].status == "SUCCESS"){
                showSuccess()
            }else{
                showFailed()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            listener.onException(e)
        }
    }
    private fun authResponse(response: List<PurchaseResponse>) {
        try {
            if(response[0].status == "SUCCESS"){
                showSuccess()
            }else{
                showFailed()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            listener.onException(e)
        }
    }

    private fun deregResponse(response: String) {
        try {
            if(response == "SUCCESS"){
                showSuccess()
            }else{
                showFailed()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            listener.onException(e)
        }
    }

    private fun regfidoClientIntent(response: List<RegistrationRequest>) {
        try {
            val resUafReq = JSONObject().put("uafProtocolMessage", gson.toJson(response)).toString()

            val channelBinding = ChannelBinding()
            channelBinding.cid_pubkey = ""
            channelBinding.serverEndPoint = ""
            channelBinding.tlsUnique = ""
            channelBinding.tlsServerCertificate = ""

            val i = Intent("org.fidoalliance.intent.FIDO_OPERATION")
            i.addCategory("android.intent.category.DEFAULT")
            i.type = "application/fido.uaf_client+json"

            val data = Bundle()
            data.putString("message", resUafReq)
            data.putString("UAFIntentType", UAFIntentType.UAF_OPERATION.name)
            data.putString("channelBindings", gson.toJson(channelBinding))
            data.putInt("layoutfunction", REG_ACTIVITY_RES)
            i.putExtras(data)

            startActivityForResult(i, REG_ACTIVITY_RES)

            loading.visibility = View.GONE
            username.setText("")
            password.setText("")

            //showReset()

        } catch (e: Exception) {
            listener.onException(e)
        }

    }

    private fun authfidoClientIntent(response: List<AuthenticationRequest>){

        try {
            val authUafReq = JSONObject().put("uafProtocolMessage", gson.toJson(response)).toString()

            val channelBinding = ChannelBinding()
            channelBinding.cid_pubkey = ""
            channelBinding.serverEndPoint = ""
            channelBinding.tlsUnique = ""
            channelBinding.tlsServerCertificate = ""

            val i = Intent("org.fidoalliance.intent.FIDO_OPERATION")
            i.addCategory("android.intent.category.DEFAULT")
            i.type = "application/fido.uaf_client+json"

            val data = Bundle()
            data.putString("message", authUafReq)
            data.putString("UAFIntentType", UAFIntentType.UAF_OPERATION.name)
            data.putString("channelBindings", gson.toJson(channelBinding))
            data.putInt("layoutfunction", AUTH_ACTIVITY_RES)
            i.putExtras(data)

            startActivityForResult(i, AUTH_ACTIVITY_RES)

            authloading.visibility = View.GONE
            authusername.setText("")
            //showReset()

        } catch (e: Exception) {
            listener.onException(e)
        }
    }

    private fun deregfidoClientIntent(response: List<DeregistrationRequest>){

        try {
            val deregUafReq = JSONObject().put("uafProtocolMessage", gson.toJson(response)).toString()

            val channelBinding = ChannelBinding()
            channelBinding.cid_pubkey = ""
            channelBinding.serverEndPoint = ""
            channelBinding.tlsUnique = ""
            channelBinding.tlsServerCertificate = ""

            val i = Intent("org.fidoalliance.intent.FIDO_OPERATION")
            i.addCategory("android.intent.category.DEFAULT")
            i.type = "application/fido.uaf_client+json"

            val data = Bundle()
            data.putString("message", deregUafReq)
            data.putString("UAFIntentType", UAFIntentType.UAF_OPERATION.name)
            data.putString("channelBindings", gson.toJson(channelBinding))
            data.putInt("layoutfunction", DEREG_ACTIVITY_RES)
            i.putExtras(data)

            startActivityForResult(i, DEREG_ACTIVITY_RES)

            deregloading.visibility = View.GONE
            deregusername.setText("")
            //showReset()

        } catch (e: Exception) {
            listener.onException(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REG_ACTIVITY_RES) {
            if (resultCode == Activity.RESULT_OK) {

                showPlswait()

                val message = data!!.getStringExtra("message") as String
                val msgResponse = gson.fromJson(message, RegistrationResponse::class.java)
                val responseArray: ArrayList<RegistrationResponse> = ArrayList()
                responseArray.add(msgResponse)
                reg.getUafMsgRegResponse(responseArray, listener)
            }
            if (resultCode == Activity.RESULT_CANCELED) {

                val error = data!!.getStringExtra("error") as String

                if (error.isNotEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                    showFailed()
                }
            }
        }
        if(requestCode == AUTH_ACTIVITY_RES){

            if (resultCode == Activity.RESULT_OK) {

                showPlswait()

                val message = data!!.getStringExtra("message") as String
                val msgResponse = gson.fromJson(message, AuthenticationResponse::class.java)
                val responseArray: ArrayList<AuthenticationResponse> = ArrayList()
                responseArray.add(msgResponse)
                auth.getUafMsgAuthResponse(responseArray, listener)
            }
            if (resultCode == Activity.RESULT_CANCELED) {

                val error = data!!.getStringExtra("error") as String

                if (error.isNotEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                    showFailed()
                }
            }
        }
        if(requestCode == DEREG_ACTIVITY_RES){

            if (resultCode == Activity.RESULT_OK) {

                showPlswait()
                val message = data!!.getStringExtra("message") as String
                deregResponse(message)
            }
            if (resultCode == Activity.RESULT_CANCELED) {

                val error = data!!.getStringExtra("error") as String

                if (error.isNotEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                    showFailed()
                }
            }
        }
    }

    private fun setOnClickRegFido(){

        loading.visibility = View.VISIBLE
        login.isEnabled = false
        regRequest(username.text.toString(), password.text.toString())
    }

    private fun setOnClickAuthFido(){

        signin.setOnClickListener {
            if(authusername.text.isNullOrEmpty()){
                Toast.makeText(
                    applicationContext,
                    "Please add Username",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                authloading.visibility = View.VISIBLE
                authRequest(authusername.text.toString())
            }
        }
    }

    private fun setOnClickDeregFido(){

        deletekey.setOnClickListener {
            if(deregusername.text.isNullOrEmpty()){
                Toast.makeText(
                    applicationContext,
                    "Please add Username",
                    Toast.LENGTH_LONG
                ).show()
            }else{

                if (doubleBackToExitPressedOnce) {
                    deregloading.visibility = View.VISIBLE
                    deregRequest(deregusername.text.toString())
                }

                this.doubleBackToExitPressedOnce = true
                Toast.makeText(this, "Please double click to delete User's key", Toast.LENGTH_SHORT).show()
                Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 1000)
            }
        }

       /* deleteall.setOnClickListener {

            if (doubleBackToExitPressedOnce) {
                deleteall.isEnabled = false
                deregloading.visibility = View.VISIBLE
                //deregAllRequest()
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please double click to delete all key", Toast.LENGTH_SHORT).show()

            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 1000)

        }*/
    }

    private fun showPlswait() {
        setContentView(R.layout.activity_progress)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun showSuccess() {
        setContentView(R.layout.activity_success)
    }

    private fun showFailed() {
        setContentView(R.layout.activity_error)
    }

    private fun supportsFingerprintAuth(): Boolean {
        val fingerprintManager = getSystemService(FingerprintManager::class.java)
        return fingerprintManager!!.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()
    }

    override fun onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)

    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })

}

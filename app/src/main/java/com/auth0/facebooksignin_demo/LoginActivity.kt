package com.auth0.facebooksignin_demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        callbackManager = CallbackManager.Factory.create()

        with(login_button) {
            setPermissions("email")
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.accessToken?.token?.let {
                        Log.e("Login", "Access Token: $it")
                        obtainFbSessionToken(it) {
                            //TODO Use session token
                        }
                    }
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun obtainFbSessionToken(fbAccessToken: String, callback: (String?) -> Unit) {
        val params = Bundle()
        params.putString("grant_type", "fb_attenuate_token")
        params.putString("fb_exchange_token", fbAccessToken)
        params.putString("client_id", getString(R.string.facebook_app_id))

        val request = GraphRequest()
        request.parameters = params
        request.graphPath = "oauth/access_token"
        request.callback = GraphRequest.Callback { response ->
            if (response.error != null) {
                // TODO Handle errors
                callback.invoke(null)
                return@Callback
            }
            val fbSessionToken = response.jsonObject.getString("access_token")
            Log.e("Login", "Session token: $fbSessionToken")
            callback.invoke(fbSessionToken)
        }
        request.executeAsync()
    }
}
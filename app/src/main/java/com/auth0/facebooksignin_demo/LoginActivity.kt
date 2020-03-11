package com.auth0.facebooksignin_demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    companion object {
        private val TAG = LoginActivity::class.simpleName
        private const val FACEBOOK_PERMISSIONS = "email"
    }

    private lateinit var fbCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LoginManager.getInstance().logOut()
        setContentView(R.layout.activity_login)
        fbCallbackManager = CallbackManager.Factory.create()

        with(login_button) {
            setPermissions(FACEBOOK_PERMISSIONS)
            registerCallback(fbCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    result.accessToken.let { accessToken ->
                        fetchSessionToken(accessToken.token) { sessionToken ->
                            sessionToken?.let {
                                fetchUserProfile(accessToken.token, accessToken.userId) { profile ->
                                }
                            }
                        }
                    }
                }

                override fun onCancel() {
                    Log.i(TAG, "Facebook sign-in cancelled")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Error ${error.message}")
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fbCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun fetchSessionToken(token: String, callback: (String?) -> Unit) {
        val params = Bundle()
        params.putString("grant_type", "fb_attenuate_token")
        params.putString("fb_exchange_token", token)
        params.putString("client_id", getString(R.string.facebook_app_id))

        val request = GraphRequest()
        request.parameters = params
        request.graphPath = "oauth/access_token"
        request.callback = GraphRequest.Callback { response ->
            if (response.error != null) {
                Log.e(TAG, "Failed to fetch session token. ${response.error.errorMessage}")
                callback.invoke(null)
                return@Callback
            }
            val fbSessionToken = response.jsonObject.getString("access_token")
            callback.invoke(fbSessionToken)
        }
        request.executeAsync()
    }

    private fun fetchUserProfile(token: String, userId: String, callback: (String?) -> Unit) {
        val params = Bundle()
        params.putString("access_token", token)
        params.putString("fields", "first_name,last_name,email")

        val request = GraphRequest()
        request.parameters = params
        request.graphPath = userId
        request.callback = GraphRequest.Callback { response ->
            if (response.error != null) {
                Log.w(TAG, "Failed to fetch user profile: ${response.error.errorMessage}")
                callback.invoke(null)
                return@Callback
            }
            callback.invoke(response.rawResponse)
        }
        request.executeAsync()
    }

}
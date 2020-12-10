package com.example.museumapplication.utils.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.museumapplication.R
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.LoginActivity
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.auth.AuthUtils.enableAllItems
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.FacebookAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import org.json.JSONException
import org.json.JSONObject

class FacebookAuth(var context: Context) : IBaseAuth {
    var auth: AGConnectAuth = AGConnectAuth.getInstance()
    var loginButton: LoginButton = (context as LoginActivity).findViewById(R.id.facebookButton)
    var mCallbackManager: CallbackManager = CallbackManager.Factory.create()
    override fun login() {
        LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("FacebookLogin:", ":onSuccess:$loginResult")
                try {
                    authWithFacebook(loginResult.accessToken)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancel() {
                Log.d("FacebookLogin:", ":onCancel")
                enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
            }

            override fun onError(error: FacebookException) {
                Log.d("FacebookLogin:", ":onError", error)
                enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    fun graphRequest(accessToken: AccessToken?, UserID: String?) {
        val request = GraphRequest.newMeRequest(
                accessToken
        ) { `object`: JSONObject, response: GraphResponse? ->
            try {
                if (instance.checkFirstTimeUser(`object`.getString("email"))) {
                    val user = User(UserID, `object`.getString("id"), `object`.getString("email"), `object`.getString("name"), "https://graph.facebook.com/" + `object`.getString("id") + "/picture?type=large")
                    instance.upsertUser(user)
                    instance.upsertAccountLinkInfo(LinkedAccount(UserID, user.uid))
                    UserLoggedIn.instance.setUser(user)
                    val home = Intent(context, HomeActivity::class.java)
                    context.startActivity(home)
                } else {
                    try {
                        val user = instance.queryByEmail(`object`.getString("email"))
                        instance.upsertAccountLinkInfo(LinkedAccount(`object`.getString("id"), user!!.uid))
                        UserLoggedIn.instance.setUser(user)
                        val home = Intent(context, HomeActivity::class.java)
                        context.startActivity(home)
                    } catch (e: AGConnectCloudDBException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,link,gender,birthday,email")
        request.parameters = parameters
        request.executeAsync()
    }

    fun authWithFacebook(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.credentialWithToken(accessToken.token)
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener { signInResult: SignInResult ->
                    // onSuccess
                    val user = signInResult.user
                    graphRequest(accessToken, user.uid)
                    enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
                }
                .addOnFailureListener { e: Exception ->
                    // onFail
                    enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
    }

    fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    init {
        loginButton.setPermissions("email", "public_profile")
    }
}
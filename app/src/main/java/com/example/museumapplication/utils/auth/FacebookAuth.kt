/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.museumapplication.utils.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.museumapplication.R
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.SharedAuthViewModel
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.FacebookAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import org.json.JSONException
import org.json.JSONObject

class FacebookAuth(val viewModel: SharedAuthViewModel) : IBaseAuth {
    companion object{
        const val RC_SIGN_IN = 64206
        const val TAG = "Facebook Login"
    }

    var auth: AGConnectAuth = AGConnectAuth.getInstance()

    var mCallbackManager: CallbackManager = CallbackManager.Factory.create()

    /**
     * Login task for facebook
     * Login Manager with a callback is used to login task
     */
    override fun login() {
        LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, ":onSuccess:$loginResult")
                try {
                    authWithFacebook(loginResult.accessToken)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                viewModel.progressBarVisibility.postValue(View.GONE)
            }

            override fun onCancel() {
                Log.d(TAG, ":onCancel")
                viewModel.itemClickableOrEnabled.postValue(true)
                viewModel.progressBarVisibility.postValue(View.GONE)
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, ":onError", error)
                viewModel.itemClickableOrEnabled.postValue(true)
                Toast.makeText(viewModel.mContext, viewModel.mContext!!.getString(R.string.login_error), Toast.LENGTH_LONG).show()
                viewModel.progressBarVisibility.postValue(View.GONE)
            }
        })
    }

    /**
     * graphRequest function is used to retrieve user information from the Facebook
     * facebook credential does not provide user information without graph request
     */
    private fun graphRequest(accessToken: AccessToken?, userID: String?) {
        val request = GraphRequest.newMeRequest(
                accessToken
        ) { `object`: JSONObject, _: GraphResponse? ->
            try {
                if (instance.checkFirstTimeUser(`object`.getString("email"))) {
                    val user = User(userID, `object`.getString("id"), `object`.getString("email"), `object`.getString("name"), "https://graph.facebook.com/" + `object`.getString("id") + "/picture?type=large")
                    instance.upsertUser(user)
                    instance.upsertAccountLinkInfo(LinkedAccount(userID, user.uid))
                    UserLoggedIn.instance.setUser(user)
                    viewModel.navigateToHomePage.postValue(true)
                } else {
                    try {
                        val user = instance.getUserByEmail(`object`.getString("email"))
                        instance.upsertAccountLinkInfo(LinkedAccount(`object`.getString("id"), user!!.uid))
                        UserLoggedIn.instance.setUser(user)
                        viewModel.navigateToHomePage.postValue(true)
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

    /**
     * Facebook- Huawei Auth Service integration
     */
    fun authWithFacebook(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.credentialWithToken(accessToken.token)
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener { signInResult: SignInResult ->
                    // onSuccess
                    val user = signInResult.user
                    graphRequest(accessToken, user.uid)
                    viewModel.itemClickableOrEnabled.postValue(true)
                }
                .addOnFailureListener { e: Exception ->
                    // onFail
                    viewModel.itemClickableOrEnabled.postValue(true)
                    Toast.makeText(viewModel.mContext, "AGC Auth Error", Toast.LENGTH_LONG).show()
                }
    }

    fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
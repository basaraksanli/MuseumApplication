package com.example.museumapplication.utils.auth

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.museumapplication.R
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.SharedAuthViewModel
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.GoogleAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import java.util.*

class GoogleAuth(var viewModel: SharedAuthViewModel) : IBaseAuth {

    var auth: AGConnectAuth = AGConnectAuth.getInstance()
    private var client: GoogleApiClient
    companion object {
        const val RC_SIGN_IN = 9001
    }

    /**
     * Google Sign in initialization
     */
    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(viewModel.mContext!!.getString(R.string.google_client_id))
                .requestEmail()
                .requestProfile()
                .build()

        client = GoogleApiClient.Builder(viewModel.mContext!!)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        client.connect()
    }

    /**
     * login task. Provides the google sign in intent
     */
    override fun login() {
        viewModel.RC_SIGN_IN= RC_SIGN_IN
        viewModel.signInIntent.value = Auth.GoogleSignInApi.getSignInIntent(client)
    }

    /**
     * activity result of the Google Sign in is processed in this function
     */
    fun activityResult(data: Intent?) {
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        Log.d("Login Google", "handleSignInResult:" + result!!.status)
        if (result.isSuccess) {
            viewModel.itemClickableOrEnabled.postValue(true)
            val account = result.signInAccount
            account?.let { authWithGoogle(it) }
            viewModel.progressBarVisibility.postValue(View.GONE)
        } else {
            viewModel.itemClickableOrEnabled.postValue(true)
            client.disconnect()
            viewModel.progressBarVisibility.postValue(View.GONE)
        }
    }

    /**
     * Google Sign in - Huawei Auth Service Integration
     */
    private fun authWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.credentialWithToken(account.idToken)
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener { signInResult: SignInResult ->
                    // onSuccess
                    val agcUser = signInResult.user
                    if (instance.checkFirstTimeUser(account.email)) {
                        val user = User(agcUser.uid, account.id, account.email, account.givenName + " " + account.familyName, Objects.requireNonNull(account.photoUrl).toString())
                        instance.upsertUser(user)
                        instance.upsertAccountLinkInfo(LinkedAccount(user.uid, user.uid))
                        UserLoggedIn.instance.setUser(user)
                    } else {
                        try {
                            val user = instance.getUserByEmail(account.email)
                            instance.upsertAccountLinkInfo(LinkedAccount(agcUser.uid, user!!.uid))
                            UserLoggedIn.instance.setUser(user)
                        } catch (e: AGConnectCloudDBException) {
                            e.printStackTrace()
                        }
                    }
                    viewModel.navigateToHomePage.postValue(true)
                    viewModel.itemClickableOrEnabled.postValue(true)
                }
                .addOnFailureListener { e: Exception ->
                    // onFail
                    viewModel.itemClickableOrEnabled.postValue(true)
                    Toast.makeText(viewModel.mContext,"Insert user info failed", Toast.LENGTH_LONG).show()
                }
    }


}
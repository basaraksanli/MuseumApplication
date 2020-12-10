package com.example.museumapplication.utils.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.museumapplication.R
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.LoginActivity
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.auth.AuthUtils.enableAllItems
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.GoogleAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import java.util.*

class GoogleAuth(var context: Context) : IBaseAuth, GoogleApiClient.OnConnectionFailedListener {
    var auth: AGConnectAuth = AGConnectAuth.getInstance()
    private var client: GoogleApiClient
    override fun login() {
        val signIntent = Auth.GoogleSignInApi.getSignInIntent(client)
        (context as LoginActivity).startActivityForResult(signIntent, RC_SIGN_IN)
    }

    fun activityResult(data: Intent?, activity: Activity?) {
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        Log.d("Login Google", "handleSignInResult:" + result!!.status)
        if (result.isSuccess) {
            enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
            val account = result.signInAccount
            account?.let { authWithGoogle(it) }
        } else {
            enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
            client.stopAutoManage((activity as FragmentActivity?)!!)
            client.disconnect()
        }
    }

    fun authWithGoogle(account: GoogleSignInAccount) {
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
                            val user = instance.queryByEmail(account.email)
                            instance.upsertAccountLinkInfo(LinkedAccount(agcUser.uid, user!!.uid))
                            UserLoggedIn.instance.setUser(user)
                        } catch (e: AGConnectCloudDBException) {
                            e.printStackTrace()
                        }
                    }
                    val home = Intent(context, HomeActivity::class.java)
                    context.startActivity(home)
                    enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
                }
                .addOnFailureListener { e: Exception ->
                    // onFail
                    enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.google_client_id))
                .requestEmail()
                .requestProfile()
                .build()
        client = GoogleApiClient.Builder(context.applicationContext)
                .enableAutoManage((context as LoginActivity), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }
}
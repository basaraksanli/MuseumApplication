package com.example.museumapplication.utils.authProviders

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.museumapplication.R
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.LoginActivity
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.AuthUtils.enableAllItems
import com.example.museumapplication.utils.services.CloudDBHelper.Companion.instance
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService

class HuaweiAuth(var context: Context) : IBaseAuth {
    var auth: AGConnectAuth = AGConnectAuth.getInstance()
    var service: HuaweiIdAuthService
    override fun login() {
        val signIntent = service.signInIntent
        (context as LoginActivity).startActivityForResult(signIntent, RC_SIGN_IN)
    }

    fun activityResult(data: Intent?) {
        val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
        if (authHuaweiIdTask.isSuccessful) {
            val huaweiAccount = authHuaweiIdTask.result
            Log.i("Huawei Login:", "accessToken:" + huaweiAccount.accessToken)
            authWithHuawei(huaweiAccount)
        } else {
            Log.e("Huawei ID Fail", "sign in failed : " + (authHuaweiIdTask.exception as ApiException).statusCode)
            enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
        }
    }

    private fun authWithHuawei(huaweiAccount: AuthHuaweiId) {
        val credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken)
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener { signInResult: SignInResult ->
            // onSuccess
            val agcUser = signInResult.user
            if (instance.checkFirstTimeUser(huaweiAccount.email)) {
                val user = User(agcUser.uid, huaweiAccount.uid, huaweiAccount.email, huaweiAccount.givenName + " " + huaweiAccount.familyName, huaweiAccount.avatarUriString)
                instance.upsertUser(user)
                instance.upsertAccountLinkInfo(LinkedAccount(user.uid, user.uid))
                UserLoggedIn.instance.setUser(user)
            } else {
                try {
                    val user = instance.queryByEmail(huaweiAccount.email)
                    instance.upsertAccountLinkInfo(LinkedAccount(agcUser.uid, user!!.uid))
                    UserLoggedIn.instance.setUser(user)
                } catch (e: AGConnectCloudDBException) {
                    e.printStackTrace()
                }
            }
            val home = Intent(context, HomeActivity::class.java)
            context.startActivity(home)
            enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
        }.addOnFailureListener { e ->
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            Log.d("HuaweiID Fail:", e.message!!)
            enableAllItems((context as LoginActivity).findViewById(R.id.linearLayout))
        }
    }

    companion object {
        private const val RC_SIGN_IN = 8888
    }

    init {
        val huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        /*val scopeList: MutableList<Scope> = ArrayList()
        scopeList.add(Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE))
        scopeList.add(Scope(HwIDConstant.SCOPE.SCOPE_ACCOUNT_EMAIL))
        huaweiIdAuthParamsHelper.setScopeList(scopeList)*/
        val authParams = huaweiIdAuthParamsHelper.setEmail().setAccessToken().setIdToken().createParams()
        service = HuaweiIdAuthManager.getService(context as LoginActivity, authParams)
    }
}
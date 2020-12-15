package com.example.museumapplication.utils.auth

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.LoginFragment
import com.example.museumapplication.ui.auth.SharedAuthViewModel
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
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

class HuaweiAuth(val viewModel: SharedAuthViewModel) : IBaseAuth {
    var auth: AGConnectAuth = AGConnectAuth.getInstance()
    var service: HuaweiIdAuthService

    companion object {
        const val RC_SIGN_IN = 8888
    }

    init {
        val huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        val authParams = huaweiIdAuthParamsHelper.setEmail().setAccessToken().setIdToken().createParams()
        service = HuaweiIdAuthManager.getService(viewModel.mContext, authParams)
    }

    override fun login() {
        viewModel.RC_SIGN_IN=  RC_SIGN_IN
        viewModel.signInIntent.value = service.signInIntent
    }

    fun activityResult(data: Intent?) {
        val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
        if (authHuaweiIdTask.isSuccessful) {
            val huaweiAccount = authHuaweiIdTask.result
            Log.i("Huawei Login:", "accessToken:" + huaweiAccount.accessToken)
            authWithHuawei(huaweiAccount)
        } else {
            Log.e("Huawei ID Fail", "sign in failed : " + (authHuaweiIdTask.exception as ApiException).statusCode)
            viewModel.itemClickableOrEnabled.postValue(true)
        }
    }

    private fun authWithHuawei(huaweiAccount: AuthHuaweiId) {
        val credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.accessToken)
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener { signInResult: SignInResult ->
            // onSuccess
            val agcUser = signInResult.user
            if (instance.checkFirstTimeUser(huaweiAccount.email)) {
                val user = User(agcUser.uid, huaweiAccount.uid, huaweiAccount.email,
                        huaweiAccount.givenName + " " +
                                huaweiAccount.familyName, huaweiAccount.avatarUriString)
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
            viewModel.navigateToHomePage.postValue(true)
            viewModel.itemClickableOrEnabled.postValue(true)
        }.addOnFailureListener { e ->
            Toast.makeText(viewModel.mContext, e.message, Toast.LENGTH_LONG).show()
            Log.d("HuaweiID Fail:", e.message!!)
            viewModel.itemClickableOrEnabled.postValue(true)
        }
    }
}
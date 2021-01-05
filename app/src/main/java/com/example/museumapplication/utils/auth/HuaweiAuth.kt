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
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.museumapplication.R
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.SharedAuthViewModel
import com.example.museumapplication.utils.GeneralUtils
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

    /**
     * Huawei ID- Account kit initialization
     */
    init {
        val huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        val authParams = huaweiIdAuthParamsHelper.setEmail().setAccessToken().setIdToken().createParams()
        service = HuaweiIdAuthManager.getService(viewModel.mContext, authParams)
    }

    /**
     * Huawei intent is requested here
     */
    override fun login() {
        viewModel.RC_SIGN_IN=  RC_SIGN_IN
        viewModel.signInIntent.value = service.signInIntent
    }

    /**
     * Huawei activity result is processed here
     */
    fun activityResult(data: Intent?) {
        val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
        if (authHuaweiIdTask.isSuccessful) {
            val huaweiAccount = authHuaweiIdTask.result
            Log.i("Huawei Login:", "accessToken:" + huaweiAccount.accessToken)
            if(huaweiAccount!!.email !=null)
                authWithHuawei(huaweiAccount)
            else
                GeneralUtils.showWarnDialog(viewModel.mContext!!.getString(R.string.email_permissions_warning), viewModel.mContext, null)
            viewModel.progressBarVisibility.postValue(View.GONE)
        } else {
            Log.e("Huawei ID Fail", "sign in failed : " + (authHuaweiIdTask.exception as ApiException).statusCode)
            viewModel.itemClickableOrEnabled.postValue(true)
            viewModel.progressBarVisibility.postValue(View.GONE)
        }
    }

    /**
     * Account Kit - Auth Service integration
     */
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
                    val user = instance.getUserByEmail(huaweiAccount.email)
                    instance.upsertAccountLinkInfo(LinkedAccount(agcUser.uid, user!!.uid))
                    UserLoggedIn.instance.setUser(user)
                } catch (e: AGConnectCloudDBException) {
                    e.printStackTrace()
                }
            }
            viewModel.navigateToHomePage.postValue(true)
            viewModel.itemClickableOrEnabled.postValue(true)
        }.addOnFailureListener { e ->
            Toast.makeText(viewModel.mContext, "AGC Auth failed", Toast.LENGTH_LONG).show()
            Log.d("HuaweiID Fail:", e.message!!)
            viewModel.itemClickableOrEnabled.postValue(true)
        }
    }
}
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
 */package com.example.museumapplication.ui.splash_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.museumapplication.R
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.AuthActivity
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import kotlin.system.exitProcess


class SplashActivity : AppCompatActivity() {
    var agcuser: AGConnectUser? = null
    var agConnectAuth: AGConnectAuth? = null
    var countDownTimer: CountDownTimer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        //initialize Cloud DB
        instance.initAGConnectCloudDB(this)


        /**
         * Cloud db needs at least 1 second delay for running queries
         * Also if cloud db can not be initialized, it tries over an over. If it can not succeed in 10 minutes, an alert dialog is shown to the user.
         * In this case application won't start. User can retry to connect
         */
        agConnectAuth = AGConnectAuth.getInstance()
        agcuser = agConnectAuth!!.currentUser
        if (agConnectAuth!!.currentUser != null) {
            val loginTask = LoginTask().execute(this)
            countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // Empty On Tick function
                }
                override fun onFinish() {
                    loginTask.cancel(true)
                    val builder = AlertDialog.Builder(this@SplashActivity)
                    builder.setTitle(resources.getIdentifier("app_name", "string", packageName))
                    builder.setMessage(getString(R.string.could_not_connect))
                    builder.setNegativeButton("EXIT") { _: DialogInterface?, _: Int ->
                        finish()
                        exitProcess(0)
                    }
                    builder.setPositiveButton(getString(R.string.retry_string)) { _: DialogInterface?, _: Int ->
                        val intent = intent
                        finish()
                        startActivity(intent)
                    }
                    builder.show()
                }
            }.start()
        } else {
            Handler().postDelayed({
                val loginActivity = Intent(this@SplashActivity, AuthActivity::class.java)
                startActivity(loginActivity)
            }, 500)
        }
    }


    @SuppressLint("StaticFieldLeak")
    open inner class LoginTask : AsyncTask<Activity?, Void?, Void?>() {

        override fun doInBackground(vararg params: Activity?): Void? {
            var accountID: String?
            try {
                var user: User?
                do {
                    accountID = instance.getMainAccountIDofLinkedAccount(agcuser!!.uid)
                    user = instance.getUserByID(accountID)
                    if (user != null) UserLoggedIn.instance.setUser(user)
                } while (user == null)
            } catch (e: AGConnectCloudDBException) {
                e.printStackTrace()
            }
            countDownTimer!!.cancel()
            val homeActivity = Intent(params[0], HomeActivity::class.java)
            if (intent.extras != null) {
                val extra = Bundle()
                extra.putString("MuseumName", intent.getStringExtra("MuseumName"))
                homeActivity.putExtras(extra)
            }
            startActivity(homeActivity)
            return null
        }
    }
}
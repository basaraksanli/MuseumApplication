package com.example.museumapplication.ui.splash_screen

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

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        UserLoggedIn.instance.retrieveFavoriteMuseumList(this)
        UserLoggedIn.instance.retrieveFavoriteArtifactList(this)

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
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    loginTask.cancel(true)
                    val builder = AlertDialog.Builder(this@SplashActivity)
                    builder.setTitle(resources.getIdentifier("app_name", "string", packageName))
                    builder.setMessage("Could not connect to internet services. Check your network")
                    builder.setNegativeButton("EXIT") { _: DialogInterface?, _: Int ->
                        finish()
                        exitProcess(0)
                    }
                    builder.setPositiveButton("RETRY") { _: DialogInterface?, _: Int ->
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
                    accountID = instance.getPrimaryAccountID_LinkedAccount(agcuser!!.uid)
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
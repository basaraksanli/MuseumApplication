package com.example.museumapplication.ui.splash_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.museumapplication.R
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.LoginActivity
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.SettingsUtils
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.system.exitProcess


class SplashActivity : AppCompatActivity() {
    var agcuser: AGConnectUser? = null
    var agConnectAuth: AGConnectAuth? = null
    var countDownTimer: CountDownTimer? = null

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        SettingsUtils.loadSettings(this)
        UserLoggedIn.instance.retrieveFavoriteMuseumList(this)
        UserLoggedIn.instance.retrieveFavoriteArtifactList(this)


        try {
            @SuppressLint("PackageManagerGetSignatures") val info = packageManager.getPackageInfo(
                    "com.example.museumapplication",
                    PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }


        instance.initAGConnectCloudDB(this)
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
                val loginActivity = Intent(this@SplashActivity, LoginActivity::class.java)
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
                    user = instance.queryByID(accountID)
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
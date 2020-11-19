package com.example.museumapplication.utils.authProviders

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.museumapplication.R
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.LoginActivity
import com.example.museumapplication.ui.auth.SignupActivity
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.AuthUtils.disableAllItems
import com.example.museumapplication.utils.AuthUtils.enableAllItems
import com.example.museumapplication.utils.AuthUtils.isFieldBlank
import com.example.museumapplication.utils.services.CloudDBHelper.Companion.instance
import com.huawei.agconnect.auth.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.TaskExecutors
import java.util.*

class EmailAuth : IBaseAuth {
    var auth: AGConnectAuth
    var email: String? = null
    var password: String? = null
    var name: String? = null
    var verificationCode: String? = null
    var context: Context? = null

    constructor(email: String?, password: String?, verificationCode: String?, context: Context?) {
        setCredentialInfo(email, password, name, verificationCode, context)
        auth = AGConnectAuth.getInstance()
    }

    constructor(email: String?, password: String?, context: Context?) {
        setCredentialInfo(email, password, "", name, context)
        auth = AGConnectAuth.getInstance()
    }

    constructor() {
        auth = AGConnectAuth.getInstance()
    }

    fun setCredentialInfo(email: String?, password: String?, verificationCode: String?, Name: String?, context: Context?) {
        this.email = email
        this.password = password
        this.verificationCode = verificationCode
        name = Name
        this.context = context
    }

    override fun login() {
        val credential = EmailAuthProvider.credentialWithPassword(email, password)
        disableAllItems((context as LoginActivity?)!!.findViewById(R.id.linearLayout))
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener { signInResult: SignInResult? ->
                    // Obtain sign-in information.
                    Log.d("Login:", "Success")
                    var user: User? = null
                    try {
                        user = instance.queryByEmail(email)
                    } catch (e: AGConnectCloudDBException) {
                        e.printStackTrace()
                    }
                    UserLoggedIn.instance.setUser(user!!)
                    val home = Intent(context, HomeActivity::class.java)
                    context!!.startActivity(home)
                    enableAllItems((context as LoginActivity?)!!.findViewById(R.id.linearLayout))
                }
                .addOnFailureListener { e: Exception ->
                    Log.d("Login:", "Fail $e")
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    enableAllItems((context as LoginActivity?)!!.findViewById(R.id.linearLayout))
                }
    }

    fun register() {
        val emailUser = EmailUser.Builder()
                .setEmail(email)
                .setVerifyCode(verificationCode)
                .setPassword(password) // Optional. If this parameter is set, the current user has created a password and can use the password to sign in.
                // If this parameter is not set, the user can only sign in using a verification code.
                .build()
        AGConnectAuth.getInstance().createUser(emailUser)
                .addOnSuccessListener { signInResult: SignInResult ->
                    // After an account is created, the user is signed in by default.
                    Log.d("Register:", "Success")
                    val user = User(signInResult.user.uid, signInResult.user.uid, email, name, "")
                    if (instance.checkFirstTimeUser(email)) {
                        instance.upsertUser(user)
                        instance.upsertAccountLinkInfo(LinkedAccount(user.uid, user.uid))
                        UserLoggedIn.instance.setUser(user)
                    } else {
                        val primaryAccount: User?
                        try {
                            primaryAccount = instance.queryByEmail(email)
                            instance.upsertAccountLinkInfo(LinkedAccount(user.uid, primaryAccount!!.uid))
                            UserLoggedIn.instance.setUser(primaryAccount)
                        } catch (e: AGConnectCloudDBException) {
                            e.printStackTrace()
                        }
                    }
                    val homeActivity = Intent(context, HomeActivity::class.java)
                    context!!.startActivity(homeActivity)
                }
                .addOnFailureListener { e: Exception ->
                    Log.d("Register:", "Fail$e")
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
    }

    fun createVerificationCode(context: Context) {
        val email = (context as SignupActivity).findViewById<EditText>(R.id.emailEditText)
        if (!isFieldBlank(email)) {
            val toBeSetDisabled = context.findViewById<Button>(R.id.requestCodeButton)
            val timerText = context.findViewById<TextView>(R.id.timerText)
            val settings = VerifyCodeSettings.newBuilder()
                    .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN) //ACTION_REGISTER_LOGIN/ACTION_RESET_PASSWORD
                    .sendInterval(120) // Minimum sending interval, ranging from 30s to 120s.
                    .locale(Locale.getDefault()) // Language in which a verification code is sent, which is optional. The default value is Locale.getDefault.
                    .build()
            val task = EmailAuthProvider.requestVerifyCode(email.text.toString(), settings)
            task.addOnSuccessListener(TaskExecutors.uiThread(), {
                Log.d("Verification:", "Success")
                object : CountDownTimer(120000, 1000) {
                    @SuppressLint("SetTextI18n")
                    override fun onTick(millisUntilFinished: Long) {
                        toBeSetDisabled.isEnabled = false
                        toBeSetDisabled.setBackgroundColor(Color.parseColor("#bdbdbd"))
                        toBeSetDisabled.setText(R.string.resend_verification_code)
                        timerText.visibility = View.VISIBLE
                        timerText.text = "Wait " + millisUntilFinished / 1000 + " seconds to resend the Verification Code"
                    }

                    override fun onFinish() {
                        timerText.visibility = View.GONE
                        toBeSetDisabled.isEnabled = true
                        toBeSetDisabled.setBackgroundColor(Color.parseColor("#c62828"))
                    }
                }.start()
            }).addOnFailureListener(TaskExecutors.uiThread(), { e: Exception ->
                Log.d("Verification:", "Fail:$e")
                if (e.message!!.contains("203818048")) {
                    val warningMessage = Toast.makeText(context, "You have already requested verification code for this email recently . \n\nPlease wait 2 minutes before requesting new one.", Toast.LENGTH_LONG)
                    warningMessage.setGravity(Gravity.TOP, 0, 135)
                    warningMessage.show()
                } else {
                    val warningMessage = Toast.makeText(context, e.message, Toast.LENGTH_LONG)
                    warningMessage.setGravity(Gravity.TOP, 0, 135)
                    warningMessage.show()
                }
            })
        } else email.error = "Fill this field to get verification code!"
    }
}
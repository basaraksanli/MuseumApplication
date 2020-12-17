package com.example.museumapplication.utils.auth

import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.example.museumapplication.R
import com.example.museumapplication.data.Constant
import com.example.museumapplication.data.LinkedAccount
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.SharedAuthViewModel
import com.example.museumapplication.utils.auth.AuthUtils.isFieldBlank
import com.example.museumapplication.utils.services.CloudDBManager.Companion.instance
import com.huawei.agconnect.auth.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.TaskExecutors
import java.util.*

class EmailAuth : IBaseAuth {
    private var email: String?  = null
    private var password: String? = null
    var viewModel: SharedAuthViewModel? = null
    var name: String? = null
    private var verificationCode: String? = null

    var auth: AGConnectAuth = AGConnectAuth.getInstance()

    constructor(viewModel: SharedAuthViewModel){
        this.viewModel =viewModel
    }

    constructor(email: String?, password: String?, viewModel: SharedAuthViewModel){
        this.email= email
        this.password = password
        this.viewModel = viewModel
    }


    /**
     * sets variables from out of the class
     */
    fun setCredentialInfo(email: String , password: String, verificationCode : String, name : String){
        this.email = email
        this.password = password
        this.verificationCode = verificationCode
        this.name = name
    }

    /**
     * login task with email and password
     */
    override fun login() {
        val credential = EmailAuthProvider.credentialWithPassword(email, password)
        viewModel!!.itemClickableOrEnabled.postValue(false)

        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener {
                    // Obtain sign-in information.
                    Log.d("Login:", "Success")
                    var user: User? = null
                    try {
                        user = instance.getUserByEmail(email)
                    } catch (e: AGConnectCloudDBException) {
                        e.printStackTrace()
                    }
                    UserLoggedIn.instance.setUser(user!!)
                    viewModel!!.navigateToHomePage.postValue(true)
                    viewModel!!.itemClickableOrEnabled.postValue(true)
                    viewModel!!.progressBarVisibility.postValue(View.GONE)
                }
                .addOnFailureListener { e: Exception ->
                    Log.d("Login:", "Fail $e")
                    Toast.makeText(viewModel!!.mContext, e.message, Toast.LENGTH_LONG).show()
                    viewModel!!.itemClickableOrEnabled.postValue(true)
                    viewModel!!.progressBarVisibility.postValue(View.GONE)
                }
    }

    /**
     * registration task in the registration page
     */
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
                            primaryAccount = instance.getUserByEmail(email)
                            instance.upsertAccountLinkInfo(LinkedAccount(user.uid, primaryAccount!!.uid))
                            UserLoggedIn.instance.setUser(primaryAccount)
                        } catch (e: AGConnectCloudDBException) {
                            e.printStackTrace()
                        }
                    }
                    viewModel!!.navigateToHomePage.postValue(true)
                    viewModel!!.progressBarVisibility.postValue(View.GONE)
                }
                .addOnFailureListener { e: Exception ->
                    Log.d("Register:", "Fail$e")
                    Toast.makeText(viewModel!!.mContext, e.message, Toast.LENGTH_LONG).show()
                    viewModel!!.progressBarVisibility.postValue(View.GONE)
                }
    }

    /**
     * This function creates verification code for the new registration
     * Requesting verification code interval is Constant.VerificationTimer value - default 120 second
     * Verification code is sent to email of the new user
     */
    fun createVerificationCode() {
        if (!isFieldBlank(viewModel!!.registerEmailText.value)) {

            val settings = VerifyCodeSettings.newBuilder()
                    .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN) //ACTION_REGISTER_LOGIN/ACTION_RESET_PASSWORD
                    .sendInterval(Constant.VERIFICATION_TIMER) // Minimum sending interval, ranging from 30s to 120s.
                    .locale(Locale.getDefault()) // Language in which a verification code is sent, which is optional. The default value is Locale.getDefault.
                    .build()
            val task = EmailAuthProvider.requestVerifyCode(viewModel!!.registerEmailText.value, settings)
            task.addOnSuccessListener(TaskExecutors.uiThread(), {
                Log.d("Verification:", "Success")
                viewModel!!.startCodeTimer()

            }).addOnFailureListener(TaskExecutors.uiThread(), { e: Exception ->
                Log.d("Verification:", "Fail:$e")
                if (e.message!!.contains("203818048")) {
                    val warningMessage = Toast.makeText(viewModel!!.mContext, viewModel!!.mContext!!.getString(R.string.verification_already_requested) +
                            "\n\nPlease wait 2 minutes before requesting new one.", Toast.LENGTH_LONG)
                    warningMessage.setGravity(Gravity.TOP, 0, 135)
                    warningMessage.show()
                } else {
                    val warningMessage = Toast.makeText(viewModel!!.mContext, e.message, Toast.LENGTH_LONG)
                    warningMessage.setGravity(Gravity.TOP, 0, 135)
                    warningMessage.show()
                }
            })
        } else Toast.makeText(viewModel!!.mContext , viewModel!!.mContext!!.getString(R.string.fill_verification_email_warning), Toast.LENGTH_LONG).show()
    }
}
package com.example.museumapplication.ui.auth

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.museumapplication.R
import com.example.museumapplication.data.Constant
import com.example.museumapplication.utils.auth.*
import com.example.museumapplication.utils.services.CloudDBManager
import com.facebook.login.widget.LoginButton
import com.google.android.gms.common.SignInButton
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton


/**
 * Shared Auth View Model is used by Auth Activity, Login Fragment, Sign Up Fragment
 */
class SharedAuthViewModel(application: Application) : AndroidViewModel(application) {
    val mContext: Context? = application.applicationContext
    lateinit var extra: Bundle

    var progressBarVisibility = MutableLiveData(View.GONE)

    //Login Fragment
    var emailText = MutableLiveData<String>()
    var passwordText = MutableLiveData<String>()
    var facebookLoginClicked = MutableLiveData(false)


    //SignUp Fragment variables
    var registerEmailText = MutableLiveData<String>()
    var registerPasswordText = MutableLiveData<String>()
    var repeatPass = MutableLiveData<String>()
    var registerName = MutableLiveData<String>()
    var verificationCode = MutableLiveData<String>()
    var timer = MutableLiveData<String>()
    var timerTextVisibility = MutableLiveData(View.GONE)
    var requestCodeButtonEnabled = MutableLiveData(true)
    var requestCodeText = MutableLiveData<String>()




    //Activity for result controls
    var signInIntent = MutableLiveData<Intent>()
    var RC_SIGN_IN: Int = 0

    //General View Interaction Lock for both fragments
    var itemClickableOrEnabled = MutableLiveData(true)

    //navigation variables
    var navigateToSignUpFragment = MutableLiveData(false)
    var navigateToMuseumPanel = MutableLiveData(false)
    var navigateToHomePage = MutableLiveData(false)
    var navigateToLoginPage = MutableLiveData(false)
    var navigateToEmailLogin = MutableLiveData(false)


    /**
     * Base Auth variable can be converted to Huawei, Facebook, Gmail, Email
     */
    lateinit var auth: IBaseAuth

    companion object {

        /**
         * Login Page
         * Google Sign in Button - GMS Availability control
         * If GMS is not available, Button will be hidden
         */
        @BindingAdapter("mContextGoogle")
        @JvmStatic
        fun checkGoogleAvailability(view: Button, mContext: Context) {
            if (!AuthUtils.checkGoogleServices(mContext)) {
                view.isEnabled = false
                view.visibility = View.GONE
            }
        }

        /**
         * Registration Page
         * Verification Button background color assignment
         */
        @BindingAdapter("buttonIsEnabled")
        @JvmStatic
        fun requestCodeButtonEnabled(view: Button, isEnabled: Boolean) {
            if (isEnabled) {
                view.setBackgroundColor(Color.parseColor("#c62828"))
            } else {
                view.setBackgroundColor(Color.parseColor("#bdbdbd"))
            }
        }
    }

    /**
     * Register Button Clicked. Base Auth is initialized as EmailAuth
     */
    fun registerButtonClicked(view: View?) {
        auth = EmailAuth(this)
        navigateToSignUpFragment.postValue(true)
    }

    /**
     * Sign in Button Clicked. Base Auth is initialized as EmailAuth
     */
    fun signInButtonClicked(view: View?) {
        progressBarVisibility.postValue(View.VISIBLE)
        auth = EmailAuth(emailText.value, passwordText.value, this)
        auth.login()
    }

    /**
     * Google Sign in Button Clicked. Base Auth is initiated as GoogleAuth
     */
    fun googleButtonClicked(view: View?) {
        progressBarVisibility.postValue(View.VISIBLE)
        auth = GoogleAuth(this)
        auth.login()
    }

    /**
     * Huawei Sign In Button Clicked. Base Auth is initialized as HuaweiAuth
     */
    fun huaweiButtonClicked(view: View?) {
        progressBarVisibility.postValue(View.VISIBLE)
        auth = HuaweiAuth(this)
        auth.login()
    }

    /**
     * Facebook Sign in Button Clicked. Base Auth is initialized as FacebookAuth
     */
    fun facebookButtonClicked(view: View?) {
        progressBarVisibility.postValue(View.VISIBLE)
        auth = FacebookAuth(this)
        facebookLoginClicked.postValue(true)
        auth.login()
    }
    fun navigationEmailLoginButton(view:View)
    {
        navigateToEmailLogin.postValue(true)
    }

    /**
     * Museum Panel Sign in Button Clicked. If the museum ID and password matches, it directs to the museum panel of that specific museum
     */
    fun museumPanelClick(view: View) {
        progressBarVisibility.postValue(View.VISIBLE)
        if(emailText.value != null && passwordText.value != null) {
            val museum = CloudDBManager.instance.checkMuseumIdAndPassword(emailText.value!!, passwordText.value!!)
            if (museum != null) {
                extra = Bundle()
                extra.putString("museumID", museum.museumID)
                navigateToMuseumPanel.postValue(true)
            } else {
                Toast.makeText(mContext, "Museum password or ID is wrong!", Toast.LENGTH_SHORT).show()
            }
            progressBarVisibility.postValue(View.GONE)
        }
        else{
            progressBarVisibility.postValue(View.GONE)
            Toast.makeText(mContext, "Please fill the museum ID and the password", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Request Verification Button Clicked.
     * Base Auth is initialized as Email Auth.
     */
    fun requestVerificationButtonClicked(view: View?) {
        (auth as EmailAuth?)!!.createVerificationCode()
    }

    /**
     * Register Complete Button Clicked
     * Base Auth is initiated as Email Auth
     * All fields are checked if everything is correctly filled
     * Registration task is initiated
     */
    fun registerCompleteButtonClicked(view: View?) {
        progressBarVisibility.postValue(View.VISIBLE)
        if (AuthUtils.checkFields(registerEmailText.value, registerPasswordText.value, repeatPass.value, verificationCode.value, registerName.value)) {
            (auth as EmailAuth).setCredentialInfo(registerEmailText.value!!, registerPasswordText.value!!, verificationCode.value!!, registerName.value!!)
            (auth as EmailAuth?)!!.register()
        }
        else
            Toast.makeText(mContext, "Please control and fill all the fields.", Toast.LENGTH_LONG).show()
    }


    /**
     * This function disables the verification code request button for 120 seconds
     */
    fun startCodeTimer() {
        requestCodeText.postValue(mContext!!.getString(R.string.resend_verification_code))
        object : CountDownTimer((Constant.VERIFICATION_TIMER * 1000).toLong(), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                requestCodeButtonEnabled.postValue(false)
                timerTextVisibility.postValue(View.VISIBLE)
                timer.value = "Wait " + millisUntilFinished / 1000 + " seconds to resend the Verification Code"
            }
            override fun onFinish() {
                timerTextVisibility.postValue(View.GONE)
                requestCodeButtonEnabled.postValue(true)
            }
        }.start()
    }

}
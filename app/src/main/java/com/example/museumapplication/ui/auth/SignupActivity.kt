package com.example.museumapplication.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.museumapplication.R
import com.example.museumapplication.utils.auth.EmailAuth
import com.example.museumapplication.utils.auth.IBaseAuth
import com.example.museumapplication.utils.auth.AuthUtils.checkFields

class SignupActivity : AppCompatActivity() {
    var emailAuth: IBaseAuth? = null
    var email: EditText? = null
    var password: EditText? = null
    var repeatPass: EditText? = null
    var name: EditText? = null
    var verificationCode: EditText? = null
    var requestCodeButton: Button? = null
    var timerText: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        repeatPass = findViewById(R.id.repeatPassEditText)
        name = findViewById(R.id.nameEditText)
        verificationCode = findViewById(R.id.verificationCodeEditText)
        requestCodeButton = findViewById(R.id.requestCodeButton)
        timerText = findViewById(R.id.timerText)
        emailAuth = EmailAuth()
    }

    fun requestVerificationButtonClicked(view: View?) {
        (emailAuth as EmailAuth?)!!.createVerificationCode(this)
    }

    fun registerButtonClicked(view: View?) {
        if (checkFields(email!!, password!!, repeatPass!!, verificationCode!!, name!!)) {
            (emailAuth as EmailAuth?)!!.setCredentialInfo(email!!.text.toString(), password!!.text.toString(), verificationCode!!.text.toString(), name!!.text.toString(), this)
            (emailAuth as EmailAuth?)!!.register()
        }
    }
}
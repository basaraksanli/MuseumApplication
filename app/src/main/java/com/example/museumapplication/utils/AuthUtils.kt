package com.example.museumapplication.utils

import android.content.Context
import android.util.Patterns
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object AuthUtils {
    fun isEmailValid(email: String?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordLengthGreaterThan5(password: String): Boolean {
        return password.length > 5
    }

    @JvmStatic
    fun isFieldBlank(textView: TextView): Boolean {
        val textString = textView.text.toString().trim { it <= ' ' }
        return textString.isEmpty()
    }

    @JvmStatic
    fun checkFields(email: TextView, password: TextView, repeatPass: TextView, verificationCode: TextView, name: TextView): Boolean {
        var result = true
        if (isFieldBlank(email)) {
            result = false
            email.error = "Email is required to register!"
        } else if (!isEmailValid(email.text.toString())) {
            result = false
            email.error = "Email is not valid!"
        }
        if (isFieldBlank(password)) {
            result = false
            password.error = "Password is required to register!"
        } else if (!isPasswordLengthGreaterThan5(password.text.toString())) {
            result = false
            password.error = "Password length must be greater than 5 and must contain at least one character!"
        }
        if (isFieldBlank(repeatPass)) {
            result = false
            repeatPass.error = "Repeat Password is required to register!"
        } else if (repeatPass.text.toString() != password.text.toString()) {
            result = false
            repeatPass.error = "Passwords does not match!"
        }
        if (isFieldBlank(verificationCode)) {
            result = false
            verificationCode.error = "Fill here with the verification code you received to your mail!"
        }
        if (isFieldBlank(name)) {
            result = false
            name.error = "This field is mandatory!"
        }
        return result
    }

    @JvmStatic
    fun checkGoogleServices(context: Context?): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return if (resultCode != ConnectionResult.SUCCESS) {
            false
        } else true
    }

    @JvmStatic
    fun disableAllItems(layout: LinearLayout) {
        for (i in 0 until layout.childCount) {
            val v = layout.getChildAt(i)
            if (v.tag != null) {
                if (v.tag.toString() == "progressBar") v.visibility = View.VISIBLE
            } else v.isEnabled = false
        }
    }

    @JvmStatic
    fun enableAllItems(layout: LinearLayout) {
        for (i in 0 until layout.childCount) {
            val v = layout.getChildAt(i)
            if (v.tag != null) {
                if (v.tag.toString() == "progressBar") v.visibility = View.GONE
            } else v.isEnabled = true
        }
    }
}
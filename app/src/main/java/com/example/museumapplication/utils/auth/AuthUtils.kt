package com.example.museumapplication.utils.auth

import android.content.Context
import android.util.Patterns
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object AuthUtils {

    /**
     * Auth Utilities
     */

    /**
     * Checks if the email is valid
     */
    private fun isEmailValid(email: String?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * checks password length
     */
    private fun isPasswordLengthGreaterThan5(password: String): Boolean {
        return password.length > 5
    }

    /**
     * checks if the field blank
     */
    @JvmStatic
    fun isFieldBlank(string: String?): Boolean {
        return if (string == null)
            true
        else
            return string.isBlank()


    }

    /**
     * check all the fields in registration page
     */
    @JvmStatic
    fun checkFields(email: String?, password: String?, repeatPass: String?, verificationCode: String?, name: String?): Boolean {
        var result = true
        if (email == null || password == null || repeatPass == null || verificationCode == null || name == null)
            return false

        if (isFieldBlank(email)) {
            result = false
        } else if (!isEmailValid(email)) {
            result = false
        }
        if (isFieldBlank(password)) {
            result = false
        } else if (!isPasswordLengthGreaterThan5(password)) {
            result = false
        }
        if (isFieldBlank(repeatPass)) {
            result = false
        } else if (repeatPass != password) {
            result = false
        }
        if (isFieldBlank(verificationCode)) {
            result = false
        }
        if (isFieldBlank(name)) {
            result = false
        }
        return result
    }

    /**
     * check google services availability
     */
    @JvmStatic
    fun checkGoogleServices(context: Context?): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }
}
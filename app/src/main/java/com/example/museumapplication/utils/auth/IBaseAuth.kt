package com.example.museumapplication.utils.auth

import android.content.Context
import android.content.Intent
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.AuthActivity
import com.example.museumapplication.ui.auth.LoginFragment
import com.huawei.agconnect.auth.AGConnectAuth

interface IBaseAuth {


    /**
     * Strategy Pattern for Auth methods.
     * With this pattern new auth methods can be added easily
     * All methods are implemented according to this interface
     */
    fun login()

    companion object {
        val currentUser: UserLoggedIn
            get() = UserLoggedIn.instance

        @JvmStatic
        fun logout(context: Context) {
            AGConnectAuth.getInstance().signOut()
            val login = Intent(context, AuthActivity::class.java)
            context.startActivity(login)
        }
    }
}
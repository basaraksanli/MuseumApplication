package com.example.museumapplication.utils.auth

import android.content.Context
import android.content.Intent
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.auth.LoginActivity
import com.huawei.agconnect.auth.AGConnectAuth

interface IBaseAuth {
    fun login()

    companion object {
        val currentUser: UserLoggedIn?
            get() = UserLoggedIn.instance

        @JvmStatic
        fun logout(context: Context) {
            AGConnectAuth.getInstance().signOut()
            val login = Intent(context, LoginActivity::class.java)
            context.startActivity(login)
        }
    }
}
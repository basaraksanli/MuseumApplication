package com.example.museumapplication.data;

import android.content.Context;

import com.example.museumapplication.utils.AuthProviders.EmailAuth;
import com.example.museumapplication.utils.AuthProviders.FacebookAuth;
import com.example.museumapplication.utils.AuthProviders.GoogleAuth;
import com.example.museumapplication.utils.AuthProviders.HuaweiAuth;
import com.example.museumapplication.utils.AuthProviders.IBaseAuth;

public class UserLoggedIn {
    private static String uID;
    private static String name;
    private static String email;

    public UserLoggedIn(String uID, String name, String email) {
        UserLoggedIn.uID = uID;
        UserLoggedIn.name = name;
        UserLoggedIn.email = email;
    }

    public static String getuID() {
        return uID;
    }

    public static String getName() {
        return name;
    }

    public static String getEmail() {
        return email;
    }


}

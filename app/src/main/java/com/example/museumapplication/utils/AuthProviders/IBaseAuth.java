package com.example.museumapplication.utils.AuthProviders;

import android.content.Context;
import android.content.Intent;



import com.example.museumapplication.ui.auth.LoginActivity;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;

public interface IBaseAuth {
    void login();
    AGConnectUser getCurrentUser();

    static void logout(Context context ){
        AGConnectAuth.getInstance().signOut();
        Intent login = new Intent(context, LoginActivity.class);
        context.startActivity(login);
    }
}

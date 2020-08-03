package com.example.museumapplication.utils.AuthProviders;
import android.content.Context;
import android.content.Intent;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.huawei.agconnect.auth.AGConnectAuth;



public interface IBaseAuth {
    void login();

    static UserLoggedIn getCurrentUser(){
        return UserLoggedIn.getInstance();
    }

    static void logout(Context context ){
        AGConnectAuth.getInstance().signOut();
        Intent login = new Intent(context, LoginActivity.class);
        context.startActivity(login);
    }

}

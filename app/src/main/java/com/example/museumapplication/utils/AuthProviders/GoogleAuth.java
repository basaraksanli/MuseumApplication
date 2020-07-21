package com.example.museumapplication.utils.AuthProviders;

import com.example.museumapplication.utils.IBaseAuth;
import com.huawei.agconnect.auth.AGConnectUser;

public class GoogleAuth implements IBaseAuth {
    @Override
    public void login(String email, String password) {

    }

    @Override
    public void logout() {

    }

    @Override
    public AGConnectUser getCurrentUser() {
        return null;
    }
}

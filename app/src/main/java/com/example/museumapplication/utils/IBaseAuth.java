package com.example.museumapplication.utils;

import com.huawei.agconnect.auth.AGConnectUser;

public interface IBaseAuth {
    void login(String email,String password);
    void logout();
    AGConnectUser getCurrentUser();
}

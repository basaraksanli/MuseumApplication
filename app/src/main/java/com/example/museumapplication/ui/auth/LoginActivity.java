package com.example.museumapplication.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.museumapplication.R;
import com.huawei.agconnect.auth.AGConnectAuth;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AGConnectAuth agConnectAuth = AGConnectAuth.getInstance();

        if(agConnectAuth.getCurrentUser()==null)
        {

        }
        else {
            //Intent homeActivity = new Intent(this, HomeActivity.class);
            //startActivity(homeActivity);
        }
    }
    public void registerButtonClicked(View v){
        Intent register = new Intent(this,SignupActivity.class);
        startActivity(register);
    }
}

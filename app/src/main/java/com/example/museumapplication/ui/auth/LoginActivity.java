package com.example.museumapplication.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.FaceDetector;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.museumapplication.R;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthProviders.EmailAuth;
import com.example.museumapplication.utils.AuthProviders.FacebookAuth;
import com.example.museumapplication.utils.AuthProviders.GoogleAuth;
import com.example.museumapplication.utils.AuthProviders.HuaweiAuth;
import com.example.museumapplication.utils.AuthProviders.IBaseAuth;
import com.example.museumapplication.utils.AuthUtils;
import com.facebook.login.widget.LoginButton;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;

public class LoginActivity extends AppCompatActivity {

    TextView email;
    TextView password;
    IBaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AGConnectAuth agConnectAuth = AGConnectAuth.getInstance();
        ImageButton googleButton= findViewById(R.id.googleButton);
        if(!AuthUtils.checkGoogleServices(this)) {
            googleButton.setEnabled(false);
            googleButton.setVisibility(View.GONE);
        }

        if (agConnectAuth.getCurrentUser() == null) {


        } else {
            AGConnectUser user =agConnectAuth.getCurrentUser();
            new UserLoggedIn(user.getUid(), user.getDisplayName(), user.getEmail());

            Intent homeActivity = new Intent(this, HomeActivity.class);
            startActivity(homeActivity);
        }
        email = findViewById(R.id.editEmail);
        password = findViewById(R.id.editPassword);
    }

    public void registerButtonClicked(View v) {
        Intent register = new Intent(this, SignupActivity.class);
        startActivity(register);
    }

    public void signInButtonClicked(View view) {
        auth = new EmailAuth(email.getText().toString(), password.getText().toString(), this);
        auth.login();
    }

    public void googleButtonClicked(View view) {
        auth = new GoogleAuth(this);
        auth.login();
    }
    public void huaweiButtonClicked(View view) {
        auth = new HuaweiAuth(this);
        auth.login();
    }
    public void facebookButtonClicked(View view) {
        auth = new FacebookAuth(this);
        LoginButton facebookButton = findViewById(R.id.facebookButton);
        facebookButton.performClick();
        auth.login();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        AuthUtils.disableAllItems(findViewById(R.id.linearLayout));
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001) {
            ((GoogleAuth)auth).activityResult(data,this);
        }
        if (requestCode== 8888){
            ((HuaweiAuth)auth).activityResult(data);
        }
        if (requestCode== 64206){
            ((FacebookAuth)auth).activityResult(requestCode,resultCode,data);
        }
    }



}

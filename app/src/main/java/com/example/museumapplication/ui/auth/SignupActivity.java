package com.example.museumapplication.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.museumapplication.R;
import com.example.museumapplication.utils.AuthProviders.IBaseAuth;
import com.example.museumapplication.utils.AuthUtils;
import com.example.museumapplication.utils.AuthProviders.EmailAuth;

public class SignupActivity extends AppCompatActivity {

    IBaseAuth emailAuth;
    EditText email;
    EditText password;
    EditText repeatPass;
    EditText name;
    EditText verificationCode;

    Button requestCodeButton;
    TextView timerText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email=  findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        repeatPass= findViewById(R.id.repeatPassEditText);
        name = findViewById(R.id.nameEditText);
        verificationCode = findViewById(R.id.verificationCodeEditText);

        requestCodeButton = findViewById(R.id.requestCodeButton);
        timerText = findViewById(R.id.timerText);

        emailAuth = new EmailAuth();

    }

    public void requestVerificationButtonClicked(View view) {
        ((EmailAuth)emailAuth).createVerificationCode(this);
    }

    public void registerButtonClicked(View view) {

        if(AuthUtils.checkFields(email, password, repeatPass,verificationCode,name)){
            ((EmailAuth)emailAuth).setCredentialInfo(email.getText().toString(), password.getText().toString(), verificationCode.getText().toString(), this);
            ((EmailAuth)emailAuth).register();
        }
    }


}
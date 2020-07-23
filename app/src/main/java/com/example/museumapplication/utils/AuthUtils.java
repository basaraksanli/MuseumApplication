package com.example.museumapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.museumapplication.ui.auth.LoginActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;

public class AuthUtils {

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordLengthGreaterThan5(String password) {
        return password.length() > 5;
    }

    public static boolean isFieldBlank(TextView textView){
        String textString = textView.getText().toString().trim();
        return textString.matches("");
    }

    public static boolean checkFields(TextView email, TextView password, TextView repeatPass, TextView verificationCode, TextView name ) {
        boolean result = true;
        if (isFieldBlank(email)) {
            result = false;
            email.setError("Email is required to register!");
        } else if (!AuthUtils.isEmailValid(email.getText().toString())) {
            result = false;
            email.setError("Email is not valid!");
        }

        if (isFieldBlank(password)) {
            result = false;
            password.setError("Password is required to register!");
        } else if (!AuthUtils.isPasswordLengthGreaterThan5(password.getText().toString())) {
            result = false;
            password.setError("Password length must be greater than 5 and must contain at least one character!");
        }

        if (isFieldBlank(repeatPass)) {
            result = false;
            repeatPass.setError("Repeat Password is required to register!");
        } else if (!repeatPass.getText().toString().equals(password.getText().toString())) {
            result = false;
            repeatPass.setError("Passwords does not match!");
        }
        if (isFieldBlank(verificationCode)){
            result = false;
            verificationCode.setError("Fill here with the verification code you received to your mail!");
        }
        if(isFieldBlank(name))
        {
            result= false;
            name.setError("This field is mandatory!");
        }

        return result;
    }

    public static boolean checkGoogleServices(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }
    public static void disableAllItems(LinearLayout layout){
        for(int i=0 ; i<layout.getChildCount(); i++){
            View v =layout.getChildAt(i);
            if(v.getTag()!=null) {
                if (v.getTag().toString().equals("progressBar"))
                    v.setVisibility(View.VISIBLE);
            }
            else
                v.setEnabled(false);
        }
    }
    public static void enableAllItems(LinearLayout layout){
        for(int i=0 ; i<layout.getChildCount(); i++){
            View v =layout.getChildAt(i);
            if(v.getTag()!=null) {
                if (v.getTag().toString().equals("progressBar"))
                    v.setVisibility(View.GONE);
            }
            else
                v.setEnabled(true);
        }
    }
}

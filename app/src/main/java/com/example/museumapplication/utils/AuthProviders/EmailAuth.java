package com.example.museumapplication.utils.AuthProviders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.museumapplication.R;
import com.example.museumapplication.utils.IBaseAuth;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.Locale;

public class EmailAuth implements IBaseAuth {
    AGConnectAuth auth;
    public EmailAuth(){
        auth= AGConnectAuth.getInstance();
    }
    @Override
    public void login(String email, String password) {
        AGConnectAuthCredential credential = EmailAuthProvider.credentialWithPassword("your Email", "password");

        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // Obtain sign-in information.
                        Log.d("Login:" , "Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("Login:", "Fail " + e);
                    }
                });
    }

    @Override
    public void logout() {

    }

    public void register(final String email, final String password, String verificationCode, final Context context) {
        EmailUser emailUser = new EmailUser.Builder()
                .setEmail(email)
                .setVerifyCode(verificationCode)
                .setPassword(password) // Optional. If this parameter is set, the current user has created a password and can use the password to sign in.
                // If this parameter is not set, the user can only sign in using a verification code.
                .build();
        AGConnectAuth.getInstance().createUser(emailUser)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // After an account is created, the user is signed in by default.
                        Log.d("Register:", "Success" );
                        /*Intent homeActivity = new Intent(context,HomeActivity.class);
                        context.startActivity(homeActivity);*/
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("Register:", "Fail" + e );
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public AGConnectUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void createVerificationCode(String emailStr, final Button toBeSetDisabled, final TextView timerText, final Context context){
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)   //ACTION_REGISTER_LOGIN/ACTION_RESET_PASSWORD
                .sendInterval(120) // Minimum sending interval, ranging from 30s to 120s.
                .locale(Locale.getDefault()) // Language in which a verification code is sent, which is optional. The default value is Locale.getDefault.
                .build();
        Task<VerifyCodeResult> task= EmailAuthProvider.requestVerifyCode(emailStr, settings);
        task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
            @Override
            public void onSuccess(VerifyCodeResult verifyCodeResult) {
                Log.d("Verification:", "Success");
                new CountDownTimer(120000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        toBeSetDisabled.setEnabled(false);
                        toBeSetDisabled.setBackgroundColor(Color.parseColor("#bdbdbd"));
                        toBeSetDisabled.setText(R.string.resend_verification_code);
                        timerText.setVisibility(View.VISIBLE);
                        timerText.setText("Wait " + millisUntilFinished/1000 + " seconds to resend the Verification Code");
                    }
                    @Override
                    public void onFinish() {
                        timerText.setVisibility(View.GONE);
                        toBeSetDisabled.setEnabled(true);
                        toBeSetDisabled.setBackgroundColor(Color.parseColor("#c62828"));
                    }
                }.start();
            }
        }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d("Verification:", "Fail:" + e);
                if(e.getMessage().contains("203818048")) {
                    Toast warningMessage = Toast.makeText(context, "You have already requested verification code for this email recently . \n\nPlease wait 2 minutes before requesting new one.", Toast.LENGTH_LONG);
                    warningMessage.setGravity(Gravity.TOP,0,135);
                    warningMessage.show();
                }
            }
        });
    }

}

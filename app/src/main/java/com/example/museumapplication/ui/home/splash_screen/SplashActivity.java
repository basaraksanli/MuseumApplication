package com.example.museumapplication.ui.home.splash_screen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.example.museumapplication.R;
import com.example.museumapplication.data.User;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.Services.CloudDBHelper;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplashActivity extends AppCompatActivity {

    AGConnectUser agcuser;
    AGConnectAuth agConnectAuth;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.museumapplication",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        CloudDBHelper.getInstance().initAGConnectCloudDB(this);

        agConnectAuth = AGConnectAuth.getInstance();
        agcuser = agConnectAuth.getCurrentUser();

        if (agConnectAuth.getCurrentUser() != null) {
            AsyncTask<Activity, Void, Void> loginTask=new LoginTask().execute(this);
            countDownTimer =new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) { }
                public void onFinish() {
                    loginTask.cancel(true);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                    builder.setTitle(getResources().getIdentifier("app_name", "string", getPackageName()));
                    builder.setMessage("Could not connect to internet services. Check your network");
                    builder.setNegativeButton("EXIT", (dialogInterface, i) ->{
                        finish();
                        System.exit(0);
                    });
                    builder.setPositiveButton("RETRY", (dialogInterface, i) -> {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    });
                    builder.show();
                }
            }.start();
        }
        else{
            new Handler().postDelayed(() -> {
                Intent loginActivity = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(loginActivity);
            }, 500);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class LoginTask extends AsyncTask<Activity, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Activity... params) {
            String accountID;
            try {
                User user;
                do {
                    accountID = CloudDBHelper.getInstance().getPrimaryAccountID_LinkedAccount(agcuser.getUid());
                    user = CloudDBHelper.getInstance().queryByID(accountID);
                    if (user != null)
                        UserLoggedIn.getInstance().setUser(user);
                } while (user == null);
            } catch (AGConnectCloudDBException e) {
                e.printStackTrace();
            }
            countDownTimer.cancel();
            Intent homeActivity = new Intent(params[0], HomeActivity.class);
            if(getIntent().getExtras()!=null)
            {
                Bundle extra= new Bundle();
                extra.putString("MuseumName", getIntent().getStringExtra("MuseumName"));
                homeActivity.putExtras(extra);
            }
            startActivity(homeActivity);

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Void result) {
            super.onCancelled(result);
        }
    }
}
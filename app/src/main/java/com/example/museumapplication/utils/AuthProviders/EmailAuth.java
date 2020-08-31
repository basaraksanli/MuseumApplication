package com.example.museumapplication.utils.AuthProviders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.museumapplication.R;
import com.example.museumapplication.data.LinkedAccount;
import com.example.museumapplication.data.User;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.auth.SignupActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthUtils;
import com.example.museumapplication.utils.CloudDBHelper;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.Locale;
import java.util.Objects;

public class EmailAuth implements IBaseAuth {
    AGConnectAuth auth;
    String email;
    String password;
    String name;
    String verificationCode;
    Context context;

    public EmailAuth(String email, String password, String verificationCode, Context context) {
        setCredentialInfo(email, password, name, verificationCode, context);
        auth = AGConnectAuth.getInstance();
    }

    public EmailAuth(String email, String password, Context context) {
        setCredentialInfo(email, password, "", name, context);
        auth = AGConnectAuth.getInstance();
    }

    public EmailAuth() {
        auth = AGConnectAuth.getInstance();
    }

    public void setCredentialInfo(String email, String password, String verificationCode, String Name, Context context) {
        this.email = email;
        this.password = password;
        this.verificationCode = verificationCode;
        this.name = Name;
        this.context = context;
    }

    @Override
    public void login() {

        AGConnectAuthCredential credential = EmailAuthProvider.credentialWithPassword(email, password);

        AuthUtils.disableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));

        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(signInResult -> {
                    // Obtain sign-in information.
                    Log.d("Login:", "Success");
                    User user=null;
                    try {
                        user = CloudDBHelper.getInstance().queryByEmail(email);
                    } catch (AGConnectCloudDBException e) {
                        e.printStackTrace();
                    }
                    UserLoggedIn.getInstance().setUser(user);

                    Intent home = new Intent(context, HomeActivity.class);
                    context.startActivity(home);
                    AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                })
                .addOnFailureListener(e -> {
                    Log.d("Login:", "Fail " + e);
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                });
    }

    public void register() {
        EmailUser emailUser = new EmailUser.Builder()
                .setEmail(email)
                .setVerifyCode(verificationCode)
                .setPassword(password) // Optional. If this parameter is set, the current user has created a password and can use the password to sign in.
                // If this parameter is not set, the user can only sign in using a verification code.
                .build();
        AGConnectAuth.getInstance().createUser(emailUser)
                .addOnSuccessListener(signInResult -> {
                    // After an account is created, the user is signed in by default.
                    Log.d("Register:", "Success");
                    User user = new User(signInResult.getUser().getUid(), signInResult.getUser().getUid(), email, name, "");
                    if (CloudDBHelper.getInstance().checkFirstTimeUser(email)) {
                        CloudDBHelper.getInstance().upsertUser(user);
                        CloudDBHelper.getInstance().upsertAccountLinkInfo(new LinkedAccount(user.getUID(), user.getUID()));
                        UserLoggedIn.getInstance().setUser(user);
                    } else {
                        User primaryAccount;
                        try {
                            primaryAccount = CloudDBHelper.getInstance().queryByEmail(email);
                            CloudDBHelper.getInstance().upsertAccountLinkInfo(new LinkedAccount(user.getUID(), primaryAccount.getUID()));
                            UserLoggedIn.getInstance().setUser(primaryAccount);
                        } catch (AGConnectCloudDBException e) {
                            e.printStackTrace();
                        }

                    }

                    Intent homeActivity = new Intent(context, HomeActivity.class);
                    context.startActivity(homeActivity);
                })
                .addOnFailureListener(e -> {
                    Log.d("Register:", "Fail" + e);
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void createVerificationCode(final Context context) {

        EditText email = ((SignupActivity) context).findViewById(R.id.emailEditText);
        if (!AuthUtils.isFieldBlank(email)) {

            Button toBeSetDisabled = ((SignupActivity) context).findViewById(R.id.requestCodeButton);
            TextView timerText = ((SignupActivity) context).findViewById(R.id.timerText);


            VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                    .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)   //ACTION_REGISTER_LOGIN/ACTION_RESET_PASSWORD
                    .sendInterval(120) // Minimum sending interval, ranging from 30s to 120s.
                    .locale(Locale.getDefault()) // Language in which a verification code is sent, which is optional. The default value is Locale.getDefault.
                    .build();
            Task<VerifyCodeResult> task = EmailAuthProvider.requestVerifyCode(email.getText().toString(), settings);
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
                            timerText.setText("Wait " + millisUntilFinished / 1000 + " seconds to resend the Verification Code");
                        }

                        @Override
                        public void onFinish() {
                            timerText.setVisibility(View.GONE);
                            toBeSetDisabled.setEnabled(true);
                            toBeSetDisabled.setBackgroundColor(Color.parseColor("#c62828"));
                        }
                    }.start();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), e -> {
                Log.d("Verification:", "Fail:" + e);
                if (Objects.requireNonNull(e.getMessage()).contains("203818048")) {
                    Toast warningMessage = Toast.makeText(context, "You have already requested verification code for this email recently . \n\nPlease wait 2 minutes before requesting new one.", Toast.LENGTH_LONG);
                    warningMessage.setGravity(Gravity.TOP, 0, 135);
                    warningMessage.show();
                } else {
                    Toast warningMessage = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
                    warningMessage.setGravity(Gravity.TOP, 0, 135);
                    warningMessage.show();
                }
            });
        } else
            email.setError("Fill this field to get verification code!");
    }


}

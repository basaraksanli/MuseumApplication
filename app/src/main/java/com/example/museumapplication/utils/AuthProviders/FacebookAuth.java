package com.example.museumapplication.utils.AuthProviders;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.museumapplication.R;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.FacebookAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;

public class FacebookAuth implements IBaseAuth {

    Context context;
    AGConnectAuth auth;
    LoginButton loginButton;
    CallbackManager mCallbackManager;

    public FacebookAuth(Context context) {

        this.context = context;

        auth = AGConnectAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = ((LoginActivity) context).findViewById(R.id.facebookButton);
    }

    @Override
    public void login() {
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FacebookLogin:", ":onSuccess:" + loginResult);
                try {
                    authWithFacebook(loginResult.getAccessToken());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {
                Log.d("FacebookLogin:", ":onCancel");
                AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FacebookLogin:", ":onError", error);
                AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void authWithFacebook(final AccessToken accessToken) {
        AGConnectAuthCredential credential = FacebookAuthProvider.credentialWithToken(accessToken.getToken());
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // onSuccess
                        AGConnectUser user = signInResult.getUser();
                        new UserLoggedIn(user.getUid(), user.getDisplayName() ,user.getEmail());

                        Intent home = new Intent(context, HomeActivity.class);
                        context.startActivity(home);
                        AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // onFail
                        AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    public AGConnectUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

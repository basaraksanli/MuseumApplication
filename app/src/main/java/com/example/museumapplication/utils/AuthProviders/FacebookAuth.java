package com.example.museumapplication.utils.AuthProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.museumapplication.R;
import com.example.museumapplication.data.LinkedAccount;
import com.example.museumapplication.data.User;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthUtils;
import com.example.museumapplication.utils.CloudDBHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.FacebookAuthProvider;
import com.huawei.agconnect.cloud.database.CloudDBZoneTask;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;

import org.json.JSONException;

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
        loginButton.setPermissions("email","public_profile");
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
    public void graphRequest(AccessToken accessToken, String UserID){
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    try {

                        if(CloudDBHelper.getInstance().checkFirstTimeUser(object.getString("email")))
                        {
                            User user = new User(UserID, object.getString("id"), object.getString("email"),object.getString("name"), "https://graph.facebook.com/"+ object.getString("id") + "/picture?type=large");
                            CloudDBHelper.getInstance().upsertUser(user);
                            CloudDBHelper.getInstance().upsertAccountLinkInfo(new LinkedAccount(UserID,user.getUID()));
                            UserLoggedIn.getInstance().setUser(user);

                            Intent home = new Intent(context, HomeActivity.class);
                            context.startActivity(home);
                        }
                        else{
                            try {
                                User user= CloudDBHelper.getInstance().queryByEmail(object.getString("email"));
                                CloudDBHelper.getInstance().upsertAccountLinkInfo(new LinkedAccount(object.getString("id"),user.getUID()));
                                UserLoggedIn.getInstance().setUser(user);

                                Intent home = new Intent(context, HomeActivity.class);
                                context.startActivity(home);
                            } catch (AGConnectCloudDBException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,birthday,email");
        request.setParameters(parameters);
        request.executeAsync();
    }


    public void authWithFacebook(final AccessToken accessToken) {
        AGConnectAuthCredential credential = FacebookAuthProvider.credentialWithToken(accessToken.getToken());
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(signInResult -> {
                    // onSuccess
                    AGConnectUser user = signInResult.getUser();
                    graphRequest(accessToken, user.getUid());


                    AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                })
                .addOnFailureListener(e -> {
                    // onFail
                    AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

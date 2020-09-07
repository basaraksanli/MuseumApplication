package com.example.museumapplication.utils.AuthProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.museumapplication.R;
import com.example.museumapplication.data.LinkedAccount;
import com.example.museumapplication.data.User;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthUtils;
import com.example.museumapplication.utils.Services.CloudDBHelper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.GoogleAuthProvider;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;

import java.util.Objects;


public class GoogleAuth implements IBaseAuth, GoogleApiClient.OnConnectionFailedListener {

    AGConnectAuth auth;
    Context context;
    GoogleApiClient client;
    private static final int RC_SIGN_IN = 9001;

    public GoogleAuth(Context context) {
        this.context = context;
        auth = AGConnectAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.google_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        client = new GoogleApiClient.Builder(context.getApplicationContext())
                .enableAutoManage((LoginActivity) context, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void login() {
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        ((LoginActivity) context).startActivityForResult(signIntent, RC_SIGN_IN);
    }


    public void activityResult(Intent data, Activity activity) {

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        Log.d("Login Google", "handleSignInResult:" + result.getStatus());
        if (result.isSuccess()) {
            AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
            GoogleSignInAccount account = result.getSignInAccount();
            if (account != null) {
                //set ProviderUID Name Email
                //UserLoggedIn.getInstance().setUser("" , account.getId(), account.getDisplayName() ,account.getEmail(), Objects.requireNonNull(account.getPhotoUrl()).toString());
                authWithGoogle(account);
            }
        } else {
            AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
            client.stopAutoManage((FragmentActivity) activity);
            client.disconnect();
        }
    }

    public void authWithGoogle(GoogleSignInAccount account) {

        AGConnectAuthCredential credential = GoogleAuthProvider.credentialWithToken(account.getIdToken());
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(signInResult -> {
                    // onSuccess
                    AGConnectUser agcUser = signInResult.getUser();

                    if (CloudDBHelper.getInstance().checkFirstTimeUser(account.getEmail())) {
                        User user = new User(agcUser.getUid(), account.getId(), account.getEmail(), account.getGivenName() + " " + account.getFamilyName(), Objects.requireNonNull(account.getPhotoUrl()).toString());
                        CloudDBHelper.getInstance().upsertUser(user);
                        CloudDBHelper.getInstance().upsertAccountLinkInfo(new LinkedAccount(user.getUID(),user.getUID()));
                        UserLoggedIn.getInstance().setUser(user);
                    } else {
                        try {
                            User user = CloudDBHelper.getInstance().queryByEmail(account.getEmail());
                            CloudDBHelper.getInstance().upsertAccountLinkInfo(new LinkedAccount(agcUser.getUid(),user.getUID()));
                            UserLoggedIn.getInstance().setUser(user);
                        } catch (AGConnectCloudDBException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent home = new Intent(context, HomeActivity.class);
                    context.startActivity(home);
                    AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));


                })
                .addOnFailureListener(e -> {
                    // onFail
                    AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

package com.example.museumapplication.utils.AuthProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.museumapplication.R;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthUtils;
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
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;


public class GoogleAuth implements IBaseAuth ,GoogleApiClient.OnConnectionFailedListener{

    AGConnectAuth auth;
    Context context;
    GoogleApiClient client;
    private static final int RC_SIGN_IN = 9001;

    public GoogleAuth(Context context){
        this.context = context;
        auth = AGConnectAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.google_client_id))
                .requestProfile()
                .build();
        client = new GoogleApiClient.Builder(context.getApplicationContext())
                .enableAutoManage((LoginActivity)context,  this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
    @Override
    public void login() {
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        ((LoginActivity)context).startActivityForResult(signIntent, RC_SIGN_IN);
    }

    @Override
    public AGConnectUser getCurrentUser() {
        return auth.getCurrentUser();
    }


    public void activityResult(Intent data, Activity activity) {

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        Log.d("Login Google", "handleSignInResult:" + result.getStatus());
        if (result.isSuccess()) {
            AuthUtils.enableAllItems(((LoginActivity)context).findViewById(R.id.linearLayout));
            GoogleSignInAccount account = result.getSignInAccount();
            if(account!=null)
                authWithGoogle(account.getIdToken());
        } else{
            AuthUtils.enableAllItems(((LoginActivity)context).findViewById(R.id.linearLayout));
            client.stopAutoManage((FragmentActivity) activity);
            client.disconnect();
        }
    }
    public void authWithGoogle(String idToken){

        AGConnectAuthCredential credential = GoogleAuthProvider.credentialWithToken(idToken);
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // onSuccess
                        AGConnectUser user = signInResult.getUser();
                        new UserLoggedIn(user.getUid(), user.getDisplayName() ,user.getEmail());

                        Intent home = new Intent(context, HomeActivity.class);
                        context.startActivity(home);
                        AuthUtils.enableAllItems(((LoginActivity)context).findViewById(R.id.linearLayout));


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // onFail
                        AuthUtils.enableAllItems(((LoginActivity)context).findViewById(R.id.linearLayout));
                        Toast.makeText(context, e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

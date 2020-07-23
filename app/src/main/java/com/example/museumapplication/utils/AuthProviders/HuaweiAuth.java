package com.example.museumapplication.utils.AuthProviders;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.museumapplication.R;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthUtils;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.entity.hwid.HwIDConstant;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HuaweiAuth implements IBaseAuth {
    Context context;
    AGConnectAuth auth;
    HuaweiIdAuthService service;
    private static final int RC_SIGN_IN = 8888;

    public HuaweiAuth(Context context) {
        this.context = context;
        auth = AGConnectAuth.getInstance();

        HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
        List<Scope> scopeList = new ArrayList<>();
        scopeList.add(new Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE));
        scopeList.add(new Scope(HwIDConstant.SCOPE.SCOPE_ACCOUNT_EMAIL));
        huaweiIdAuthParamsHelper.setScopeList(scopeList);
        HuaweiIdAuthParams authParams = huaweiIdAuthParamsHelper.setAccessToken().setIdToken().createParams();

        service = HuaweiIdAuthManager.getService((LoginActivity) context, authParams);
    }

    @Override
    public void login() {
        Intent signIntent = service.getSignInIntent();
        ((LoginActivity) context).startActivityForResult(signIntent, RC_SIGN_IN);
    }


    @Override
    public AGConnectUser getCurrentUser() {
        return auth.getCurrentUser();
    }


    public void activityResult(Intent data) {
        Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
        if (authHuaweiIdTask.isSuccessful()) {
            AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
            Log.i("Huawei Login:", "accessToken:" + huaweiAccount.getAccessToken());
            authWithHuawei(huaweiAccount);

        } else {
            Log.e("Huawei ID Fail", "sign in failed : " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
        }
    }

    private void authWithHuawei(AuthHuaweiId huaweiAccount) {
        AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.getAccessToken());
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
            @Override
            public void onSuccess(SignInResult signInResult) {
                // onSuccess
                AGConnectUser user = signInResult.getUser();
                new UserLoggedIn(user.getUid(), user.getDisplayName() ,user.getEmail());

                Intent home = new Intent(context, HomeActivity.class);
                context.startActivity(home);
                AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("HuaweiID Fail:", Objects.requireNonNull(e.getMessage()));
                AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
            }
        });
    }
}

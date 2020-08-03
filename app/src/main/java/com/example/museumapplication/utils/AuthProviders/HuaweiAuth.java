package com.example.museumapplication.utils.AuthProviders;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.museumapplication.R;
import com.example.museumapplication.data.User;
import com.example.museumapplication.data.UserLoggedIn;
import com.example.museumapplication.ui.auth.LoginActivity;
import com.example.museumapplication.ui.home.HomeActivity;
import com.example.museumapplication.utils.AuthUtils;
import com.example.museumapplication.utils.CloudDBHelper;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
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
        HuaweiIdAuthParams authParams = huaweiIdAuthParamsHelper.
                setEmail().
                setAccessToken().
                setIdToken().
                createParams();

        service = HuaweiIdAuthManager.getService((LoginActivity) context, authParams);
    }

    @Override
    public void login() {
        Intent signIntent = service.getSignInIntent();
        ((LoginActivity) context).startActivityForResult(signIntent, RC_SIGN_IN);
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
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(signInResult -> {
            // onSuccess
            AGConnectUser agcUser = signInResult.getUser();


            if(CloudDBHelper.getInstance().checkFirstTimeUser(huaweiAccount.getEmail()))
            {
                User user = new User(agcUser.getUid(), huaweiAccount.getUid(), huaweiAccount.getEmail(),huaweiAccount.getGivenName()+ " "+ huaweiAccount.getFamilyName(), huaweiAccount.getAvatarUriString());
                CloudDBHelper.getInstance().insertUser(user);
                UserLoggedIn.getInstance().setUser(user.getUID(), user.getProviderUID() ,user.getEmail(), user.getDisplayName(), user.getPhotoURL());
            }
            else{
                try {
                    User user= CloudDBHelper.getInstance().queryByEmail(huaweiAccount.getEmail());
                    UserLoggedIn.getInstance().setUser(user.getUID(), user.getProviderUID() ,user.getEmail(), user.getDisplayName(), user.getPhotoURL());
                } catch (AGConnectCloudDBException e) {
                    e.printStackTrace();
                }
            }

            Intent home = new Intent(context, HomeActivity.class);
            context.startActivity(home);
            AuthUtils.enableAllItems(((LoginActivity) context).findViewById(R.id.linearLayout));
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

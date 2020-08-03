package com.example.museumapplication.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.museumapplication.data.ObjectTypeInfoHelper;
import com.example.museumapplication.data.User;
import com.example.museumapplication.data.UserLoggedIn;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.CloudDBZoneTask;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;

public class CloudDBHelper {
    //Singleton
    private static CloudDBHelper instance = new CloudDBHelper();

    AGConnectCloudDB mCloudDB;
    CloudDBZone mCloudDBZone;

    public CloudDBHelper(){

    }

    public static CloudDBHelper getInstance() {
        return instance;
    }

    public void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);

        mCloudDB =AGConnectCloudDB.getInstance();
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            e.printStackTrace();
        }
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig("MuseumApp",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        try {
            mCloudDBZone = mCloudDB.openCloudDBZone(mConfig, true);
        } catch (AGConnectCloudDBException e) {
            Log.w("Login Activity:", "openCloudDBZone: " + e.getMessage());
        }
    }

    public void insertUser(User user) {
        if (mCloudDBZone == null) {
            Log.d("CLOUD DB:","CloudDBZone is null, try re-open it");
            return;
        }
        CloudDBZoneTask<Integer> upsertTask = mCloudDBZone.executeUpsert(user);

        upsertTask.addOnSuccessListener(cloudDBZoneResult -> Log.d("CLOUD DB:", "insert " + cloudDBZoneResult + " records")).addOnFailureListener(e -> {
            Log.e("CLOUD DB:", "Insert user info failed");
            e.printStackTrace();
        });
    }

    public CloudDBZoneSnapshot<User> queryUser(CloudDBZoneQuery<User> query) {
        if (mCloudDBZone == null) {
            Log.w("CLOUD DB", "CloudDBZone is null, try re-open it");
            return null;
        }
        CloudDBZoneTask<CloudDBZoneSnapshot<User>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.await();
        if (queryTask.getException() != null) {
            Log.e("CLOUD DB:" ,"Query failed" );
            return null;
        }
        return queryTask.getResult();
    }
    public User queryByEmail(String email) throws AGConnectCloudDBException {
        CloudDBZoneQuery<User> query = CloudDBZoneQuery.where(User.class).equalTo("Email", email);
        CloudDBZoneSnapshot<User> result = queryUser(query);
        return result.getSnapshotObjects().get(0);
    }
    public boolean checkFirstTimeUser(String email){
        CloudDBZoneQuery<User> query = CloudDBZoneQuery.where(User.class).equalTo("Email", email);
        CloudDBZoneSnapshot<User> result = queryUser(query);
        return result.getSnapshotObjects().size() == 0;
    }
}

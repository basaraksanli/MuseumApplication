package com.example.museumapplication.ui.home.home;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.museumapplication.R;
import com.example.museumapplication.utils.BeaconUtils;
import com.example.museumapplication.utils.BluetoothCheckUtils;
import com.example.museumapplication.utils.GpsCheckUtils;
import com.example.museumapplication.utils.NetCheckUtils;
import com.example.museumapplication.utils.Permission.PermissionHelper;
import com.example.museumapplication.utils.Permission.PermissionInterface;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements PermissionInterface {

    private static final int REQUEST_CODE = 8488;
    Context mContext;
    private boolean isGetPermission = false;
    private PermissionHelper mPermissionHelper;
    private static final int THREAD_SLEEP_TIME = 500;

    private BroadcastReceiver stateChangeReceiver =
            new BroadcastReceiver() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("HomeFragment", "Start StatusMonitoring.onReceive");
                    String action = intent.getAction();
                    switch (action) {
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                            showWarnDialog("Bluetooth Warn");
                            break;
                        }
                        case BluetoothAdapter.ACTION_STATE_CHANGED: {
                            int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                            if (blueState == BluetoothAdapter.STATE_OFF) {
                                showWarnDialog("Bluetooth Warn");
                            }
                            break;
                        }
                        case ConnectivityManager.CONNECTIVITY_ACTION: {
                            operateConnectivityAction(context);
                            break;
                        }
                        case LocationManager.PROVIDERS_CHANGED_ACTION: {
                            Object object = context.getSystemService(Context.LOCATION_SERVICE);
                            if (!(object instanceof LocationManager)) {
                                showWarnDialog("Gps Warn");
                                return;
                            }
                            LocationManager locationManager = (LocationManager) object;
                            if (!locationManager.isLocationEnabled()) {
                                showWarnDialog("Gps Warn");
                            }
                            break;
                        }
                        case BluetoothDevice.ACTION_ACL_CONNECTED:
                        default: {
                            break;
                        }
                    }
                }
            };

    @Override
    public int getPermissionsRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    @Override
    public void requestPermissionsSuccess() {
        isGetPermission = true;
        Log.i("HomeFragment", "requestPermissionsSuccess");
    }

    @Override
    public void requestPermissionsFail() {
        //TODO onFail
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults);
    }



    @RequiresApi(api = Build.VERSION_CODES.P)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        boolean isSuccess = requestPermissions(this, this);
        if (!isSuccess) {
            return null;
        }
        Log.i("HomeFragment", "requestPermissions success");
        if (!NetCheckUtils.isNetworkAvailable(requireContext())) {
            showWarnDialog("NETWORK_ERROR");
            return null;
        }
        if (!BluetoothCheckUtils.isBlueEnabled()) {
            showWarnDialog("BLUETOOTH_ERROR");
            return null;
        }
        if (!GpsCheckUtils.isGpsEnabled(requireContext())) {
            showWarnDialog("GPS_ERROR");
            return null;
        }

        mContext = getContext();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            while (!isGetPermission) {
                try {
                    Thread.sleep(THREAD_SLEEP_TIME);
                } catch (InterruptedException e) {
                    Log.i("HomeFragment", "Thread sleep error", e);
                }
            }
            registerStatusReceiver();
            BeaconUtils.getInstance().startScanning(requireActivity() , root );
        });

        return root;

    }
    private boolean requestPermissions(Fragment fragment, PermissionInterface permissionInterface) {
        mPermissionHelper = new PermissionHelper(fragment, permissionInterface);
        mPermissionHelper.requestPermissions();
        return true;
    }
    private void registerStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        mContext.registerReceiver(stateChangeReceiver, intentFilter);
    }

    private void showWarnDialog(String content) {
        DialogInterface.OnClickListener onClickListener =
                (dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Warning");
        builder.setMessage(content);
        builder.setNegativeButton("Confirm", onClickListener);
        builder.show();
    }
    private void operateConnectivityAction(Context context) {
        Object object = context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!(object instanceof ConnectivityManager)) {
            showWarnDialog("Network Warn");
            return;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) object;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            showWarnDialog("Network Warn");
            return;
        }
        int networkType = activeNetworkInfo.getType();
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_WIFI: {
                break;
            }
            default: {
                showWarnDialog("Network Warn");
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BeaconUtils.getInstance().ungetMessageEngine();
    }
}
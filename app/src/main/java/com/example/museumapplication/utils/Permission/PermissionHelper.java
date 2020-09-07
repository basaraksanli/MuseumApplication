package com.example.museumapplication.utils.Permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Permission Helper
 *
 * @since 2019-12-13
 */
public class PermissionHelper {
    private Fragment mFragment;
    private PermissionInterface mPermissionInterface;

    /**
     * PermissionHelper Construct
     *
     * @param fragment Fragment
     * @param permissionInterface PermissionInterface
     */
    public PermissionHelper(@NonNull Fragment fragment, @NonNull PermissionInterface permissionInterface) {
        mFragment = fragment;
        mPermissionInterface = permissionInterface;
    }

    /**
     * Request Permissions
     */
    public void requestPermissions() {
        String[] deniedPermissions =
                PermissionUtil.getDeniedPermissions(mFragment.getContext(), mPermissionInterface.getPermissions());
        if (deniedPermissions.length > 0) {
            PermissionUtil.requestPermissions(
                    mFragment, deniedPermissions, mPermissionInterface.getPermissionsRequestCode());
        } else {
            mPermissionInterface.requestPermissionsSuccess();
        }
    }

    /**
     * Request Permissions Result
     *
     * @param requestCode requestCode
     * @param permissions permissions
     * @param grantResults grantResults
     * @return true:Request Permissions success
     */
    public boolean requestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == mPermissionInterface.getPermissionsRequestCode()) {
            boolean isAllGranted = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                mPermissionInterface.requestPermissionsSuccess();
            } else {
                mPermissionInterface.requestPermissionsFail();
            }
            return true;
        }
        return false;
    }
}
package com.example.museumapplication.utils.Permission;

public interface PermissionInterface {
    /**
     * Get Permissions Request Code
     *
     * @return PermissionsRequestCode
     */
    int getPermissionsRequestCode();

    /**
     * Get Permissions
     *
     * @return Permissions
     */
    String[] getPermissions();

    /**
     * Request Permissions Success
     */
    void requestPermissionsSuccess();

    /**
     * Request Permissions Fail
     */
    void requestPermissionsFail();
}
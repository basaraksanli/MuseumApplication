package com.example.museumapplication.utils.permission

interface PermissionInterface {
    /**
     * Get Permissions Request Code
     *
     * @return PermissionsRequestCode
     */
    fun permissionsRequestCode(): Int

    /**
     * Get Permissions
     *
     * @return Permissions
     */
    fun permissions(): Array<String?>?

    /**
     * Request Permissions Success
     */
    fun requestPermissionsSuccess()

    /**
     * Request Permissions Fail
     */
    fun requestPermissionsFail()
}
package com.example.museumapplication.utils.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import java.util.*

/**
 * Permission Util
 *
 * @since 2019-12-13
 */
object PermissionUtil {
    /**
     * If has permission
     *
     * @param context Context
     * @param permission permission
     * @return true:has permission
     */
    fun hasPermission(context: Context, permission: String?): Boolean {
        if (context.checkSelfPermission(permission!!) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    /**
     * Request Permissions
     *
     * @param fragment fragment
     * @param permissions permissions
     * @param requestCode requestCode
     */
    @JvmStatic
    fun requestPermissions(fragment: Fragment, permissions: Array<String?>?, requestCode: Int) {
        fragment.requestPermissions(permissions!!, requestCode)
    }

    /**
     * Get Denied Permissions
     *
     * @param context Context
     * @param permissions permissions
     * @return Denied Permissions
     */
    @JvmStatic
    fun getDeniedPermissions(context: Context, permissions: Array<String?>): Array<String?> {
        val deniedPermissionList = ArrayList<String?>()
        for (permission in permissions) {
            if (context.checkSelfPermission(permission!!) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permission)
            }
        }
        val size = deniedPermissionList.size
        if (size > 0) {
            return deniedPermissionList.toTypedArray()
        }
        return arrayOfNulls(0)
    }
}
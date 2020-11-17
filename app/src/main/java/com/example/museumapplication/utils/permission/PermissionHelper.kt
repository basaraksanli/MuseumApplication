package com.example.museumapplication.utils.permission

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import com.example.museumapplication.utils.permission.PermissionUtil.getDeniedPermissions
import com.example.museumapplication.utils.permission.PermissionUtil.requestPermissions

/**
 * Permission Helper
 *
 * @since 2019-12-13
 */
class PermissionHelper
/**
 * PermissionHelper Construct
 *
 * @param fragment Fragment
 * @param permissionInterface PermissionInterface
 */(private val mFragment: Fragment, private val mPermissionInterface: PermissionInterface) {
    /**
     * Request Permissions
     */
    fun requestPermissions() {
        val deniedPermissions = getDeniedPermissions(mFragment.requireContext(), mPermissionInterface.permissions()!!)
        if (deniedPermissions.isNotEmpty()) {
            requestPermissions(
                    mFragment, deniedPermissions, mPermissionInterface.permissionsRequestCode())
        } else {
            mPermissionInterface.requestPermissionsSuccess()
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
    fun requestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        if (requestCode == mPermissionInterface.permissionsRequestCode()) {
            var isAllGranted = true
            for (result in grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false
                    break
                }
            }
            if (isAllGranted) {
                mPermissionInterface.requestPermissionsSuccess()
            } else {
                mPermissionInterface.requestPermissionsFail()
            }
            return true
        }
        return false
    }
}
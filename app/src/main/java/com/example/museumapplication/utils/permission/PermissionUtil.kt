/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
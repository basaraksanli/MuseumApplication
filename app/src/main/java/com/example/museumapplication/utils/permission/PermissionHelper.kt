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

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import com.example.museumapplication.utils.permission.PermissionUtil.getDeniedPermissions
import com.example.museumapplication.utils.permission.PermissionUtil.requestPermissions


class PermissionHelper

(private val mFragment: Fragment, private val mPermissionInterface: PermissionInterface) {

    fun requestPermissions() {
        val deniedPermissions = getDeniedPermissions(mFragment.requireContext(), mPermissionInterface.permissions()!!)
        if (deniedPermissions.isNotEmpty()) {
            requestPermissions(
                    mFragment, deniedPermissions, mPermissionInterface.permissionsRequestCode())
        } else {
            mPermissionInterface.requestPermissionsSuccess()
        }
    }


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
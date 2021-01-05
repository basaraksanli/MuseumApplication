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
package com.example.museumapplication.utils

import android.content.Context
import android.content.DialogInterface
import android.os.Process
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData

class GeneralUtils {

    companion object{
        /**
         * Warning Dialog Builder for the issues
         */
        fun showWarnDialog(content: String , context: Context, navigation : MutableLiveData<Boolean>?) {
            DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> Process.killProcess(Process.myPid()) }
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Warning")
            builder.setMessage(content)
            builder.setNegativeButton("Confirm") { _: DialogInterface, _: Int ->
                navigation?.postValue(true)
            }
            builder.show()
        }
    }
}
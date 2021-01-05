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
package com.example.museumapplication.utils.settings

import android.app.Activity
import androidx.preference.PreferenceManager
import com.example.museumapplication.R

class SettingsUtils {
    companion object{

        /**
         * Choose theme between dark mode and light mode
         */
        fun setTheme(activity: Activity){
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)

            val darkMode = sp.getBoolean(activity.applicationContext.getString(R.string.darkModePreferences), false)
            if(darkMode)
                activity.setTheme(R.style.AppThemeDarkNoActionBar)
            else
                activity.setTheme(R.style.AppThemeNoActionBar)
        }

        /**
         * choose map style between night and day
         */
        fun mapStyleDark (activity: Activity): Int {
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)
            return if(sp.getBoolean(activity.applicationContext.getString(R.string.mapDarkPreferences), false))
                R.raw.mapstyle_dark
            else
                R.raw.mapstyle
        }
    }
}
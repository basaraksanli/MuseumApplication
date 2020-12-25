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
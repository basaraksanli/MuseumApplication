package com.example.museumapplication.ui.home.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.museumapplication.R


class SettingsFragment : PreferenceFragmentCompat() , SharedPreferences.OnSharedPreferenceChangeListener{

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        if (key.equals("darkMode")) {
            // Set summary to be the user-description for the selected value

            val darkMode = sharedPreferences!!.getBoolean("darkMode", true)
            if(darkMode)
                activity?.setTheme(R.style.AppThemeDarkNoActionBar)
            else
                activity?.setTheme(R.style.AppThemeNoActionBar)
            activity?.recreate()
        }
    }
    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}
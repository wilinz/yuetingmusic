package com.wilinz.yuetingmusic.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.util.IntentUtil.browse

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        try {
            val versionName = requireContext().packageManager.getPackageInfo(
                requireContext().packageName,
                0
            ).versionName
            preferenceManager.findPreference<Preference>("app_version")!!.summary = versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.fitsSystemWindows = true
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val key = preference.key
        if (key == "app_author") {
            browse(requireContext(), preference.summary.toString() + "")
        } else if (key == "source_code") {
            browse(requireContext(), preference.summary.toString() + "")
        }
        return super.onPreferenceTreeClick(preference)
    }
}
package com.wilinz.yuetingmusic.ui.settings;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.util.IntentUtil;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        try {
            String versionName = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
            getPreferenceManager().findPreference("app_version").setSummary(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setFitsSystemWindows(true);

    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        String key = preference.getKey();
        if (key.equals("app_author")) {
            IntentUtil.browse(requireContext(), preference.getSummary() + "");
        } else if (key.equals("source_code")) {
            IntentUtil.browse(requireContext(), preference.getSummary() + "");
        }
        return super.onPreferenceTreeClick(preference);
    }


}
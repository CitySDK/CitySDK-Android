package com.citysdk.demo.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.citysdk.demo.R;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Preference connectionPref = findPreference(SettingsActivity.PREF_MENU_EVENTS_DAYS);
        connectionPref.setSummary(sharedPreferences.getString(SettingsActivity.PREF_MENU_EVENTS_DAYS, "7"));
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.PREF_MENU_EVENTS_DAYS)) {
            Preference connectionPref = findPreference(key);
            connectionPref.setSummary(sharedPreferences.getString(key, "7"));
        }
    }
}

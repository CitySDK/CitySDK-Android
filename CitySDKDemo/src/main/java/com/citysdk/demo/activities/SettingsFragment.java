package com.citysdk.demo.activities;

import com.citysdk.demo.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        Preference connectionPref = findPreference(SettingsActivity.PREF_MENU_EVENTS_DAYS);
        connectionPref.setSummary(
                sharedPreferences.getString(SettingsActivity.PREF_MENU_EVENTS_DAYS, "7"));

        Preference radiusPref = findPreference(SettingsActivity.PREF_SEARCH_RADIUS);
        String radius = sharedPreferences.getString(SettingsActivity.PREF_SEARCH_RADIUS, "10000");
        radiusPref.setSummary(radius + "m");

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
        } else if (key.equals(SettingsActivity.PREF_SEARCH_RADIUS)) {
            Preference connectionPref = findPreference(key);
            String radius = sharedPreferences.getString(key, "10000");
            connectionPref.setSummary(radius + "m");
        }

    }
}

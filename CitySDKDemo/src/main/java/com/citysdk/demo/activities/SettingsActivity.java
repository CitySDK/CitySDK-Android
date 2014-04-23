package com.citysdk.demo.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

    public static final String PREF_MENU_EVENTS_DAYS = "prefs_menu_events_number_days";
    public static final String PREF_SEARCH_RADIUS = "prefs_menu_search_radius";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }
}

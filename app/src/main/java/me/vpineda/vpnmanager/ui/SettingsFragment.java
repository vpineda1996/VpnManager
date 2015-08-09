package me.vpineda.vpnmanager.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import me.vpineda.vpnmanager.R;

/**
 * Thanks to Google most of the code has been written for me so I'll just use it to store and
 * ask for info to users :D
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

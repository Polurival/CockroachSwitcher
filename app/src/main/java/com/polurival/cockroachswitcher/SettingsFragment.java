package com.polurival.cockroachswitcher;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsFragment extends PreferenceFragmentCompat
        implements OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static final Pattern IP_ADDRESS = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");
    private static final int MAX_PORT = 65535;
    private static final int MIN_PORT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);

        PreferenceScreen prefScreen = getPreferenceScreen();

        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            Preference preference = prefScreen.getPreference(i);
            String value = prefScreen.getSharedPreferences().getString(preference.getKey(), "");
            preference.setSummary(value);

            preference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        String value = sharedPreferences.getString(preference.getKey(), "");
        preference.setSummary(value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String ipKey = getString(R.string.pref_ip_key);
        String portKey = getString(R.string.pref_port_key);

        if (preference.getKey().equals(ipKey)) {

            Matcher matcher = IP_ADDRESS.matcher((String) newValue);
            if (!matcher.matches()) {
                showError(R.string.ip_error);
                return false;
            }

        } else if (preference.getKey().equals(portKey)) {

            try {
                int port = Integer.parseInt((String) newValue);
                if (port < MIN_PORT || port > MAX_PORT) {
                    showError(R.string.port_error);
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(R.string.port_error);
                return false;
            }
        }

        return true;
    }

    private void showError(int resId) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(resId), Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}

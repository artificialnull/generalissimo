package com.gabdeg.generalissimo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference logoutButton = findPreference("logOutButton");
        logoutButton.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.remove("dippyPasswd");
                        editor.putString("dippyUsernm", "LogOut");
                        editor.apply();
                        Toast.makeText(getActivity().getApplicationContext(), "Logged out of webDiplomacy",
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                        return true;
                    }
                }
        );

        Preference updateInterval = findPreference("notifCheckInterval");
        updateInterval.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);
                        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                                Integer.parseInt(
                                        (String) newValue
                                ),
                                pendingIntent
                        );

                        return true;
                    }
                }
        );

    }
}

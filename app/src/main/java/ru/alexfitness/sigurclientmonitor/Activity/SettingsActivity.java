package ru.alexfitness.sigurclientmonitor.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Set;

import ru.alexfitness.sigurclientmonitor.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);
        addPreferencesFromResource(R.xml.pref_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> keySet = preferences.getAll().keySet();
        for(String key : keySet) {
            setPreferenceSummary(preferences, key);
        }
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setPreferenceSummary(sharedPreferences, key);
    }

    public void setPreferenceSummary(SharedPreferences sharedPreferences, String key){
        Preference preference = findPreference(key);
        if(preference!=null) {
            switch (key) {
                case "direction_pref":
                    break;
                case "enable_voice_pref":
                    break;
                default:
                    preference.setSummary(sharedPreferences.getString(key, ""));
                    break;
            }
        }
    }
}


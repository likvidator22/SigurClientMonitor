package ru.alexfitness.sigurclientmonitor.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.alexfitness.sigurclientmonitor.R;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurEventType;


public class SigurMonitorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);
        setDefaultMessages();
    }

    private void setDefaultMessages(){
        DefaultMessagesManager.setDefaultMessages(this);
    }
}

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
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        SigurEventType[] types = SigurEventType.values();
//
//        for(SigurEventType type:types){
//            if(!sharedPreferences.contains(type.name())){
//                String defaultMessage = DefaultMessagesManager.getDefaultSigurEventMessageText(this, type);
//                if(defaultMessage!=null){
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(type.name(),defaultMessage);
//                    editor.commit();
//                }
//            }
//        }
//
//        if(!sharedPreferences.contains(getString(R.string.waiting_message_pref))){
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString(getString(R.string.waiting_message_pref), getString(R.string.waiting_text));
//            editor.commit();
//        }
        DefaultMessagesManager.setDefaultMessages(this);
    }
}

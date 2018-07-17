package ru.alexfitness.sigurclientmonitor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.alexfitness.sigurclientmonitor.Db.SigurMonitorDAO;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;

public class MessageBuilder {

    private Context context;
    private SharedPreferences preferences;

    public MessageBuilder(Context context){
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getMessageFromPreferences(String preferenceName){
        return preferences.getString(preferenceName, "");
    }

    public String buildSigurEventMessage(SigurEvent sigurEvent){
        String result = null;
        if(sigurEvent.getEventType()!=null) {
            switch (sigurEvent.getEventType()) {
                case SUCCESS_ENTER:
                    result = getMessageFromPreferences(sigurEvent.getEventType().name());
                    String name = SigurMonitorDAO.getName(context, sigurEvent.getObjectID());
                    if (name == null) {
                        name = "";
                    }
                    result = result.replaceAll("\\[name\\]", name);
                    break;
                default:
                    result = getMessageFromPreferences(sigurEvent.getEventType().name());
                    break;
            }
        }
        return result;
    }



}
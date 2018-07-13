package ru.alexfitness.sigurclientmonitor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.alexfitness.sigurclientmonitor.R;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurEventType;

public class DefaultMessagesManager {

    private DefaultMessagesManager(){

    }

    public static String getDefaultSigurEventMessageText(Context context, SigurEventType eventType){
        String result = null;
        switch (eventType){
            case SUCCESS_ENTER:
                result = context.getString(R.string.greeting_text);
                break;
            case FAIL_FACE_SCAN:
                result = context.getString(R.string.recognition_problem_text);
                break;
            case FAIL_WRONG_CODE:
                result = context.getString(R.string.wrong_code_problem_text);
                break;
            case FAIL_EXPIRED:
                result = context.getString(R.string.expired_problem_text);
                break;
            case FAIL_TIME_LIMIT:
                result = context.getString(R.string.time_limit_problem_text);
                break;
            default:
                result = eventType.getDescription();
                break;
        }
        return result;
    }

    public static final void setDefaultMessages(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SigurEventType[] types = SigurEventType.values();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(SigurEventType type:types){
            if(!sharedPreferences.contains(type.name())){
                String defaultMessage = getDefaultSigurEventMessageText(context, type);
                if(defaultMessage!=null){
                    editor.putString(type.name(),defaultMessage);
                    editor.commit();
                }
            }
        }
        if(!sharedPreferences.contains(context.getString(R.string.waiting_message_pref))) {
            editor.putString(context.getString(R.string.waiting_message_pref), context.getString(R.string.waiting_text));
            editor.commit();
        }
    }
}

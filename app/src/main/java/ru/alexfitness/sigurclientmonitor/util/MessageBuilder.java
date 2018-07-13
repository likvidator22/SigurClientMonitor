package ru.alexfitness.sigurclientmonitor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import ru.alexfitness.sigurclientmonitor.Db.SigurDbHelper;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;

public class MessageBuilder {

    private Context context;
    private SharedPreferences preferences;

    public MessageBuilder(Context context){
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String buildMessage(String preferenceName){
        return preferences.getString(preferenceName, "");
    }

    public String buildMessage(int resId){
        return context.getString(resId);
    }

    public String buildSigurEventMessage(SigurEvent sigurEvent){
        String result = null;
        if(sigurEvent.getEventType()!=null) {
            switch (sigurEvent.getEventType()) {
                case SUCCESS_ENTER:
                    result = buildMessage(sigurEvent.getEventType().name());
                    String name = getName(sigurEvent.getObjectID());
                    if (name == null) {
                        name = "";
                    }
                    result = result.replaceAll("\\[name\\]", name);
                    break;
                default:
                    result = buildMessage(sigurEvent.getEventType().name());
                    break;
            }
        }
        return result;
    }

    @Nullable
    private String getName(int id){
        try {
            SigurDbHelper dbHelper = new SigurDbHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT NAME, FATHERNAME FROM VISITORS WHERE SIGUR_ID = ?", new String[]{String.valueOf(id)});
            if (cursor.moveToNext()) {
                StringBuilder sb = new StringBuilder();
                sb.append(cursor.getString(0)).append(" ").append(cursor.getString(1));
                return sb.toString();
            }
            cursor.close();
            db.close();
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
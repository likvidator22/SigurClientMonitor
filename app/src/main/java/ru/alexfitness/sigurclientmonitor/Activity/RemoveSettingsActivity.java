package ru.alexfitness.sigurclientmonitor.Activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import ru.alexfitness.sigurclientmonitor.Db.SigurDbHelper;
import ru.alexfitness.sigurclientmonitor.R;
import ru.alexfitness.sigurclientmonitor.util.DefaultMessagesManager;

public class RemoveSettingsActivity extends Activity {

    private class ClearDbTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            SigurDbHelper dbHelper = new SigurDbHelper(RemoveSettingsActivity.this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            db.execSQL("DELETE FROM VISITORS");
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            RemoveSettingsActivity.this.finishAndClose();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_settings);

        //clear preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);

        DefaultMessagesManager.setDefaultMessages(this);

        //clear db
        new ClearDbTask().execute();
    }

    private void finishAndClose(){
        setResult(MainActivity.REMOVE_SETTINGS_RESULT_OK);
        finish();
    }

}

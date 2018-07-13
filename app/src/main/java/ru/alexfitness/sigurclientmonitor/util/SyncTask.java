package ru.alexfitness.sigurclientmonitor.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ru.alexfitness.sigurclientmonitor.Db.SigurDbHelper;

public class SyncTask extends AsyncTask<Void, Integer, Boolean> {

    private SharedPreferences preferences;
    private Context context;
    private SyncTaskListener listener;

    public SyncTask(SharedPreferences prefs, Context context){
        this.preferences = prefs;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        if(listener!=null){
            listener.onSyncStart();
        }
    }

    @Override
    protected Boolean doInBackground(Void... objects) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            SigurDbHelper dbHelper = new SigurDbHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("jdbc:mysql://");
            stringBuilder.append(preferences.getString("host_pref", "")).append(":3305").append("/");
            stringBuilder.append(preferences.getString("db_name_pref", ""));

            DriverManager.setLoginTimeout(60);
            Connection mysqlconnection = DriverManager.getConnection(stringBuilder.toString(), "root", "");
            Statement stmt = mysqlconnection.createStatement();
            ResultSet rs;
            int syncSize;
            int syncProgress = 0;

            rs = stmt.executeQuery("SELECT COUNT(id) FROM personal");
            rs.next();
            syncSize = rs.getInt(1);
            rs.close();

            String name;
            int object_id;
            Cursor cursor= null;

            rs = stmt.executeQuery("SELECT id, name FROM personal");

            while(rs.next()){
                if(isCancelled()){
                    return false;
                }
                object_id = rs.getInt(1);
                name = rs.getString(2);
                cursor = db.rawQuery("SELECT SIGUR_ID, NAME, SURNAME, FATHERNAME FROM VISITORS WHERE SIGUR_ID = ?", new String[]{String.valueOf(object_id)});
                if(cursor.moveToNext()){
                    //check name?
                } else {
                    String[] nameSplit = name.split(" ");
                    if(nameSplit.length == 3) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("NAME", nameSplit[1]);
                        contentValues.put("SURNAME", nameSplit[0]);
                        contentValues.put("FATHERNAME", nameSplit[2]);
                        contentValues.put("SIGUR_ID", object_id);
                        db.insert("VISITORS", null, contentValues);
                    }
                }
                cursor.close();
                syncProgress++;
                publishProgress((syncProgress  * 100)/syncSize);
            }

            if(cursor!=null){
                cursor.close();
            }
            rs.close();
            stmt.close();
            mysqlconnection.close();
            db.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean res) {
        if(res){
            if(listener!=null){
                listener.onFinishAction();
            }
        } else {
            if(listener!=null){
                listener.onSyncFailed();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(listener!=null){
            listener.handleProgress(values[0]);
        }
    }

    public SyncTaskListener getListener() {
        return listener;
    }

    public void setListener(SyncTaskListener listener) {
        this.listener = listener;
    }
}

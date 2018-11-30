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
import java.util.Properties;

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

        SQLiteDatabase db = null;
        Connection mysqlconnection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Cursor cursor = null;

        try {
            SigurDbHelper dbHelper = new SigurDbHelper(context);
            db = dbHelper.getReadableDatabase();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("jdbc:mysql://");
            stringBuilder.append(preferences.getString("host_pref", "")).append(":3305").append("/");
            stringBuilder.append(preferences.getString("db_name_pref", ""));

            DriverManager.setLoginTimeout(60);
            Properties connectionProperties = new Properties();
            connectionProperties.put("connectTimeout", "10000");
            connectionProperties.put("user", "root");
            connectionProperties.put("password", "");
            mysqlconnection = DriverManager.getConnection(stringBuilder.toString(), connectionProperties);
            stmt = mysqlconnection.createStatement();

            int syncSize;
            int syncProgress = 0;

            rs = stmt.executeQuery("SELECT COUNT(id) FROM personal");
            rs.next();
            syncSize = rs.getInt(1);
            rs.close();

            String name, tabid;
            int objectid;

            rs = stmt.executeQuery("SELECT id, name, TABID FROM personal");

            while(rs.next()){
                if(isCancelled()){
                    return false;
                }
                objectid = rs.getInt(1);
                name = rs.getString(2);
                tabid = rs.getString(3);
                cursor = db.rawQuery("SELECT SIGUR_ID, NAME, SURNAME, FATHERNAME, TABID FROM VISITORS WHERE SIGUR_ID = ?", new String[]{String.valueOf(objectid)});
                if(cursor.moveToNext()){
                    //check name?
                } else {
                    String[] nameSplit = name.split(" ");
                    if(nameSplit.length == 3) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("NAME", nameSplit[1]);
                        contentValues.put("SURNAME", nameSplit[0]);
                        contentValues.put("FATHERNAME", nameSplit[2]);
                        contentValues.put("SIGUR_ID", objectid);
                        contentValues.put("TABID", tabid);
                        db.insert("VISITORS", null, contentValues);
                    }
                }
                cursor.close();
                syncProgress++;
                publishProgress((syncProgress  * 100)/syncSize);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor!=null){
                cursor.close();
            }
            if(rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(stmt!=null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(mysqlconnection!=null){
                try {
                    mysqlconnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(db!=null) {
                db.close();
            }
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

    public void setListener(SyncTaskListener listener) {
        this.listener = listener;
    }
}

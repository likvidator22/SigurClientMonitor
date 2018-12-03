package ru.alexfitness.sigurclientmonitor.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

            rs = stmt.executeQuery("SELECT COUNT(id) FROM personal WHERE STATUS = 'AVAILABLE'");
            rs.next();
            syncSize = rs.getInt(1);
            rs.close();

            String name, tabid;
            int objectid;
            long ts, photots;
            Blob photoBlob;

            ArrayList<Integer> objectsToLoadPhoto = new ArrayList<>();

            /*rs = stmt.executeQuery("SELECT PS.ID, PS.NAME, PS.TABID, PH.TS AS TS, PH.PREVIEW_RASTER " +
                    "FROM personal AS PS " +
                    "       LEFT JOIN photo AS PH " +
                    "ON PS.id = PH.id " +
                    "WHERE STATUS = 'AVAILABLE'");*/
            rs = stmt.executeQuery("SELECT PS.ID, PS.NAME, PS.TABID, PH.TS AS TS " +
                    "FROM personal AS PS " +
                    "       LEFT JOIN photo AS PH " +
                    "ON PS.id = PH.id " +
                    "WHERE STATUS = 'AVAILABLE'");

            db.beginTransaction();

            while(rs.next()){
                if(isCancelled()){
                    return false;
                }
                objectid = rs.getInt(1);
                name = rs.getString(2);
                tabid = rs.getString(3);
                ts = rs.getLong(4);

                cursor = db.rawQuery("SELECT SIGUR_ID, NAME, SURNAME, FATHERNAME, TABID, PHOTO_TS FROM VISITORS WHERE SIGUR_ID = ?", new String[]{String.valueOf(objectid)});
                if(cursor.moveToNext()){
                    photots = cursor.getLong(cursor.getColumnIndex("PHOTO_TS"));
                    if(photots != ts){
                        /*Blob photoBlob = rs.getBlob(5);
                        if(photoBlob!=null) {
                            savePhoto(photoBlob, objectid);
                        }*/
                        objectsToLoadPhoto.add(objectid);
                        db.execSQL("UPDATE VISITORS SET PHOTO_TS = " + String.valueOf(ts) + " WHERE SIGUR_ID = " + String.valueOf(objectid));
                    }
                } else {
                    String[] nameSplit = name.split(" ");
                    if(nameSplit.length == 3) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("NAME", nameSplit[1]);
                        contentValues.put("SURNAME", nameSplit[0]);
                        contentValues.put("FATHERNAME", nameSplit[2]);
                        contentValues.put("SIGUR_ID", objectid);
                        contentValues.put("TABID", tabid);
                        contentValues.put("PHOTO_TS", ts);
                        db.insert("VISITORS", null, contentValues);
                        /*Blob photoBlob = rs.getBlob(5);
                        if(photoBlob!=null) {
                            savePhoto(photoBlob, objectid);
                        }*/
                        objectsToLoadPhoto.add(objectid);
                    }
                }
                cursor.close();
                syncProgress++;
                publishProgress((syncProgress  * 100)/syncSize);
            }

            // LOADING PHOTO
            syncSize = objectsToLoadPhoto.size();
            if(syncSize > 0) {
                syncProgress = 0;
                publishProgress((syncProgress * 100) / syncSize);

                rs = stmt.executeQuery("SELECT ID, PREVIEW_RASTER " +
                        "FROM photo " +
                        "WHERE ID in (" + TextUtils.join(",", objectsToLoadPhoto) + ")");
                while (rs.next()) {
                    if (isCancelled()) {
                        return false;
                    }
                    objectid = rs.getInt(1);
                    photoBlob = rs.getBlob(2);

                    if (photoBlob != null) {
                        savePhoto(photoBlob, objectid);
                    }

                    syncProgress++;
                    publishProgress((syncProgress * 100) / syncSize);
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(db!=null && db.inTransaction()){
                db.endTransaction();
            }
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

    private void savePhoto(@NonNull Blob photoBlob, int objectid) throws IOException, SQLException {
        //Blob photoBlob = rs.getBlob(5);
        byte[] photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
        String fileName = String.format("%d", objectid);
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        fos.write(photoBytes);
        fos.close();
    }

}

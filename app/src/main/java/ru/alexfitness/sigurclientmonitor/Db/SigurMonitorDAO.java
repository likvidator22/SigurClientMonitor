package ru.alexfitness.sigurclientmonitor.Db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;

public class SigurMonitorDAO {

    public static String getName(Context context, int id){
        String result = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            SigurDbHelper dbHelper = new SigurDbHelper(context);
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT NAME, FATHERNAME FROM VISITORS WHERE SIGUR_ID = ?", new String[]{String.valueOf(id)});
            if (cursor.moveToNext()) {
                result = cursor.getString(0) + " " + cursor.getString(1);
            }
        } catch(Exception ex){
            ex.printStackTrace();
        } finally {
            if(cursor!=null) {
                cursor.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return result;
    }

    public static Bitmap getPhotoData(Context context, int id) throws FileNotFoundException {
        FileInputStream fis = context.openFileInput(String.format(Locale.getDefault(),"%d", id));
        return BitmapFactory.decodeStream(fis);
    }

}

package ru.alexfitness.sigurclientmonitor.Db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SigurDbHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "sigur_monitor_db";
    private static final int DB_VERSION = 1;

    private static final String CREATE_TABLE_VISITORS_STATEMENT = "CREATE TABLE VISITORS (_ID INTEGER PRIMARY KEY AUTOINCREMENT, SIGUR_ID INTEGER, NAME TEXT, SURNAME TEXT, FATHERNAME TEXT)";

    public SigurDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createTables(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_VISITORS_STATEMENT);
    }
}

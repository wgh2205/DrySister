package com.kok.drysister;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SisterOpenHelp extends SQLiteOpenHelper {

    private static final String DB_NAME = "sister.db";
    private static final int DB_VERSION = 1;

    public SisterOpenHelp(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + TableDefine.TABLE_FULI + " ("
                + TableDefine.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TableDefine.COLUMN_FULI_ID + " TEXT,"
                + TableDefine.COLUMN_FULI_CREATEAT + " TEXT,"
                + TableDefine.COLUMN_FULI_DESC + " TEXt,"
                + TableDefine.COLUMN_FULI_PUBLISHEDAT + " TEXT,"
                + TableDefine.COLUMN_FULI_SOURCE + " TEXT,"
                + TableDefine.COLUMN_FULI_TYPE + " TEXT,"
                + TableDefine.COLUMN_FULI_URL + " TEXT,"
                + TableDefine.COLUMN_FULI_USED + " TEXT,"
                + TableDefine.COLUMN_FULI_WHO + " TEXT"
                + ")";
        db.execSQL(createTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

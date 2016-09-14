package com.liyeyu.tasktest.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Liyeyu on 2016/9/12.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "book_provider.db";
    public static final int DB_VERSION = 1;
    public static final String BOOK_TABLE_NAME = "book";
    public static final String USER_TABLE_NAME = "user";

    private String CREATE_BOOK_TABLE = "CREATE TABLE IF NOT EXISTS "+
            BOOK_TABLE_NAME+"(_id INTEGER PRIMARY KEY,"+" name TEXT)";
    private String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS "+
            USER_TABLE_NAME+"(_id INTEGER PRIMARY KEY,"+" name TEXT)";

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_TABLE);
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL("insert into book values(1,'Android')");
        db.execSQL("insert into book values(2,'IOS')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

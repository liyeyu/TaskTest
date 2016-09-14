package com.liyeyu.tasktest;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.liyeyu.tasktest.helper.DBOpenHelper;

/**
 * Created by Liyeyu on 2016/9/12.
 */
public class BookProvider extends ContentProvider {

    public static final String SCHEMA = "content://";
    //在manifest.xml中声明的唯一标识
    public static final String AUTHORITY = "com.liyeyu.tasktest.book.provider";

    public static final String BOOK_CONTENT_URI = SCHEMA + AUTHORITY + "/book";
    public static final String USER_CONTENT_URI = SCHEMA + AUTHORITY + "/user";

    public static final int BOOK_URI_CODE = 0;
    public static final int USER_URI_CODE = 1;

    private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //将自定义的uri添加到匹配器中
        mMatcher.addURI(AUTHORITY,"book",BOOK_URI_CODE);
        mMatcher.addURI(AUTHORITY,"user",USER_URI_CODE);
    }

    private Context mContext;
    private DBOpenHelper mHelper;
    private SQLiteDatabase mDb;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        initProviderData();
        return true;
    }

    private void initProviderData() {
        mHelper = new DBOpenHelper(mContext);
        mDb = mHelper.getReadableDatabase();
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        Log.e("BookProvider","query thread:"+Thread.currentThread().getName());
        String tableName = getTableName(uri);
        if(tableName==null){
            throw new IllegalArgumentException("Unsupported Uri: "+uri);
        }
        return mDb.query(tableName,strings,s,strings1,null,null,s1);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String tableName = getTableName(uri);
        if(tableName==null){
            throw new IllegalArgumentException("Unsupported Uri: "+uri);
        }
        mDb.insert(tableName,null,contentValues);
        mContext.getContentResolver().notifyChange(uri,null);
        return uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        String tableName = getTableName(uri);
        if(tableName==null){
            throw new IllegalArgumentException("Unsupported Uri: "+uri);
        }
        int count = mDb.delete(tableName, s, strings);
        if(count>0){
            mContext.getContentResolver().notifyChange(uri,null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        String tableName = getTableName(uri);
        if(tableName==null){
            throw new IllegalArgumentException("Unsupported Uri: "+uri);
        }
        int count = mDb.update(tableName, contentValues,s, strings);
        if(count>0){
            mContext.getContentResolver().notifyChange(uri,null);
        }
        return count;
    }

    public String getTableName(Uri uri){
        String tableName = null;
        switch (mMatcher.match(uri)){
            case BOOK_URI_CODE:
                tableName = DBOpenHelper.BOOK_TABLE_NAME;
                break;
            case USER_URI_CODE:
                tableName = DBOpenHelper.USER_TABLE_NAME;
                break;
        }
        return tableName;
    }
}

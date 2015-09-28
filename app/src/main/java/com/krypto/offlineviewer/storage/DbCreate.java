package com.krypto.offlineviewer.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbCreate extends SQLiteOpenHelper {

    public DbCreate(Context context ) {
        super(context, "offline.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_ARTICLE_TABLE =   "CREATE TABLE " + DataContract.ArticlesEntry.TABLE + "("
                +DataContract.ArticlesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +DataContract.ArticlesEntry.TITLE+" TEXT, "
                +DataContract.ArticlesEntry.URL+ " TEXT NOT NULL, "
                +DataContract.ArticlesEntry.DESC+ " TEXT NOT NULL, "
                +DataContract.ArticlesEntry.IMAGE+ " TEXT, "
                +" UNIQUE (" + DataContract.ArticlesEntry.TITLE + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_ARTICLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        onCreate(db);
    }
}

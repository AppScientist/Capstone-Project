package com.krypto.offlineviewer.storage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;


public class DataProvider extends ContentProvider {


    private static final int ARTICLES = 100;
    private static final UriMatcher sUriMatcher = uriMatcher();
    private DbCreate mCreator;

    private static UriMatcher uriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = DataContract.AUTHORITY;

        matcher.addURI(authority, DataContract.ARTICLES, ARTICLES);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mCreator = new DbCreate(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case ARTICLES: {

                cursor = mCreator.getReadableDatabase().query(
                        DataContract.ArticlesEntry.TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        switch (sUriMatcher.match(uri)) {

            case ARTICLES: {

                return DataContract.ArticlesEntry.CONTENT_TYPE;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mCreator.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri insertedUri;
        switch (match) {

            case ARTICLES: {
                long _id = db.insert(DataContract.ArticlesEntry.TABLE, null, values);
                if (_id > 0)
                    insertedUri = DataContract.ArticlesEntry.buildUri(_id);

                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mCreator.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {

            case ARTICLES: {
                rowsDeleted = db.delete(
                        DataContract.ArticlesEntry.TABLE, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mCreator.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowCount = 0;
        switch (match) {

            case ARTICLES: {
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.ArticlesEntry.TABLE, null, value);
                        if (_id != -1) {
                            rowCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }

            default:
                return super.bulkInsert(uri, values);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowCount;
    }

}

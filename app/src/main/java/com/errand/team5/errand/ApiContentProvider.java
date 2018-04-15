package com.errand.team5.errand;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ApiContentProvider extends ContentProvider {

    private MySQLiteHelper myDB;

    public ApiContentProvider() {
    }

    @Override
    public String getType(Uri uri) {
       return new String(uri.toString());
    }


    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        myDB = new MySQLiteHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // insert(null, values);
        final SQLiteDatabase db =
                myDB.getWritableDatabase();

        long id =
                db.insert("API",
                        null,
                        values);

        // Check if a new row is inserted or not.
        if (id > 0)
            return ContentUris.withAppendedId(uri, id);
        else
            throw new android.database.SQLException
                    ("Failed to insert row into "
                            + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Take in a selectionArgs array for a dev_key
        // Ex query(null,null,null,new String[]{"anJ02njn7362"},null);
        return myDB.getReadableDatabase().rawQuery("SELECT ID, DEV_KEY, APP_NAME FROM API WHERE DEV_KEY = ?", selectionArgs);
    }

    // Nothing below is applicable

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Not applicable
        return -1;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Not applicable
        return -1;
    }
}

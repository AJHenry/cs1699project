package com.errand.team5.errand;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class ApiContentProvider extends ContentProvider {
    private final String TAG = ApiContentProvider.class.toString();
    private MySQLiteHelper myDB;
    private DatabaseReference keys;

    public ApiContentProvider() {
    }

    @Override
    public String getType(Uri uri) {
       return new String(uri.toString());
    }


    @Override
    public boolean onCreate() {
        Log.e(TAG,"Content provider running");
        myDB = new MySQLiteHelper(getContext());
        keys = FirebaseDatabase.getInstance().getReference().child("apiKeys");

        keys.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int ID = -1;
                String APP_NAME=null,DEV_KEY=null;
                for (DataSnapshot idKey : dataSnapshot.getChildren()) {
                    ID = Integer.parseInt(idKey.getKey());
                    Log.e(TAG,"ID: " + ID);
                    for (DataSnapshot info : idKey.getChildren()) {
                        Log.e(TAG,"Thing: " + info.getValue().toString());
                        if (info.getKey().equals("APP_NAME")){
                            APP_NAME = info.getValue().toString();
                        }
                        else if (info.getKey().equals("DEV_KEY")){
                            DEV_KEY = info.getValue().toString();
                        }

                    }

                    ContentValues cv = new ContentValues();
                    cv.put("ID",ID);
                    cv.put("APP_NAME",APP_NAME);
                    cv.put("DEV_KEY",DEV_KEY);
                    Uri.Builder uri = new Uri.Builder();
                    uri.authority("keys");
                    insert(uri.build(), cv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // No need for this
            }
        });
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        try {
            final SQLiteDatabase db = myDB.getWritableDatabase();

            id = db.insert("API",
                            null,
                            values);
        } catch(Exception e){
            id = -1;
        }

        return ContentUris.withAppendedId(uri, id);
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

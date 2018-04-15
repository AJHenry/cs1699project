package com.errand.team5.errand;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The database helper used by the Hobbit Content Provider to create
 * and manage its underling database.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    /**
     * Database name.
     */
    private static String DATABASE_NAME = "api_db";

    /**
     * Database version number, which is updated with each schema
     * change.
     */
    private static int DATABASE_VERSION = 1;

    /*
     * SQL create table statements.
     */

    /**
     * SQL statement used to create the Hobbit table.
     */
    final String SQL_CREATE_API_TABLE =
            "CREATE TABLE IF NOT EXISTS "
                    + "API" + " ("
                    + "ID" + " INTEGER PRIMARY KEY, "
                    + "DEV_KEY" + " TEXT NOT NULL, "
                    + "APP_NAME" + " TEXT NOT NULL "
                    + " );";

    /**
     * Constructor - initialize database name and version, but don't
     * actually construct the database (which is done in the
     * onCreate() hook method). It places the database in the
     * application's cache directory, which will be automatically
     * cleaned up by Android if the device runs low on storage space.
     *
     * @param context
     */
    public MySQLiteHelper(Context context) {
        super(context,
                context.getCacheDir()
                        + File.separator
                        + DATABASE_NAME,
                null,
                DATABASE_VERSION);
    }

    /**
     * Hook method called when the database is created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table.
        db.execSQL(SQL_CREATE_API_TABLE);
    }

    /**
     * Hook method called when the database is upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        return;
    }
}


/**
 * 
 */
package com.concur.mobile.platform.config.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.concur.mobile.platform.provider.ClearSQLiteDatabase;

/**
 * Provides an extension of <code>SQLiteOpenHelper</code> to provide access to and handle upgrades for the Config provider SQLite
 * database.
 * 
 * @author andrewk
 */
public class ClearConfigDBHelper extends SQLiteOpenHelper {

    /**
     * Constructs an instance of <code>ClearConfigDBHelper</code> with an application context.
     * 
     * @param context
     *            contains a reference to the application context.
     */
    public ClearConfigDBHelper(Context context) {
        super(context, ConfigDBSchema.DATABASE_NAME, null, ConfigDBSchema.DATABASE_VERSION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        ConfigDBSchema.onCreate(new ClearSQLiteDatabase(db));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ConfigDBSchema.onUpgrade(new ClearSQLiteDatabase(db), oldVersion, newVersion);
    }

}

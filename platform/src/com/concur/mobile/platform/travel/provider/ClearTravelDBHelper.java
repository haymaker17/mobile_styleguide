package com.concur.mobile.platform.travel.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.concur.mobile.platform.provider.ClearSQLiteDatabase;

public class ClearTravelDBHelper extends SQLiteOpenHelper {

    /**
     * Constructs an instance of <code>ClearTravelDBHelper</code> with an application context.
     * 
     * @param context
     *            contains a reference to the application context.
     */
    public ClearTravelDBHelper(Context context) {
        super(context, TravelDBSchema.DATABASE_NAME, null, TravelDBSchema.DATABASE_VERSION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        TravelDBSchema.onCreate(new ClearSQLiteDatabase(db));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        TravelDBSchema.onUpgrade(new ClearSQLiteDatabase(db), oldVersion, newVersion);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = super.getReadableDatabase();
        new ClearSQLiteDatabase(db).enableForeignKeySupport();
        return db;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        new ClearSQLiteDatabase(db).enableForeignKeySupport();
        return db;
    }

}

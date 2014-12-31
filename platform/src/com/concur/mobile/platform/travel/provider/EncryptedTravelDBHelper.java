package com.concur.mobile.platform.travel.provider;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

import com.concur.mobile.platform.provider.EncryptedSQLiteDatabase;

/**
 * Provides an extension of <code>SQLiteOpenHelper</code> for accessing an encrypted SQLite database for the Travel provider.
 * 
 * @author andrewk
 */
public class EncryptedTravelDBHelper extends SQLiteOpenHelper {

    /**
     * Constructs an instance of <code>EncryptedTravelDBHelper</code> with an application context.
     * 
     * @param context
     *            contains a reference to the application context.
     */
    public EncryptedTravelDBHelper(Context context) {
        super(context, TravelDBSchema.DATABASE_NAME, null, TravelDBSchema.DATABASE_VERSION);
        // Load native libs required by SQLCipher.
        SQLiteDatabase.loadLibs(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        TravelDBSchema.onCreate(new EncryptedSQLiteDatabase(db));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        TravelDBSchema.onUpgrade(new EncryptedSQLiteDatabase(db), oldVersion, newVersion);
    }

    /**
     * Gets a reference to a <code>SQLiteDatabase</code> suitable for reading.
     * 
     * @param passphrase
     *            contains the passphrase used to access encrypted content.
     */
    public SQLiteDatabase getReadableDatabase(String passphrase) {
        SQLiteDatabase db = super.getReadableDatabase(passphrase);
        new EncryptedSQLiteDatabase(db).enableForeignKeySupport();
        return db;
    }

    /**
     * Gets a reference to a <code>SQLiteDatabase</code> suitable for reading/writing.
     * 
     * @param passphrase
     *            contains the passphrase used to access encrypted content.
     */
    public SQLiteDatabase getWritableDatabase(String passphrase) {
        SQLiteDatabase db = super.getWritableDatabase(passphrase);
        new EncryptedSQLiteDatabase(db).enableForeignKeySupport();
        return db;
    }

}

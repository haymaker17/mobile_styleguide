package com.concur.mobile.platform.config.provider;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

import com.concur.mobile.platform.provider.EncryptedSQLiteDatabase;

/**
 * Provides an extension of <code>SQLiteOpenHelper</code> for accessing an encrypted SQLite database for the Config provider.
 * 
 * @author andrewk
 */
public class EncryptedConfigDBHelper extends SQLiteOpenHelper {

    /**
     * Constructs an instance of <code>EncryptedConfigDBHelper</code> with an application context.
     * 
     * @param context
     *            contains a reference to the application context.
     */
    public EncryptedConfigDBHelper(Context context) {
        super(context, ConfigDBSchema.DATABASE_NAME, null, ConfigDBSchema.DATABASE_VERSION);

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
        ConfigDBSchema.onCreate(new EncryptedSQLiteDatabase(db));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ConfigDBSchema.onUpgrade(new EncryptedSQLiteDatabase(db), oldVersion, newVersion);
    }

}

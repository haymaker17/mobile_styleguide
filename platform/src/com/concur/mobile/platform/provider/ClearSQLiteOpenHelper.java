package com.concur.mobile.platform.provider;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * Provides an implementation of <code>PlatformSQLiteOpenHelper</code> for obtain unencrypted database references.
 * 
 * @author andrewk
 */
public class ClearSQLiteOpenHelper implements PlatformSQLiteOpenHelper {

    private static final String CLS_TAG = "ClearSQLiteOpenHelper";

    private SQLiteOpenHelper helper;

    public ClearSQLiteOpenHelper(SQLiteOpenHelper helper) {
        final String MTAG = CLS_TAG + ".<init>: ";

        try {
            Assert.assertNotNull(MTAG + "helper is null.", helper);
            this.helper = helper;
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, MTAG + afe.getMessage());
            throw afe;
        }
    }

    @Override
    public PlatformSQLiteDatabase getWritableDatabase(String passphrase) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return new ClearSQLiteDatabase(db);
    }

    @Override
    public PlatformSQLiteDatabase getReadableDatabase(String passphrase) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return new ClearSQLiteDatabase(db);
    }

    @Override
    public void close() {
        helper.close();
    }

}

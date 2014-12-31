package com.concur.mobile.platform.provider;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * Provides an implementation of <code>PlatformSQLiteOpenHelper</code> for obtain unencrypted database references.
 * 
 * @author andrewk
 */
public class EncryptedSQLiteOpenHelper implements PlatformSQLiteOpenHelper {

    private static final String CLS_TAG = "EncryptedSQLiteOpenHelper";

    private SQLiteOpenHelper helper;

    public EncryptedSQLiteOpenHelper(SQLiteOpenHelper helper) {
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
    public PlatformSQLiteDatabase getWritableDatabase(String passphrase) throws android.database.sqlite.SQLiteException {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase(passphrase);
        } catch (net.sqlcipher.database.SQLiteException sqlExc) {
            android.database.sqlite.SQLiteException exc = new android.database.sqlite.SQLiteException(
                    sqlExc.getMessage());
            exc.initCause(sqlExc);
            throw exc;
        }
        return new EncryptedSQLiteDatabase(db);
    }

    @Override
    public PlatformSQLiteDatabase getReadableDatabase(String passphrase) throws android.database.sqlite.SQLiteException {
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase(passphrase);
        } catch (net.sqlcipher.database.SQLiteException sqlExc) {
            android.database.sqlite.SQLiteException exc = new android.database.sqlite.SQLiteException(
                    sqlExc.getMessage());
            exc.initCause(sqlExc);
            throw exc;
        }
        return new EncryptedSQLiteDatabase(db);
    }

    @Override
    public void close() {
        helper.close();
    }

}

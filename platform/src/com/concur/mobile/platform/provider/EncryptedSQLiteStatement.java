package com.concur.mobile.platform.provider;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.sqlcipher.database.SQLiteStatement;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

public class EncryptedSQLiteStatement implements PlatformSQLiteStatement {

    private static final String CLS_TAG = "EncryptedSQLiteStatement";

    private SQLiteStatement sqlStmt;

    public EncryptedSQLiteStatement(SQLiteStatement sqlStmt) {
        final String MTAG = CLS_TAG + ".<init>: ";

        try {
            Assert.assertNotNull(MTAG + "sqlStmt is null.", sqlStmt);
            this.sqlStmt = sqlStmt;
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, MTAG + afe.getMessage());
            throw afe;
        }
    }

    @Override
    public void execute() throws android.database.SQLException {
        sqlStmt.execute();
    }

    @Override
    public void clearBindings() {
        sqlStmt.clearBindings();
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        sqlStmt.bindBlob(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        sqlStmt.bindDouble(index, value);
    }

    @Override
    public void bindLong(int index, long value) {
        sqlStmt.bindLong(index, value);
    }

    @Override
    public void bindString(int index, String value) {
        sqlStmt.bindString(index, value);
    }

    @Override
    public void bindNull(int index) {
        sqlStmt.bindNull(index);
    }

}

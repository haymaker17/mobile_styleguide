/**
 * 
 */
package com.concur.mobile.platform.provider;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * An implementation of <code>PlatformSQLiteDatabase</code> that does not encrypt data.
 * 
 * @author andrewk
 */
public class ClearSQLiteDatabase extends AbstractPlatformSQLiteDatabase implements PlatformSQLiteDatabase {

    private static final String CLS_TAG = "ClearSQLiteDatabase";

    private SQLiteDatabase db;

    /**
     * Constructs an instance of <code>ClearSQLiteDatabase</code>.
     * 
     * @param db
     *            contains a reference to a <code>SQLiteDatabase</code> instance.
     */
    public ClearSQLiteDatabase(SQLiteDatabase db) {

        final String MTAG = CLS_TAG + ".<init>: ";

        try {
            Assert.assertNotNull(MTAG + "db is null.", db);
            this.db = db;
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, MTAG + afe.getMessage());
            throw afe;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#delete(java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    @Override
    public int delete(String tableName, String selection, String[] selectionArgs) {
        return db.delete(tableName, selection, selectionArgs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#insert(java.lang.String, java.lang.String,
     * android.content.ContentValues)
     */
    @Override
    public long insert(String tableName, String nullColumnName, ContentValues values) {
        return db.insert(tableName, nullColumnName, values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#update(java.lang.String, android.content.ContentValues,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public int update(String tableName, ContentValues values, String selection, String[] selectionArgs) {
        return db.update(tableName, values, selection, selectionArgs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#compileStatement(java.lang.String)
     */
    @Override
    public PlatformSQLiteStatement compileStatement(String statement) throws SQLException {
        SQLiteStatement stmt = db.compileStatement(statement);
        return new ClearSQLiteStatement(stmt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#beginTransaction()
     */
    @Override
    public void beginTransaction() {
        db.beginTransaction();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#endTransaction()
     */
    @Override
    public void endTransaction() {
        db.endTransaction();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#setTransactionSuccessful()
     */
    @Override
    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#execSQL(java.lang.String)
     */
    @Override
    public void execSQL(String sql) {
        db.execSQL(sql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#getSQLiteQueryBuilder()
     */
    @Override
    public PlatformSQLiteQueryBuilder getSQLiteQueryBuilder() {
        return new ClearSQLiteQueryBuilder(db, new SQLiteQueryBuilder());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#rawQuery(java.lang.String, java.lang.String[])
     */
    @Override
    public Cursor rawQuery(String sql, String[] sqlArgs) {
        return db.rawQuery(sql, sqlArgs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return db.isReadOnly();
    }

}

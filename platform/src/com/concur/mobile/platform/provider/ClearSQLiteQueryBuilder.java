/**
 * 
 */
package com.concur.mobile.platform.provider;

import java.util.Map;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * An implementation of <code>PlatformSQLiteQueryBuilder</code> that does not encrypt data.
 * 
 * @author andrewk
 */
public class ClearSQLiteQueryBuilder implements PlatformSQLiteQueryBuilder {

    private static final String CLS_TAG = "ClearSQLiteQueryBuilder";

    private SQLiteQueryBuilder queryBldr;

    private SQLiteDatabase db;

    public ClearSQLiteQueryBuilder(SQLiteDatabase db, SQLiteQueryBuilder queryBldr) {
        final String MTAG = CLS_TAG + ".<init>: ";

        try {
            Assert.assertNotNull(MTAG + "db is null.", db);
            Assert.assertNotNull(MTAG + "queryBldr is null.", queryBldr);
            this.db = db;
            this.queryBldr = queryBldr;
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, MTAG + afe.getMessage());
            throw afe;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteQueryBuilder#query(java.lang.String[], java.lang.String,
     * java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Cursor query(String[] projection, String selection, String[] selectionArgs, String groupBy, String having,
            String orderBy) {
        return queryBldr.query(db, projection, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public void setTables(String tables) {
        queryBldr.setTables(tables);
    }

    @Override
    public void setProjectionMap(Map<String, String> projectionMap) {
        queryBldr.setProjectionMap(projectionMap);
    }

    @Override
    public void appendWhere(String where) {
        queryBldr.appendWhere(where);
    }

}

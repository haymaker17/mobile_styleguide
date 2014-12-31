/**
 * 
 */
package com.concur.mobile.platform.provider;

import java.util.Map;

import android.database.Cursor;

/**
 * Provides an interface for abstracting SQLiteQueryBuilder operations.
 * 
 * @author andrewk
 */
public interface PlatformSQLiteQueryBuilder {

    /**
     * Sets the list of tables used.
     * 
     * @param tables
     *            contains a list of tables used.
     */
    public void setTables(String tables);

    /**
     * Sets the projection map.
     * 
     * @param projectionMap
     *            contains the projection map.
     */
    public void setProjectionMap(Map<String, String> projectionMap);

    /**
     * Appends a where clause.
     * 
     * @param where
     *            contains the where clause to append.
     */
    public void appendWhere(String where);

    /**
     * Performs a database query operation on the database associated with this instance of
     * <code>PlatformSQLiteQueryBuilder</code>.
     * 
     * @param projection
     *            contains the projection to return.
     * @param selection
     *            contains the selection clause.
     * @param selectionArgs
     *            contains the selection arguments.
     * @param groupBy
     *            contains the "group by" clause.
     * @param having
     *            contains the "having" clause.
     * @param orderBy
     *            contains the "order by" clause.
     * @return returns an instance of <code>Cursor</code> containing the results.
     */
    public Cursor query(String[] projection, String selection, String[] selectionArgs, String groupBy, String having,
            String orderBy);

}

/**
 * 
 */
package com.concur.mobile.platform.provider;

import android.content.ContentValues;

/**
 * Provides an interface that can be used to perform bulk insertion operations for a particular URI.
 */
public interface BulkInserter {

    /**
     * Will prepare a SQLite statement that can be used to perform a table insertion.
     * 
     * @param db
     *            contains a reference to the database upon which the statement will be prepared.
     * 
     * @return returns an instance of <code>SQLiteStatement</code> used to perform an insertion operation.
     */
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db);

    /**
     * Will bind values in <code>values</code> to argument positions within <code>sqlStmt</code>.
     * 
     * <br>
     * <br>
     * <b>NOTE:</b> This method should only perform a "bind" operation on values in <code>values</code> to argument positions in
     * <code>sqlStmt</code>.
     * 
     * @param sqlStmt
     *            contains a reference to a <code>SQLiteStatment</code> to be bound to values in <code>values</code>.
     * @param values
     *            contains a reference to a <code>ContentValues</code> object containing values to be bound to arguments in
     *            <code>sqlStmt</code>.
     */
    public void bindSQLiteStatmentValues(PlatformSQLiteStatement sqlStmt, ContentValues values);

}

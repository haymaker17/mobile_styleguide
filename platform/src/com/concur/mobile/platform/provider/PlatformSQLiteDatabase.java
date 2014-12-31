package com.concur.mobile.platform.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

/**
 * Provides an interface for platform content provider DB operations.
 * 
 * @author andrewk
 */
public interface PlatformSQLiteDatabase {

    /**
     * Will perform a database deletion operation.
     * 
     * @param tableName
     *            contains the table name.
     * @param selection
     *            contains the selection.
     * @param selectionArgs
     *            contains the selection arguments.
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a count pass "1"
     *         as the whereClause.
     */
    public int delete(String tableName, String selection, String[] selectionArgs);

    /**
     * Will perform a database insertion operation.
     * 
     * @param tableName
     *            contains the table name.
     * @param nullColumnName
     *            contains the null column name.
     * @param values
     *            contains the set of values to insert.
     * @return the row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insert(String tableName, String nullColumnName, ContentValues values);

    /**
     * Will perform a database update operation.
     * 
     * @param tableName
     *            contains the table name.
     * @param values
     *            contains the set of update values.
     * @param selection
     *            contains the selection.
     * @param selectionArgs
     *            contains the selection arguments.
     * @return the number of rows affected
     */
    public int update(String tableName, ContentValues values, String selection, String[] selectionArgs);

    /**
     * Will compile a SQL statement into <code>PlatformSQLiteStatement</code>.
     * 
     * @param statement
     *            contains the SQL statement.
     * @return returns an instance of <code>PlatformSQLiteStatement</code>>
     */
    public PlatformSQLiteStatement compileStatement(String statement) throws SQLException;

    /**
     * Begins a transaction in EXCLUSIVE mode.
     * 
     * Transactions can be nested. When the outer transaction is ended all of the work done in that transaction and all of the
     * nested transactions will be committed or rolled back. The changes will be rolled back if any transaction is ended without
     * being marked as clean (by calling setTransactionSuccessful). Otherwise they will be committed.
     */
    public void beginTransaction();

    /**
     * End a transaction. See beginTransaction for notes about how to use this and when transactions are committed and rolled
     * back.
     */
    public void endTransaction();

    /**
     * Marks the current transaction as successful. Do not do any more database work between calling this and calling
     * endTransaction. Do as little non-database work as possible in that situation too. If any errors are encountered between
     * this and endTransaction the transaction will still be committed.
     * 
     * @throws IllegalStateException
     *             if the current thread is not in a transaction or the transaction is already marked as successful.
     */
    public void setTransactionSuccessful();

    /**
     * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
     * 
     * It has no means to return any data (such as the number of affected rows).
     * 
     * @param sql
     *            the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
     */
    public void execSQL(String sql) throws SQLException;

    /**
     * Gets an instance of <code>PlatformSQLiteQueryBuilder</code>.
     * 
     * @return
     */
    public PlatformSQLiteQueryBuilder getSQLiteQueryBuilder();

    /**
     * Will perform a raw SQL query.
     * 
     * @param sql
     *            contains the SQL statement.
     * @param sqlArgs
     *            contains the SQL arguments.
     * @return returns an instance of <code>Cursor</code>.
     */
    public Cursor rawQuery(String sql, String[] sqlArgs);

    /**
     * Gets whether this database is opened read-only.
     * 
     * @return returns whether this database is opened read-only.
     */
    public boolean isReadOnly();

    /**
     * Will enable foreign key support by executing the SQL "PRAGMA foreign_keys = ON".
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean enableForeignKeySupport();

    /**
     * Will disable foreign key support by executing the SQL "PRAGMA foreign_keys = OFF".
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean disableForeignKeySupport();

    /**
     * Gets the version of SQLite.
     * 
     * @return returns the version of SQLite upon success; otherwise, <code>null</code> is returned.
     */
    public String getSQLiteVersion();

}

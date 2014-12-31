/**
 * 
 */
package com.concur.mobile.platform.provider;

import android.database.sqlite.SQLiteException;

/**
 * An interface describing an API for opening/closing a SQLite database.
 * 
 * @author andrewk
 */
public interface PlatformSQLiteOpenHelper {

    /**
     * Will retrieve an instance of <code>PlatformSQLiteDatabase</code> supporting database write operations.
     * 
     * @param passphrase
     *            contains the passphrase for accessing the database.
     * @return returns a writable instance of <code>PlatformSQLiteDatabase</code>.
     */
    public PlatformSQLiteDatabase getWritableDatabase(String passphrase) throws SQLiteException;

    /**
     * Will retrieve an instance of <code>PlatformSQLiteDatabase</code> supporting read-only operations.
     * 
     * @param passphrase
     *            contains the passphrase for accessing the database.
     * @return returns a read-only instance of <code>PlatformSQLiteDatabase</code>.
     */
    public PlatformSQLiteDatabase getReadableDatabase(String passphrase);

    /**
     * Will perform a close operation on the database.
     */
    public void close();

}

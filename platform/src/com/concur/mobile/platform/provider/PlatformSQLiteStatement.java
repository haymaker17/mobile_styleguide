/**
 * 
 */
package com.concur.mobile.platform.provider;

import android.database.SQLException;

/**
 * An interface providing SQLiteStatement support.
 * 
 * @author andrewk
 */
public interface PlatformSQLiteStatement {

    /**
     * Will execute the statement.
     */
    public void execute() throws SQLException;

    /**
     * Will clear all statement bindings.
     */
    public void clearBindings();

    /**
     * Will bind a blob value.
     * 
     * @param index
     *            contains the bind index.
     * @param value
     *            contains the bind value.
     */
    public void bindBlob(int index, byte[] value);

    /**
     * Will bind a double value.
     * 
     * @param index
     *            contains the bind index.
     * @param value
     *            contains the bind value.
     */
    public void bindDouble(int index, double value);

    /**
     * Will bind a long value.
     * 
     * @param index
     *            contains the bind index.
     * @param value
     *            contains the bind value.
     */
    public void bindLong(int index, long value);

    /**
     * Will bind a string value.
     * 
     * @param index
     *            contains the bind index.
     * @param value
     *            contains the bind value.
     */
    public void bindString(int index, String value);

    /**
     * Will bind the value <code>NULL</code>.
     * 
     * @param index
     *            contains the bind index.
     */
    public void bindNull(int index);

}

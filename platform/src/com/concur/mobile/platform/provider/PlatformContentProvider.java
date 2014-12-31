/**
 * 
 */
package com.concur.mobile.platform.provider;

import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.concur.mobile.platform.util.Const;

/**
 * Provides an abstract base class upon which a content provider can be based.
 */
public abstract class PlatformContentProvider extends ContentProvider {

    private static final String CLS_TAG = "PlatformContentProvider";

    /**
     * Contains the result data bundle key for obtaining the result (BOOLEAN) of calling a provider method.
     */
    public static final String PROVIDER_METHOD_RESULT_KEY = "provider.method.result";

    /**
     * Contains the extras data bundle key to pass in the a passphrase.
     */
    public static final String PROVIDER_METHOD_PASSPHRASE_KEY = "provider.method.passphrase";

    /**
     * Contains the method name used to set the passphrase for access to the content. This method will put into the
     * <code>Bundle</code> return object the result using the <code>PROVIDER_METHOD_RESULT_KEY</code> key. If this method returns
     * <code>false</code> for <code>PROVIDER_METHOD_RESULT_KEY</code>, then it indicates that the passed in passphrase is
     * incorrect resulting in access to content failing. The passphrase should be passed as the second argument to this method
     * call.
     */
    public static final String PROVIDER_METHOD_SET_PASSPHRASE = "SetPassphrase";

    /**
     * Contains the method name used to clear all content from this provider. This method will put into the <code>Bundle</code>
     * return object the result using the <code>PROVIDER_METHOD_RESULT_KEY</code> key.
     */
    public static final String PROVIDER_METHOD_CLEAR_CONTENT = "ClearData";

    /**
     * Contains the method name used to reset the passphrase for access to the content. This method expects as the
     * <code>arg</code> parameter (the 2nd argument to the call) the current passphrase. The <code>extras</code> bundle in the
     * call should contain the new passphrase using the key <code>PROVIDER_METHOD_PASSPHRASE_KEY</code>.
     */
    public static final String PROVIDER_METHOD_RESET_PASSPHRASE = "ResetPassphrase";

    // Contains the Uri matcher.
    private UriMatcher sUriMatcher;

    /**
     * Contains a map from integer based codes to instances of <code>UriMatcherInfo</code> used in the various methods below,
     * i.e., insert, update, etc.
     */
    private SparseArray<UriMatcherInfo> codeUriMatcherInfoMap;

    /**
     * Contains a reference to the instance of <code>PlatformSQLiteOpenHelper</code> used to obtain a database handle.
     */
    private PlatformSQLiteOpenHelper dbHelper;

    /**
     * Contains the passphrase used to access the database.
     */
    private String passphrase = "7c5TVH7MOr7BAsPz6OhwioL3KW84emM1MvqtVywwzjGRaHqq6jmph4SvCug6OwW8";

    @Override
    public boolean onCreate() {

        // Init the Uri matcher.
        sUriMatcher = initUriMatcher();
        // Init projection maps.
        initProjectionMaps();
        // Init the Uri matcher info map.
        codeUriMatcherInfoMap = initCodeUriMatcherInfoMap();
        // Init the SQLite open helper.
        dbHelper = initPlatformSQLiteOpenHelper(getContext());

        return true;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Bundle retVal = new Bundle();

        if (!TextUtils.isEmpty(method)) {
            if (method.equalsIgnoreCase(PROVIDER_METHOD_SET_PASSPHRASE)) {
                setPassphrase(retVal, arg);
            } else if (method.equalsIgnoreCase(PROVIDER_METHOD_CLEAR_CONTENT)) {
                clearContent(retVal);
            } else if (method.equalsIgnoreCase(PROVIDER_METHOD_RESET_PASSPHRASE)) {
                resetPassphrase(retVal, arg, extras);
            }
        }
        return retVal;
    }

    /**
     * Will reset the passphrase used to access content.
     * 
     * @param result
     *            contains the result bundle.
     * @param currentPassphrase
     *            contains the current passphrase.
     * @param extras
     *            contains the bundle with new passphrase.
     */
    private void resetPassphrase(Bundle result, String currentPassphrase, Bundle extras) {

        try {
            // Close and re-open the database.
            dbHelper.close();
            PlatformSQLiteDatabase db = dbHelper.getWritableDatabase(currentPassphrase);
            String newPassphrase = extras.getString(PROVIDER_METHOD_PASSPHRASE_KEY);
            if (newPassphrase != null) {
                newPassphrase = newPassphrase.trim();
                if (!TextUtils.isEmpty(newPassphrase)) {
                    // Exec the PRAGMA rekey statement.
                    db.execSQL("PRAGMA rekey = '" + newPassphrase + "'");
                    this.passphrase = newPassphrase;
                    result.putBoolean(PROVIDER_METHOD_RESULT_KEY, true);
                } else {
                    result.putBoolean(PROVIDER_METHOD_RESULT_KEY, false);
                }
            } else {
                result.putBoolean(PROVIDER_METHOD_RESULT_KEY, false);
            }

        } catch (Throwable throwable) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".clearContent: " + throwable.getMessage());
            result.putBoolean(PROVIDER_METHOD_RESULT_KEY, false);
        } finally {
            dbHelper.close();
        }
    }

    private void clearContent(Bundle result) {
        try {
            // Close any open database.
            dbHelper.close();
            // Punt the database.
            getContext().deleteDatabase(getDatabaseName());
            result.putBoolean(PROVIDER_METHOD_RESULT_KEY, true);
        } catch (Throwable throwable) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".clearContent: " + throwable.getMessage());
            result.putBoolean(PROVIDER_METHOD_RESULT_KEY, false);
        }
    }

    /**
     * Gets the name of the database storing content.
     * 
     * @return returns the name of the database storing content.
     */
    protected abstract String getDatabaseName();

    /**
     * Will set the passphrase that should be used to access content.
     * 
     * @param result
     *            contains the result bundle.
     * @param passphrase
     *            contains the passphrase used to access the content.
     */
    private void setPassphrase(Bundle result, String passphrase) {
        try {
            if (passphrase != null) {
                passphrase = passphrase.trim();
                if (!TextUtils.isEmpty(passphrase)) {
                    // Ensure any database is closed.
                    dbHelper.close();
                    // This method will throw a SQLException if the database already exists
                    // and was encrypted using a different passphrase than 'arg'.
                    dbHelper.getWritableDatabase(passphrase);
                    this.passphrase = passphrase;
                    result.putBoolean(PROVIDER_METHOD_RESULT_KEY, true);
                } else {
                    result.putBoolean(PROVIDER_METHOD_RESULT_KEY, false);
                }
            } else {
                result.putBoolean(PROVIDER_METHOD_RESULT_KEY, false);
            }
        } catch (Throwable throwable) {
            result.putBoolean(PROVIDER_METHOD_RESULT_KEY, false);
        } finally {
            dbHelper.close();
        }
    }

    /**
     * Initializes the <code>sUriMatcher</code> member used to match URI's to integer-based codes.
     * 
     * @return returns an initialized instance of <code>UriMatcher</code>.
     */
    protected abstract UriMatcher initUriMatcher();

    /**
     * Initializes any projection maps used in queries.
     */
    protected abstract void initProjectionMaps();

    /**
     * Initializes the <code>codeUriMatcherInfoMap</code> map used to map from UriMatcher codes to instances of
     * <code>UriMatcherInfo</code> objects.
     * 
     * @return returns an initialized instance of a <code>SparseArray</code>.
     */
    protected abstract SparseArray<UriMatcherInfo> initCodeUriMatcherInfoMap();

    /**
     * Initializes the <code>dbHelper</code> uses to obtain instances of the database.
     * 
     * @param context
     *            contains a reference to the application context.
     * 
     * @return returns an initialized instance of <code>PlatformSQLiteOpenHelper</code>.
     */
    public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
        PlatformSQLiteOpenHelper helper = null;

        return helper;
    }

    /**
     * Gets the instance of <code>UriMatcherInfo</code> for <code>uri</code>.
     * 
     * @param uri
     *            contains the uri.
     * @return contains the instance of <code>UriMatcherInfo</code> matched to <code>uri</code>.
     */
    private UriMatcherInfo getMatcherInfo(Uri uri) {
        UriMatcherInfo info = null;
        int uriMatcherCode = sUriMatcher.match(uri);
        if (uriMatcherCode != UriMatcher.NO_MATCH) {
            // Grab the UriMatcherInfo object based on the matched integer code.
            info = codeUriMatcherInfoMap.get(uriMatcherCode);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return info;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {

        UriMatcherInfo info = getMatcherInfo(uri);
        if (info == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getType: missing info object for matched URI + '" + uri.toString() + "'.");
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return info.mimeType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int count = 0;

        try {

            // Perform parameter checks.

            // Obtain a reference to the database.
            PlatformSQLiteDatabase db = dbHelper.getWritableDatabase(passphrase);

            StringBuilder idSelection = null;

            UriMatcherInfo info = getMatcherInfo(uri);
            if (info == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".delete: missing info object for matched URI + '" + uri.toString()
                        + "'.");
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
            if (info.isIdSelection) {
                idSelection = new StringBuilder(info.idColumnName);
                idSelection.append(" = ");
                idSelection.append(uri.getPathSegments().get(info.idPathPosition));
            }

            // If there were additional selection criteria, append them to the final
            // WHERE clause
            if (!TextUtils.isEmpty(selection)) {
                if (idSelection != null) {
                    idSelection.append(" AND ");
                    idSelection.append(selection);
                    selection = idSelection.toString();
                }
            } else if (idSelection != null) {
                selection = idSelection.toString();
            }

            // Performs the delete.
            count = db.delete(info.tableName, selection, selectionArgs);

            /*
             * Gets a handle to the content resolver object for the current context, and notifies it that the incoming URI
             * changed. The object passes this along to the resolver framework, and observers that have registered themselves for
             * the provider are notified.
             */
            getContext().getContentResolver().notifyChange(uri, null);

        } catch (Throwable t) {
            Throwable throwable = t.getCause();
            if (throwable == null) {
                throwable = t;
            }
            Log.e(Const.LOG_TAG, CLS_TAG + ".delete: " + throwable);
        } finally {
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Uri insRowUri = null;

        try {

            // Perform parameter checks.

            // Obtain a reference to the database.
            PlatformSQLiteDatabase db = dbHelper.getWritableDatabase(passphrase);

            UriMatcherInfo info = getMatcherInfo(uri);
            if (info == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".insert: missing info object for matched URI + '" + uri.toString()
                        + "'.");
                throw new IllegalArgumentException("Unknown URI " + uri);
            }

            // Performs the insert and returns the ID of the new note.
            long rowId = db.insert(info.tableName, info.nullColumnName, values);

            // If the insert succeeded, the row ID exists.
            if (rowId > 0) {
                // Creates a URI with the ID pattern and the new row ID appended to it.
                insRowUri = ContentUris.withAppendedId(info.contentIdUriBase, rowId);

                // Notifies observers registered against this provider that the data changed.
                getContext().getContentResolver().notifyChange(insRowUri, null);
            } else {
                // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
                throw new SQLException("Failed to insert row into " + uri);
            }

        } catch (Throwable t) {
            Throwable throwable = t.getCause();
            if (throwable == null) {
                throwable = t;
            }
            Log.e(Const.LOG_TAG, CLS_TAG + ".insert: " + throwable);
        } finally {
        }
        return insRowUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[],
     * java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;

        try {

            // Perform parameter checks.

            // Obtain a reference to the database.
            PlatformSQLiteDatabase db = dbHelper.getWritableDatabase(passphrase);

            // Perform db ops.
            PlatformSQLiteQueryBuilder sqlBldr = db.getSQLiteQueryBuilder();
            String groupBy = null;

            UriMatcherInfo info = getMatcherInfo(uri);
            if (info == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".query: missing info object for matched URI + '" + uri.toString()
                        + "'.");
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
            sqlBldr.setTables(info.tableName);
            sqlBldr.setProjectionMap(info.projectionMap);
            if (info.isIdSelection) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(info.idColumnName);
                strBldr.append(" = ");
                strBldr.append(uri.getPathSegments().get(info.idPathPosition));
                sqlBldr.appendWhere(strBldr.toString());
            }

            // Set the "order by" clause.
            String orderBy;
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = info.defaultSortOrder;
            } else {
                orderBy = sortOrder;
            }

            // Get the database and run the query
            cursor = sqlBldr.query(projection, selection, selectionArgs, groupBy, null, orderBy);

            // Tell the cursor what uri to watch, so it knows when its source data changes
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        } catch (Throwable t) {
            Throwable throwable = t.getCause();
            if (throwable == null) {
                throwable = t;
            }
            Log.e(Const.LOG_TAG, CLS_TAG + ".query: " + throwable);
        } finally {
        }
        return cursor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String,
     * java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int count = 0;

        try {

            // Perform parameter checks.
            // Obtain a reference to the database.
            PlatformSQLiteDatabase db = dbHelper.getWritableDatabase(passphrase);

            StringBuilder idSelection = null;

            UriMatcherInfo info = getMatcherInfo(uri);
            if (info == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".update: missing info object for matched URI + '" + uri.toString()
                        + "'.");
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
            if (info.isIdSelection) {
                idSelection = new StringBuilder(info.idColumnName);
                idSelection.append(" = ");
                idSelection.append(uri.getPathSegments().get(info.idPathPosition));
            }

            // If there were additional selection criteria, append them to the final
            // WHERE clause
            if (!TextUtils.isEmpty(selection)) {
                if (idSelection != null) {
                    idSelection.append(" AND ");
                    idSelection.append(selection);
                    selection = idSelection.toString();
                }
            } else if (idSelection != null) {
                selection = idSelection.toString();
            }

            // Performs the delete.
            count = db.update(info.tableName, values, selection, selectionArgs);

            /*
             * Gets a handle to the content resolver object for the current context, and notifies it that the incoming URI
             * changed. The object passes this along to the resolver framework, and observers that have registered themselves for
             * the provider are notified.
             */
            getContext().getContentResolver().notifyChange(uri, null);

        } catch (Throwable t) {
            Throwable throwable = t.getCause();
            if (throwable == null) {
                throwable = t;
            }
            Log.e(Const.LOG_TAG, CLS_TAG + ".update: " + throwable);
        } finally {
        }

        return count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final PlatformSQLiteDatabase db = dbHelper.getWritableDatabase(passphrase);

        UriMatcherInfo info = getMatcherInfo(uri);
        if (info == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bulkInsert: missing info object for matched URI + '" + uri.toString()
                    + "'.");
            throw new IllegalArgumentException(CLS_TAG + ".bulkInsert: unsupported uri: " + uri);
        }

        if (info.bulkInserter != null) {
            PlatformSQLiteStatement insert = info.bulkInserter.prepareSQLiteStatement(db);
            if (insert != null) {
                int numInserted = 0;
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        info.bulkInserter.bindSQLiteStatmentValues(insert, value);
                        insert.execute();
                        insert.clearBindings();
                    }
                    db.setTransactionSuccessful();
                    numInserted = values.length;
                } catch (Throwable t) {
                    Throwable throwable = t.getCause();
                    if (throwable == null) {
                        throwable = t;
                    }
                    Log.e(Const.LOG_TAG, CLS_TAG + ".bulkInsert: " + throwable);
                } finally {
                    db.endTransaction();
                }
                return numInserted;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".bulkInsert: info object returned null SQLiteStatment for matched URI '" + uri);
                throw new UnsupportedOperationException(CLS_TAG
                        + ".bulkInsert: info object returned null SQLiteStatement.");
            }
        } else {
            throw new UnsupportedOperationException(CLS_TAG + ".bulkInsert: unsupported uri: " + uri);
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {

        ParcelFileDescriptor retVal = null;

        // Obtain a reference to the database.
        final PlatformSQLiteDatabase db = dbHelper.getWritableDatabase(passphrase);

        UriMatcherInfo info = getMatcherInfo(uri);
        if (info == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".openFile: missing info object for matched URI + '" + uri.toString() + "'.");
            throw new IllegalArgumentException(CLS_TAG + ".openFile: unsupported uri: " + uri);
        }

        if (info.isIdSelection) {
            if (info.fileOpener != null) {
                retVal = info.fileOpener.openFile(getContext(), db, uri, mode);
                if (retVal == null) {
                    throw new FileNotFoundException(CLS_TAG + ".openFile: unable to open file");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".openFile: info object returned null FileOpener for matched URI '"
                        + uri);
                throw new UnsupportedOperationException(CLS_TAG + ".openFile: info object returned null FileOpener.");
            }
        } else {
            throw new IllegalArgumentException(CLS_TAG + ".openFile: Use an id-based URI only.");
        }
        return retVal;
    }

}

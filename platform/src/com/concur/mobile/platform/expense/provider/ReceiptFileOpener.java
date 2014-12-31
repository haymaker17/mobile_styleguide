package com.concur.mobile.platform.expense.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.concur.mobile.platform.provider.FileOpener;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.provider.PlatformSQLiteQueryBuilder;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.CursorUtil;

public class ReceiptFileOpener implements FileOpener {

    private static final String CLS_TAG = "ReceiptFileOpener";

    private static final String RECEIPT_DIR_NAME = "ExpenseProviderReceipts";

    /**
     * Contains the projection map.
     */
    HashMap<String, String> projectionMap;

    /**
     * Constructs an instance of <code>ReceiptFileOpener</code>.
     * 
     * @param projectionMap
     *            contains the projection map.
     */
    ReceiptFileOpener(HashMap<String, String> projectionMap) {
        this.projectionMap = projectionMap;
    }

    @Override
    public ParcelFileDescriptor openFile(Context context, PlatformSQLiteDatabase db, Uri uri, String mode)
            throws FileNotFoundException {

        ParcelFileDescriptor retVal = null;

        File rcptDir = getReceiptDirectory(context);
        if (rcptDir != null) {

            // Determine whether a thumbnail is being requested.
            String thumbNailQueryParam = uri.getQueryParameter(Expense.ReceiptColumns.THUMBNAIL_QUERY_PARAMETER);
            boolean isThumbNail = Boolean.parseBoolean(thumbNailQueryParam);

            // Grab the content id.
            Long contentId = ContentUris.parseId(uri);

            // Obtain any current stored receipt path.
            String path = getReceiptPath(db, contentId, isThumbNail);

            // If path non-null, then check for file existance.
            if (path != null) {
                File pathFile = new File(path);
                if (!pathFile.exists()) {
                    path = null;
                }
            }

            // If path is null, then determine if this is for 'w' access
            if (path == null) {
                // Check for write access.
                if (mode.indexOf('w') != -1) {
                    path = createPathForContentId(rcptDir, contentId, isThumbNail);
                }
            }

            // At this point, if the path isn't defined, then it's a non-write operation
            // on a file that doesn't exist and so the return value will be null.
            if (path != null) {

                int modeBits = parseMode(mode);
                retVal = ParcelFileDescriptor.open(new File(path), modeBits);

                // Update last access time and path.
                if (!updateLastAccessTimePath(context, db, uri, path, isThumbNail)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".openFile: unable to update last access time/path!");
                }
            }

        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".openFile: unable to access receipts external directory!");
            throw new FileNotFoundException(".openFile: unable to access receipts external directory!");
        }

        return retVal;
    }

    /**
     * Will retrieve the stored receipt path.
     * 
     * @param db
     *            contains a reference to the platform sqlite database object.
     * @param contentId
     *            contains the receipt content id.
     * @param isThumbNail
     *            contains whether or not this
     * @return returns the receipt path.
     * @throws a
     *             <code>FileNotFoundException</code> if the receipt path can't be read.
     */
    private String getReceiptPath(PlatformSQLiteDatabase db, Long contentId, boolean isThumbNail)
            throws FileNotFoundException {
        String path = null;

        Cursor cursor = null;
        try {
            // Perform the query to retrieve any current path.
            PlatformSQLiteQueryBuilder sqlBldr = db.getSQLiteQueryBuilder();
            sqlBldr.setTables(Expense.ReceiptColumns.TABLE_NAME);
            sqlBldr.setProjectionMap(projectionMap);
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.ReceiptColumns._ID);
            strBldr.append(" = ");
            strBldr.append(Long.toString(contentId));
            sqlBldr.appendWhere(strBldr.toString());

            // Get the database and run the query
            String[] projection = { Expense.ReceiptColumns.LOCAL_PATH, Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH };
            cursor = sqlBldr.query(projection, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (isThumbNail) {
                    path = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH);
                } else {
                    path = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.LOCAL_PATH);
                }
            }
        } catch (Throwable t) {
            Throwable throwable = t.getCause();
            if (throwable == null) {
                throwable = t;
            }
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReceiptPath: " + throwable);
            throw new FileNotFoundException(".getReceiptPath: unable to query for local path information!");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * Will generate an absolute path name for a receipt file given the receipt directory, a content id and whether this path is
     * for a thumbnail.
     * 
     * @param rcptDir
     *            contains a reference to the receipt directory.
     * @param contentId
     *            contains the content id.
     * @param isThumbnail
     *            contains whether this is for a thumbnail.
     * @return an absolute path name for a receipt file.
     */
    private String createPathForContentId(File rcptDir, Long contentId, boolean isThumbnail) {
        File pathFile = new File(rcptDir, ((isThumbnail) ? Long.toString(contentId) + "_tn" : Long.toString(contentId)));
        return pathFile.getAbsolutePath();
    }

    /**
     * Will update the "last access time" and path information for <code>uri</code>.
     * 
     * @param context
     *            contains an application context.
     * @param uri
     *            contains the uri.
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    private boolean updateLastAccessTimePath(Context context, PlatformSQLiteDatabase db, Uri uri, String path,
            boolean isThumbNail) {

        boolean retVal = true;

        try {
            StringBuilder idSelection = new StringBuilder(Expense.ReceiptColumns._ID);
            idSelection.append(" = ");
            idSelection.append(uri.getPathSegments().get(Expense.ReceiptColumns.RECEIPTS_ID_PATH_POSITION));

            ContentValues values = new ContentValues();
            values.put(Expense.ReceiptColumns.LAST_ACCESS_TIME, System.currentTimeMillis());
            if (!isThumbNail) {
                values.put(Expense.ReceiptColumns.LOCAL_PATH, path);
            } else {
                values.put(Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH, path);
            }

            // Performs the delete.
            int count = db.update(Expense.ReceiptColumns.TABLE_NAME, values, idSelection.toString(), null);

            // Ensure one row was updated.
            retVal = (count == 1);

        } catch (Throwable t) {
            Throwable throwable = t.getCause();
            if (throwable == null) {
                throwable = t;
            }
            Log.e(Const.LOG_TAG, CLS_TAG + ".update: " + throwable);
            retVal = false;
        }

        return retVal;
    }

    // NOTE: Method below was taken from:
    // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/ParcelFileDescriptor.java
    // The 'ParcelFileDescriptor.parseMode' is only available in API 19 or later!

    /**
     * Converts a string representing a file mode, such as "rw", into a bitmask suitable for use with {@link #open}.
     * <p>
     * 
     * @param mode
     *            The string representation of the file mode.
     * @return A bitmask representing the given file mode.
     * @throws IllegalArgumentException
     *             if the given string does not match a known file mode.
     */
    private static int parseMode(String mode) {
        final int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Bad mode '" + mode + "'");
        }
        return modeBits;
    }

    /**
     * Will get a reference to (creating it if necessary) a reference to the receipt directory.
     * 
     * @param context
     *            contains an application context.
     * @return returns a reference to the receipt directory.
     */
    private File getReceiptDirectory(Context context) {
        File retVal = null;
        File extFilesDir = context.getExternalFilesDir(null);
        if (extFilesDir != null) {
            retVal = new File(extFilesDir.getAbsolutePath(), RECEIPT_DIR_NAME);
            if (!retVal.exists()) {
                if (!retVal.mkdirs()) {
                    retVal = null;
                }
            }
        }
        return retVal;
    }

}

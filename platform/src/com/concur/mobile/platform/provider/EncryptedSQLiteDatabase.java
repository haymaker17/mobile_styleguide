package com.concur.mobile.platform.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.util.Const;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;
import net.sqlcipher.database.SQLiteStatement;

import java.nio.ByteBuffer;

public class EncryptedSQLiteDatabase extends AbstractPlatformSQLiteDatabase implements PlatformSQLiteDatabase {

    private static final String CLS_TAG = "ClearPlatformSQLiteDatabase";

    private SQLiteDatabase db;

    /**
     * Constructs an instance of <code>EncryptedSQLiteDatabase</code>.
     *
     * @param db contains a reference to a <code>SQLiteDatabase</code> instance.
     */
    public EncryptedSQLiteDatabase(SQLiteDatabase db) {
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
    public PlatformSQLiteStatement compileStatement(String statement) throws android.database.SQLException {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement(statement);
        } catch (net.sqlcipher.SQLException sqlExc) {
            android.database.SQLException exc = new android.database.SQLException(sqlExc.getMessage());
            exc.initCause(sqlExc);
            throw exc;
        }
        return new EncryptedSQLiteStatement(stmt);
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

    @Override
    public void execSQL(String sql) throws android.database.SQLException {
        try {
            db.execSQL(sql);
        } catch (net.sqlcipher.database.SQLiteException sqlExc) {
            android.database.SQLException exc = new android.database.SQLException(sqlExc.getMessage());
            exc.initCause(sqlExc);
            throw exc;
        }
    }

    @Override
    public PlatformSQLiteQueryBuilder getSQLiteQueryBuilder() {
        return new EncryptedSQLiteQueryBuilder(db, new SQLiteQueryBuilder());
    }

    @Override
    public Cursor rawQuery(String sql, String[] sqlArgs) {
        return db.rawQuery(sql, sqlArgs);
    }

    @Override
    public boolean isReadOnly() {
        return db.isReadOnly();
    }

    public Bitmap getReceiptImage(int id) {
        // Declare Vars
        Bitmap bitmapResult = null;
        byte [] byBitmap;
        Cursor cursor;
        String [] whereArgs;
        int idxImage;

        // Initialize
        whereArgs = new String[1];
        whereArgs[0] = new String();

        cursor = db.query(Expense.ExpenseItReceiptColumns.TABLE_NAME, null, Expense
                        .ExpenseItReceiptColumns.ID + "=?",
                whereArgs, null, null, null);

        if (cursor != null){
            idxImage = cursor.getColumnIndex(Expense.ExpenseItReceiptColumns.IMAGE_DATA);

            if (cursor.moveToNext()){
                byBitmap = cursor.getBlob(idxImage);
                bitmapResult = BitmapFactory.decodeByteArray(byBitmap,0,byBitmap.length);
            }
        }

        db.close();
        return bitmapResult;
    }

    public boolean addReceiptImage(Bitmap bitmap) {

        // Declare Vars
        ContentValues cv;
        int updateResult = 0;
        String [] whereArgs;
        boolean bResult;

        // Input Validation
        if (bitmap == null) {
            bResult = false;
            //Log.d(TAG, "Bitmap is null");
            return bResult;
        }

        // Initialize
        cv = new ContentValues();
        whereArgs = new String[1];
        whereArgs[0] = new String();
        bResult = true;

        // Table Insert
        cv.put(Expense.ExpenseItReceiptColumns.IMAGE_DATA, bitmapToByteArray(bitmap));

        updateResult = db.update(Expense.ExpenseItReceiptColumns.TABLE_NAME,
                cv, Expense.ExpenseItReceiptColumns.ID + "=?", whereArgs);

        if (updateResult == 0) {
            bResult = false;
        }

        db.close();

        return bResult;
    }

    private byte[] bitmapToByteArray(Bitmap bm) {
        int iBytes = 0;
        ByteBuffer buffer = null;

        // Create the buffer with the correct size
        iBytes = bm.getWidth() * bm.getHeight() * 4;
        buffer = ByteBuffer.allocate(iBytes);

        // Copy to buffer and then into byte array
        bm.copyPixelsToBuffer(buffer);

        return buffer.array();
    }

}

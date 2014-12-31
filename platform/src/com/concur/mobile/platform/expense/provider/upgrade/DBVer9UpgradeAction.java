package com.concur.mobile.platform.expense.provider.upgrade;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * An extension of <code>SchemaUpgradeAction</code> for the purpose of adding version 9 columns to the receipt table.
 * 
 * @author andrewk
 */
public class DBVer9UpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "DBVer9UpgradeAction";

    /**
     * Constructs an instance of <code>DBVer9UpgradeAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public DBVer9UpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {

        boolean upgraded = true;
        try {
            // Add IMAGE_UPLOAD_TIME.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.IMAGE_UPLOAD_TIME)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.IMAGE_UPLOAD_TIME + " INTEGER");
            }
            // Add FILE_NAME.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.FILE_NAME)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.FILE_NAME + " TEXT");
            }
            // Add FILE_TYPE.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.FILE_TYPE)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.FILE_TYPE + " TEXT");
            }
            // Add SYSTEM_ORIGIN.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.SYSTEM_ORIGIN)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.SYSTEM_ORIGIN + " TEXT");
            }
            // Add IMAGE_ORIGIN.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.IMAGE_ORIGIN)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.IMAGE_ORIGIN + " TEXT");
            }
            // Add IMAGE_URL.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.IMAGE_URL)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.IMAGE_URL + " TEXT");
            }
            // Add THUMB_URL.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.THUMB_URL)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.THUMB_URL + " TEXT");
            }
            // Add OCR_IMAGE_ORIGIN.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.OCR_IMAGE_ORIGIN)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.OCR_IMAGE_ORIGIN + " TEXT");
            }
            // Add OCR_STAT_KEY.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.OCR_STAT_KEY)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.OCR_STAT_KEY + " TEXT");
            }
            // Add OCR_REJECT_CODE.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.OCR_REJECT_CODE)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.OCR_REJECT_CODE + " TEXT");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;

    }

}

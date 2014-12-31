/**
 * 
 */
package com.concur.mobile.core.data;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>SchemaUpgradeAction</code> for the purposes of upgrading the schema of the RECEIPT_SHARE table.
 * 
 * @author andy
 */
public class ReceiptShareSchemaUpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = ReceiptShareSchemaUpgradeAction.class.getSimpleName();

    /**
     * Constructs an instance of <code>ReceiptShareSchemaUpgradeAction</code> to perform a database upgrade on the RECEIPT_SHARE
     * table.
     * 
     * @param db
     *            contains a reference to the database being upgraded.
     * @param oldVersion
     *            contains the old database version.
     * @param newVersion
     *            contains the new database version.
     */
    protected ReceiptShareSchemaUpgradeAction(SQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.data.DBUpgradeAction#upgrade()
     */
    @Override
    protected boolean upgrade() {
        boolean retVal = true;
        if (oldVersion == 12 && newVersion == 13) {
            retVal = addStatusColumn();
        }
        return retVal;
    }

    /**
     * Will add a 'STATUS' column to the RECEIPT_SHARE table.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    private boolean addStatusColumn() {
        boolean retVal = true;
        try {
            if (!columnExists(MobileDatabaseHelper.TABLE_RECEIPT_SHARE, MobileDatabaseHelper.COLUMN_STATUS)) {
                // Add the 'STATUS' column.
                String addStatusSQL = "ALTER TABLE " + MobileDatabaseHelper.TABLE_RECEIPT_SHARE + " ADD COLUMN "
                        + MobileDatabaseHelper.COLUMN_STATUS + " TEXT";
                db.execSQL(addStatusSQL);
                // Set the value of 'STATUS' to 'PENDING' for all entries.
                String setStatusToPendingSQL = "UPDATE " + MobileDatabaseHelper.TABLE_RECEIPT_SHARE + " SET "
                        + MobileDatabaseHelper.COLUMN_STATUS + " = '" + ReceiptShareItem.Status.PENDING.getName() + "'";
                db.execSQL(setStatusToPendingSQL);
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".addStatusColumn: " + sqlExc.getMessage(), sqlExc);
            retVal = false;
        }
        return retVal;
    }

}

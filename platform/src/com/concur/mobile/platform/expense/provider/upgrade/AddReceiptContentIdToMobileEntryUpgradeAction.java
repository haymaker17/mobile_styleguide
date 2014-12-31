/**
 * 
 */
package com.concur.mobile.platform.expense.provider.upgrade;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * @author andrewk
 * 
 */
public class AddReceiptContentIdToMobileEntryUpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddReceiptContentURIToMobileEntryUpgradeAction";

    /**
     * Constructs an instance of <code>AddReceiptContentURIToMobileEntryUpgradeAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public AddReceiptContentIdToMobileEntryUpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.util.SchemaUpgradeAction#upgrade()
     */
    @Override
    public boolean upgrade() {
        boolean upgraded = true;
        try {
            // Add RECEIPT_CONTENT_ID.
            if (!columnExists(Expense.MobileEntryColumns.TABLE_NAME, Expense.MobileEntryColumns.RECEIPT_CONTENT_ID)) {
                db.execSQL("ALTER TABLE " + Expense.MobileEntryColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.MobileEntryColumns.RECEIPT_CONTENT_ID + " INTEGER DEFAULT NULL");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}

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
 * An extension of <code>SchemaUpgradeAction</code> for the purpose of adding the <code>IS_ATTACHED</code> and
 * <code>LAST_ACCESS_TIME</code> columns to the <code>RECEIPT_TABLE</code>.
 * 
 * @author andrewk
 */
public class AddIsAttachedLastAccessTimeToReceiptUpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddIsAttachedLastAccessTimeToReceiptUpgradeAction";

    /**
     * Constructs an instance of <code>AddIsAttachedLastAccessTimeToReceiptUpgradeAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public AddIsAttachedLastAccessTimeToReceiptUpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
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
            // Add IS_ATTACHED.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.IS_ATTACHED)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.IS_ATTACHED + " INTEGER DEFAULT 0");
            }
            // Add LAST_ACCESS_TIME.
            if (!columnExists(Expense.ReceiptColumns.TABLE_NAME, Expense.ReceiptColumns.LAST_ACCESS_TIME)) {
                db.execSQL("ALTER TABLE " + Expense.ReceiptColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.ReceiptColumns.LAST_ACCESS_TIME + " INTEGER");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}

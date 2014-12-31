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
 * An extension of <code>SchemaUpgradeAction</code> for the purpose of adding the 'PCA_KEY' to the 'SMART_EXPENSE' table.
 * 
 * @author andrewk
 */
public class AddPcaKeyToSmartExpenseUpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddPcaKeyToSmartExpenseUpgradeAction";

    /**
     * Constructs an instance of <code>AddPcaKeyToSmartExpenseUpgradeAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public AddPcaKeyToSmartExpenseUpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
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
            // Add PCA_KEY.
            if (!columnExists(Expense.SmartExpenseColumns.TABLE_NAME, Expense.SmartExpenseColumns.PCA_KEY)) {
                db.execSQL("ALTER TABLE " + Expense.SmartExpenseColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.SmartExpenseColumns.PCA_KEY + " TEXT");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}

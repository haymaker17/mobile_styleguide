/**
 * 
 */
package com.concur.mobile.platform.expense.provider.upgrade;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.ExpenseDBSchema;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * An extension of <code>SchemaUpgradeAction</code> for the purpose of adding expense list tables.
 * 
 * @author andrewk
 */
public class AddExpenseListTablesUpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddExpenseListTablesUpgradeAction";

    /**
     * Constructs an instance of <code>AddExpenseListTablesUpgradeAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public AddExpenseListTablesUpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
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
            // Add Corporate Card Transaction table.
            db.execSQL(ExpenseDBSchema.SCHEMA_CREATE_CORPORATE_CARD_TRANSACTION_TABLE);
            // Add Personal Card table.
            db.execSQL(ExpenseDBSchema.SCHEMA_CREATE_PERSONAL_CARD_TABLE);
            // Add Personal Card Transaction table.
            db.execSQL(ExpenseDBSchema.SCHEMA_CREATE_PERSONAL_CARD_TRANSACTION_TABLE);
            // Add Mobile Entry table.
            db.execSQL(ExpenseDBSchema.SCHEMA_CREATE_MOBILE_ENTRY_TABLE);
            // Add Receipt Capture table.
            db.execSQL(ExpenseDBSchema.SCHEMA_CREATE_RECEIPT_CAPTURE_TABLE);
            // Add expense table.
            db.execSQL(ExpenseDBSchema.SCHEMA_CREATE_EXPENSE_VIEW);
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}

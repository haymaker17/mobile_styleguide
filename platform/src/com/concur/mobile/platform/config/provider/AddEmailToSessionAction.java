package com.concur.mobile.platform.config.provider;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * An extension of <code>SchemaUpgradeAction</code> for the purposes of:<br>
 * <br>
 * 
 * Addition of <code>EMAIL</code> column to the <code>SESSION</code> table.
 */
public class AddEmailToSessionAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddEmailToSessionSchemaUpgrade";

    /**
     * Constructs an instance of <code>AddEmailLookUpToSessionAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public AddEmailToSessionAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {
        boolean upgraded = true;
        try {
            // Add EMAIL
            db.execSQL("ALTER TABLE " + Config.SessionColumns.TABLE_NAME + " ADD COLUMN " + Config.SessionColumns.EMAIL
                    + " TEXT");
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}

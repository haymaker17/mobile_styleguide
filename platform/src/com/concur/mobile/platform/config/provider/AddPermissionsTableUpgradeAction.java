package com.concur.mobile.platform.config.provider;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

public class AddPermissionsTableUpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddPermissionsTableUpgradeAction";

    /**
     * Constructs an instance of <code>AddPermissionsTableUpgradeAction</code>.
     * 
     * @param db contains a reference to the database.
     * @param oldVersion contains the old version.
     * @param newVersion contains the new version.
     */
    public AddPermissionsTableUpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {
        boolean upgraded = true;
        try {
            // Add Permissions Config table.
            db.execSQL(ConfigDBSchema.SCHEMA_CREATE_PERMISSIONS_TABLE);
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }
}

package com.concur.mobile.platform.config.provider;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

public class AddUserConfigV2SupportAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddUserConfigV2SupportSchemaUpgrade";

    /**
     * Constructs an instance of <code>AddTravelPointsConfigTableAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    protected AddUserConfigV2SupportAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {
        boolean upgraded = true;
        try {
            // Add Travel Points Config table.
            db.execSQL(ConfigDBSchema.SCHEMA_CREATE_TRAVEL_POINTS_CONFIG_TABLE);
            // Add SHOW_GDS_NAME_IN_SEARCH_RESULTS to User Config table.
            db.execSQL("ALTER TABLE " + Config.UserConfigColumns.TABLE_NAME + " ADD COLUMN "
                    + Config.UserConfigColumns.SHOW_GDS_NAME_IN_SEARCH_RESULTS + " INTEGER DEFAULT 0");
            // column data type modified, drop and then re-create attendee col def table
            db.execSQL(ConfigDBSchema.DROP_ATTENDEE_COLUMN_DEFINITION_TABLE);
            db.execSQL(ConfigDBSchema.SCHEMA_CREATE_ATTENDEE_COLUMN_DEFINITION_TABLE);
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}

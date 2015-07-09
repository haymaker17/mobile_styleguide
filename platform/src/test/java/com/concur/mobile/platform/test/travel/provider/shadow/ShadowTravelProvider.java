package com.concur.mobile.platform.test.travel.provider.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowContentProvider;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.platform.provider.ClearSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.travel.provider.ClearTravelDBHelper;
import com.concur.mobile.platform.travel.provider.TravelProvider;

/**
 * Provides a Robolectric shadow class for the TravelProvider. This shadow will provide an implemenation of
 * <code>TravelProvider.initPlatformSQLiteOpenHelper</code> that will install a non-encrypting travel DB helper object.
 * 
 * @author andrewk
 */
@Implements(value = TravelProvider.class, inheritImplementationMethods = true)
public class ShadowTravelProvider extends ShadowContentProvider {

    private static final String CLS_TAG = "ShadowTravelProvider";

    @Implementation
    public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".initPlatformSQLiteOpenHelper: configured clear travel db helper.");

        // This implementation will use an unencrypted database.
        PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearTravelDBHelper(context));
        return helper;
    }

}

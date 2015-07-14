package com.concur.mobile.platform.test.config.provider.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowContentProvider;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.platform.config.provider.ClearConfigDBHelper;
import com.concur.mobile.platform.config.provider.ConfigProvider;
import com.concur.mobile.platform.provider.ClearSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.test.Const;

/**
 * Provides a Robolectric shadow class for the ConfigProvider. This shadow will provide an implemenation of
 * <code>ConfigProvider.initPlatformSQLiteOpenHelper</code> that will install a non-encrypting config DB helper object.
 * 
 * @author andrewk
 */
@Implements(value = ConfigProvider.class, inheritImplementationMethods = true)
@Deprecated
public class ShadowConfigProvider extends ShadowContentProvider {

    private static final String CLS_TAG = "ShadowConfigProvider";

    @Implementation
    public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".initPlatformSQLiteOpenHelper: configured clear config db helper.");

        // This implementation will use an unencrypted database.
        PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearConfigDBHelper(context));
        return helper;
    }

}

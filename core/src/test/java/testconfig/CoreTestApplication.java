package testconfig;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;


/**
 * Created by Michael Becherer on 11-Sep-15.
 */
public class CoreTestApplication extends ConcurCore {

    private  String contentProviderAuthorityPrefix = "com.concur.mobile";
    private String accountTypePrefix = "com.concur.mobile";

    public CoreTestApplication() {
        appContext = this;
        setProviderAuthorities();
        setAccountNameForSyncAdapter();
    }

    //@Override
    //public void onCreate() {
    //super.onCreate();
    //RuntimeConfig.with(this).load();
    //}

    @Override
    protected void attachBaseContext(Context base) {
        try {
            super.attachBaseContext(base);
        } catch (RuntimeException ignored) {
            // Multidex support doesn't play well with Robolectric yet
        }
    }

    public void setProduct(String componentName) {
        product = ConcurCore.Product.CORPORATE;
    }

    public String getStringResourcePackageName() {
        return "com.concur.breeze";
    }

    @Override
    public String getGATrackingId() {
        return "UA-47360705-4";
    }

    @Override
    public void expireLogin() {

    }

    @Override
    public void expireLogin(boolean forceExpiration) {

    }

    @Override
    protected boolean bindProductService() {
        return bindService(new Intent(this, ConcurService.class), serviceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected String getServerAddress() {
        return "www.concursolutions.com";
    }

    @Override
    protected String getAuthorityPreFix() {
        String retVal = "";
        retVal = "com.concur.mobile";
        if (retVal != null && !retVal.isEmpty()) {
            contentProviderAuthorityPrefix = retVal;
        }
        return contentProviderAuthorityPrefix;
    }

    @Override
    protected String getAccountTypePrefix() {
        String retVal = "";
        retVal = "com.concur.mobile";
        if (retVal != null && !retVal.isEmpty()) {
            accountTypePrefix = retVal;
        }
        return accountTypePrefix;
    }

    @Override
    protected void initEventTracker() {
        return;
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return false;
    }
}
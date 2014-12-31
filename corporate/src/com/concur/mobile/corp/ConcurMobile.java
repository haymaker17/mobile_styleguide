package com.concur.mobile.corp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.corp.activity.Home;
import com.concur.mobile.corp.activity.Startup;

public class ConcurMobile extends ConcurCore {

    // private static final String CLS_TAG = ConcurMobile.class.getSimpleName();

    /**
     * Good ol' default constructor
     */
    public ConcurMobile() {
        appContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void setProduct(String componentName) {
        product = Product.CORPORATE;
    }

    public String getStringResourcePackageName() {
        return "com.concur.breeze";
    }

    /*
     * 
     */
	@Override
	public String getGATrackingId() {
		return getString(com.concur.breeze.R.string.ga_trackingId);
	}    

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.ConcurCore#initABTests()
     */
    @Override
    protected void initABTests() {
        // Initialize the A/B test framework based on any built tests for this release.
    }

    @Override
    protected boolean bindProductService() {
        return bindService(new Intent(this, ConcurService.class), serviceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void expireLogin() {
        Home.expireLogin();
    }
    
    @Override
    public void remoteWipe() {
        Home.remoteWipe();
    }

    @Override
    public View getPromoView(Context ctx) {
        View view = null;
        if (ViewUtil.isTravelUser(this)) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            view = inflater.inflate(com.concur.core.R.layout.whats_new_traveler_promo, null);
        }
        return view;
    }

    @Override
    protected String getServerAddress() {
        return "www.concursolutions.com";
    }

    @Override
    public void launchStartUpActivity(Activity activity) {
        Intent it = new Intent(activity, Startup.class);
        it.putExtra(FROM_NOTIFICATION, true);
        activity.startActivityForResult(it, START_UP_REQ_CODE);
    }

    @Override
    public void launchHome(Activity activity) {
        Intent it = new Intent(activity, Home.class);
        it.putExtra(FROM_NOTIFICATION, true);
        activity.startActivity(it);
        activity.finish();
    }

}

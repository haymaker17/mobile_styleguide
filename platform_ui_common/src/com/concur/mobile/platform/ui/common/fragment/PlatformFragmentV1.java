package com.concur.mobile.platform.ui.common.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

/**
 * An extension of <code>Fragment</code> providing some basic functionality.
 */
public abstract class PlatformFragmentV1 extends Fragment {

    // Different tag from the one used in BaseActivity
    protected static final String FRAGMENT_RETAINER_TAG = "fragment.retainer.fragment";

    // The one RetainerFragment used to hold objects between activity recreates
    protected RetainerFragmentV1 retainer;

    /**
     * Contains whether or not the building of the view is delayed until the service is available.
     */
    protected boolean buildViewDelay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRetainerFragment();
    }

    protected void initRetainerFragment() {
        FragmentManager fm = getActivity().getFragmentManager();

        retainer = (RetainerFragmentV1) fm.findFragmentByTag(FRAGMENT_RETAINER_TAG);
        if (retainer == null) {
            retainer = new RetainerFragmentV1();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(retainer, FRAGMENT_RETAINER_TAG);
            ft.commit();
        }
    }

    /**
     * Gets the instance of <code>RetainerFragment</code> used to store data.
     * 
     * @return returns the instance of <code>RetainerFragment</code> used to store data.
     */
    public RetainerFragmentV1 getRetainer() {
        return retainer;
    }

    /**
     * Return a resource ID of the title that will be used for this fragment where needed (e.g. page title bars).
     * 
     * @return An Integer holding the title resource ID or null if there should be no title
     */
    public Integer getTitleResource() {
        return null;
    }

    /**
     * Handles a notification that the Concur service is available.
     */
    public void onServiceAvailable() {
    }

    /**
     * Handles a notification that the Concur service is unavailable.
     */
    public void onServiceUnavailable() {
    }
}

/**
 * 
 */
package com.concur.mobile.core.test.ab;

import java.util.Map;

import android.view.View;

/**
 * An interface that will perform an A/B test set-up.
 */
public interface ABTestSetup {

    /**
     * Will load a view that is specific to a particular test.
     * 
     * @param testID
     *            contains the test ID.
     * @return returns an instance of <code>View</code> that has been loaded for the test.
     */
    public abstract View loadView(String testID);

    /**
     * Will alter an existing view specific to a test.
     * 
     * @param testID
     *            contains the test ID.
     * @param view
     *            contains the view to be altered.
     */
    public abstract void alterView(String testID, View view);

    /**
     * Adds any test specific Flurry parameters.
     * 
     * @param testID
     *            contains the test ID.
     * @param params
     *            contains the Flurry param map.
     */
    public abstract void addFlurryParams(String testID, Map<String, String> params);

}

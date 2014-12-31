package com.concur.mobile.core.test.ab;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.concur.mobile.core.util.Const;

/**
 * Provides a factory class for constructing A/B tests.
 */
public class ABTestFactory {

    private static final String CLS_TAG = "ABTestFactory";

    /**
     * Contains the map from test ID to an object implementing <code>ABTestSetup</code>.
     */
    private static Map<String, Class<? extends ABTestSetup>> testIDClassMap;

    /**
     * Contains a map of built tests by ID.
     */
    private static Map<String, ABTestSetup> testIDSetupMap;

    /**
     * Initializes the test factory with a map of test ID's to class objects implementing the test. <br>
     * <b>NOTE:</b><br>
     * Classes implementing <code>ABTestSetup</code> should contain a no argument constructor. <br>
     * 
     * @param testMap
     */
    public static void init(Map<String, Class<? extends ABTestSetup>> testMap) {
        ABTestFactory.testIDClassMap = testMap;
        testIDSetupMap = new HashMap<String, ABTestSetup>();
    }

    /**
     * Will build and cache an instance of <code>ABTestSetup</code> based on a test ID.
     * 
     * @param testID
     *            contains the test ID.
     * @return returns an instance of <code>ABTestSetup</code> based on <code>testID</code>. If the map passed in the
     *         <code>ABTestFactory.init</code> call does not contain an entry for <code>testID</code>
     */
    public static ABTestSetup buildTest(String testID) {
        return buildTest(testID, true);
    }

    /**
     * Will build an instance of <code>ABTestSetup</code> based on a test ID.
     * 
     * @param testID
     *            contains the test ID.
     * @param cacheTest
     *            contains whether or not to cache the test internally.
     * @return returns an instance of <code>ABTestSetup</code> based on <code>testID</code>. If the map passed in the
     *         <code>ABTestFactory.init</code> call does not contain an entry for <code>testID</code>
     */
    public static ABTestSetup buildTest(String testID, boolean cacheTest) {
        ABTestSetup testSetup = null;
        if (testIDClassMap != null && testIDClassMap.containsKey(testID)) {
            Class<? extends ABTestSetup> setupCls = testIDClassMap.get(testID);
            if (setupCls != null) {
                try {
                    testSetup = setupCls.newInstance();
                    if (cacheTest) {
                        testIDSetupMap.put(testID, testSetup);
                    }
                } catch (IllegalAccessException illAccExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildTest: unable access no-args constructor!", illAccExc);
                } catch (InstantiationException instExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildTest: missing no-args or exception thrown!", instExc);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildTest: null class object for test ID!");
            }
        }
        return testSetup;
    }

    /**
     * Will cache <code>testSetup</code> internally by key <code>testID</code>.
     * 
     * @param testID
     *            contains the test ID.
     * @param testSetup
     *            contains the instance of <code>TestSetup</code> to be cached.
     */
    public static void cacheTest(String testID, ABTestSetup testSetup) {
        testIDSetupMap.put(testID, testSetup);
    }

    /**
     * Gets an instance of <code>ABTestSetup</code> based on a test ID. <br>
     * <b>NOTE:</b><br>
     * This method may returned a previously built test with the same ID. If clients must have a newly built test setup object, a
     * call to <code>ABTestSetup.buildTest</code> should be made.
     * 
     * @param testID
     *            contains the test ID.
     * @return returns an instance of <code>ABTestSetup</code> based on <code>testID</code> if it has been built; otherwise,
     *         returns <code>null</code>.
     */
    public static ABTestSetup getTest(String testID) {
        ABTestSetup testSetup = null;
        if (testIDSetupMap != null) {
            if (testIDSetupMap.containsKey(testID)) {
                testSetup = testIDSetupMap.get(testID);
            }
        }
        return testSetup;
    }

}

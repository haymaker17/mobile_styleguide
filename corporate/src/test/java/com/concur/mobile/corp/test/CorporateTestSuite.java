package com.concur.mobile.corp.test;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.concur.mobile.base.util.Const;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

@Config(application = CorporateTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CorpTestRunner.class)
public abstract class CorporateTestSuite {

    protected static Boolean testActivities = true;
    private static String CLS_TAG = CorporateTestSuite.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    protected static <T extends Activity> void testActivity(Class<T> activityClass) {
        Log.v(Const.LOG_TAG, CLS_TAG + ".testActivity current Activity: ===" + activityClass.getSimpleName() + "===");
        Intent newIt = new Intent();

        ActivityController<T> expenseController = Robolectric.buildActivity(activityClass)
            .withIntent(newIt)
            .create().start().resume().visible();

        Activity runningActivity = expenseController.get();
        expenseController.destroy();
    }
}

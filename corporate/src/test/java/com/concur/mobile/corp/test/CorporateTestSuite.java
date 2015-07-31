package com.concur.mobile.corp.test;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.concur.mobile.base.util.Const;
import com.concur.mobile.core.expense.charge.activity.ExpenseItDetailActivity;
import com.concur.mobile.corp.activity.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

import java.util.Arrays;
import java.util.List;

@Config(application = CorporateTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CorpTestRunner.class)
public class CorporateTestSuite {

    private static String CLS_TAG = CorporateTestSuite.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testExpenseItListBackgroundRefresh() {
        List<Class<? extends Activity>> activities = Arrays.asList(
            SimpleWebViewActivity.class
            , Register.class
            , PreLogin.class
            , Login.class
            , Home.class
            //, ExpenseItDetailActivity.class
            , EmailLookupActivity.class
        );

        for (Class<? extends Activity> item : activities) {
            testActivity(item);
        }
    }

    private static <T extends Activity> void testActivity(Class<T> activityClass) {
        Log.v(Const.LOG_TAG, CLS_TAG + ".testActivity current Activity: ===" + activityClass.getSimpleName() + "===");
        Intent newIt = new Intent();

        ActivityController<T> expenseController = Robolectric.buildActivity(activityClass)
            .withIntent(newIt)
            .create().start().resume().visible();

        Activity runningActivity = expenseController.get();
        expenseController.destroy();
    }
}

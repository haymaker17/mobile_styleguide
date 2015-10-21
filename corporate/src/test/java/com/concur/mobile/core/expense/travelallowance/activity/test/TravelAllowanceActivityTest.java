package com.concur.mobile.core.expense.travelallowance.activity.test;

import android.app.Activity;
import android.content.Intent;

import com.concur.mobile.core.expense.travelallowance.activity.TravelAllowanceActivity;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.corp.test.CorpTestRunner;
import com.concur.mobile.corp.test.CorporateTestApplication;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 21.10.2015.
 */
@Config(application = CorporateTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CorpTestRunner.class)
public class TravelAllowanceActivityTest extends TestCase{

    @Test
    public void createTest() {
    Activity activity =  Robolectric.buildActivity(TravelAllowanceActivity.class)
            .create()
            .get();

        assertNotNull(activity);
    }

    @Test
    public void lifecycleTest(){
        Activity activity =  Robolectric.buildActivity(TravelAllowanceActivity.class)
                .create()
                .start()
                .resume()
                .get();

        assertNotNull(activity);
    }

    @Test
    public void lifecycleWithIntent(){
        Intent  intent = new Intent();
        intent.putExtra(BundleId.EXPENSE_REPORT_KEY, "ExpRepKey");
        intent.putExtra(BundleId.IS_EDIT_MODE, true);

        Activity activity = Robolectric.buildActivity(TravelAllowanceActivity.class)
                .withIntent(intent)
                .create()
                .get();

        assertNotNull(activity);
    }

}

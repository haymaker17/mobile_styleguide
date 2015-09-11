package com.concur.mobile.core.expense.travelallowance.adapter.test;

import com.concur.mobile.core.expense.travelallowance.adapter.FixedTravelAllowanceListAdapter;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

/**
 * Created by D028778 on 09-Sep-15.
 */
@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class FixedTravelAllowanceListAdapterTest extends TestCase {

    @Test
    public void constructorTest(){
        FixedTravelAllowanceListAdapter adapter =
                new FixedTravelAllowanceListAdapter(RuntimeEnvironment.application);
        Assert.assertNotNull(adapter);
    }
}

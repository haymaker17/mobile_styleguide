package com.concur.mobile.core.expense.travelallowance.adapter.test;

import android.support.v7.app.AppCompatActivity;


import com.concur.mobile.core.expense.travelallowance.adapter.ViewPagerAdapter;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D049515 on 14.07.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class ViewPagerAdapterTest extends TestCase {

    private ViewPagerAdapter adapter;

    @Before
    public void setup() {
        AppCompatActivity activity = new AppCompatActivity();
        this.adapter = new ViewPagerAdapter(activity.getSupportFragmentManager(), activity, null);
    }

    @Test
    public void test() {
        //TODO: Remove dummy test case as soon as the first real test case is in here.
        assertTrue(false);
    }

}

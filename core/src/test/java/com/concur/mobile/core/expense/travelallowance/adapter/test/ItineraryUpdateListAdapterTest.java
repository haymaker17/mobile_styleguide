package com.concur.mobile.core.expense.travelallowance.adapter.test;

import android.app.Activity;
import android.view.View;

import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryUpdateListAdapter;

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
public class ItineraryUpdateListAdapterTest extends TestCase {

    private ItineraryUpdateListAdapter adapter;

    private View.OnClickListener onLocationClickListener;
    private View.OnClickListener onDateClickListener;
    private View.OnClickListener onTimeClickListener;


    @Before
    public void setup() {
        //TODO: Implement the listener
        onLocationClickListener = null;
        onDateClickListener = null;
        onTimeClickListener = null;
        this.adapter = new ItineraryUpdateListAdapter(new Activity(), onLocationClickListener, onDateClickListener,
                onTimeClickListener);

    }

    @Test
    public void test() {
        //TODO: Remove dummy test case as soon as the first real test case is in here.
        assertTrue(false);
    }
}

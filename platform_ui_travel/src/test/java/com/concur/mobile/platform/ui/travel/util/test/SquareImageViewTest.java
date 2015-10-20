package com.concur.mobile.platform.ui.travel.util.test;

import android.app.Activity;
import android.view.View;

import com.concur.mobile.platform.ui.travel.BuildConfig;
import com.concur.mobile.platform.ui.travel.util.SquareImageView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SquareImageViewTest {

    @Test
    public void testOnMeasure() throws Exception {

        Activity context = new Activity();
        SquareImageView view = new SquareImageView(context);

        // Manually measure the view at 200x100.
        view.measure(View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY));

        // Verify that it correctly resized.
        assertEquals(200, view.getMeasuredWidth());
        assertEquals((int) (200 * 0.6), view.getMeasuredHeight());


    }


}
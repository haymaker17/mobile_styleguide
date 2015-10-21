package com.concur.mobile.platform.ui.travel.util.test;

import android.app.Activity;

import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.RecyclingImageView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Created by tejoa on 05/10/2015.
 */
@RunWith(RobolectricTestRunner.class)
public class RecyclingImageViewTest {

    @Test
    public void setImageDrawable() throws Exception {
        Activity context = new Activity();
        RecyclingImageView recyclingImageView = new RecyclingImageView(context);

        recyclingImageView.setImageResource(R.drawable.hotel_results_default_image);

        recyclingImageView.getDrawable();
        //assertEquals(recyclingImageView.getI);

    }


}
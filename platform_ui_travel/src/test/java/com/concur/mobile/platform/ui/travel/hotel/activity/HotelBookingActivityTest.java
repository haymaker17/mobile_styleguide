package com.concur.mobile.platform.ui.travel.hotel.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.widget.TextView;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.travel.search.hotel.URLInfo;
import com.concur.mobile.platform.ui.travel.BuildConfig;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by RatanK on 25/07/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, manifest = "src/main/AndroidManifest.xml")
public class HotelBookingActivityTest {

    HotelBookingActivity hotelBookingActivity;
    private int numOfNights = 2;

    @Before public void setup() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        HotelRate hotelRate = new HotelRate();
        hotelRate.amount = 125.50;
        URLInfo sellOptionsURL = new URLInfo();
        sellOptionsURL.href = "";
        hotelRate.sellOptions = sellOptionsURL;
        intent.putExtra("roomSelected", hotelRate);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, numOfNights);
        intent.putExtra("hotelName", "Hotel Test");

        // will create a new instance with the intent and call through the life cycle to onCreate().
        hotelBookingActivity = Robolectric.buildActivity(HotelBookingActivity.class).withIntent(intent).create().get();
    }

    @Test public void testHotelBookingActivityNull() {
        Assert.assertNotNull(hotelBookingActivity);
    }

    @Test public void testInitView() {
        ActionBar actionBar = hotelBookingActivity.getActionBar();
        Assert.assertEquals("Hotel Test", actionBar.getTitle());
    }

    @Test public void testFooterReserve_labelText() {
        final String expected = hotelBookingActivity.getString(R.string.hotel_reserve_this_room);
        TextView reserveTxtView = (TextView) hotelBookingActivity.findViewById(R.id.footer_reserve);
        final String actual = reserveTxtView.getText().toString();
        Assert.assertEquals(expected, actual);
    }

    @Test public void testNumberOfNights_singularLabelText() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        HotelRate hotelRate = new HotelRate();
        hotelRate.amount = 125.50;
        URLInfo sellOptionsURL = new URLInfo();
        sellOptionsURL.href = "";
        hotelRate.sellOptions = sellOptionsURL;
        intent.putExtra("roomSelected", hotelRate);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, 1);
        intent.putExtra("hotelName", "Hotel Test");

        // will create a new instance with the intent and call through the life cycle to onCreate().
        HotelBookingActivity act = Robolectric.buildActivity(HotelBookingActivity.class).withIntent(intent).create().get();
        String expectedStr = Format
                .localizeText(act.getApplicationContext(), R.string.hotel_reserve_num_of_night,
                        1);
        TextView numOfNightsTxtView = (TextView) act.findViewById(R.id.hotel_room_night);
        final String actual = numOfNightsTxtView.getText().toString();
        Assert.assertEquals(expectedStr, actual);
    }

    @Test public void testNumberOfNights_pluralLabelText() {
        String expectedStr = Format
                .localizeText(hotelBookingActivity.getApplicationContext(), R.string.hotel_reserve_num_of_nights,
                        numOfNights);
        TextView numOfNightsTxtView = (TextView) hotelBookingActivity.findViewById(R.id.hotel_room_night);
        final String actual = numOfNightsTxtView.getText().toString();
        Assert.assertEquals(expectedStr, actual);
    }
}

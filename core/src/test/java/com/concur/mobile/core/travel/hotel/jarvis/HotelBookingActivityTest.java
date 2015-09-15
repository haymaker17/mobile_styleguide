//package com.concur.mobile.core.travel.hotel.jarvis;
//
//import android.app.ActionBar;
//import android.content.Intent;
//import android.widget.TextView;
//
//import com.concur.core.BuildConfig;
//import com.concur.core.R;
//import com.concur.mobile.base.util.Format;
//import com.concur.mobile.core.travel.hotel.jarvis.activity.HotelBookingActivity;
//import com.concur.mobile.platform.travel.search.hotel.HotelRate;
//import com.concur.mobile.platform.travel.search.hotel.URLInfo;
//import com.concur.mobile.platform.ui.travel.util.Const;
//
//import junit.framework.Assert;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.Robolectric;
//import org.robolectric.annotation.Config;
//
//import testconfig.ActivityDouble;
//import testconfig.RoboTestRunner;
//
///**
// * Created by RatanK on 25/07/2015.
// */
//@RunWith(RoboTestRunner.class)
//@Config(constants = BuildConfig.class, manifest = "AndroidManifest.xml", sdk = 21)
//public class HotelBookingActivityTest {
//
//    HotelBookingActivity hotelBookingActivity;
//    private int numOfNights = 2;
//
//    @Before
//    @Config(shadows = {ActivityDouble.class})
//    public void setup() {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        HotelRate hotelRate = new HotelRate();
//        hotelRate.amount = 125.50;
//        URLInfo sellOptionsURL = new URLInfo();
//        sellOptionsURL.href = "";
//        hotelRate.sellOptions = sellOptionsURL;
//        intent.putExtra("roomSelected", hotelRate);
//        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, numOfNights);
//        intent.putExtra("hotelName", "Hotel Test");
//
//        // will create a new instance with the intent and call through the life cycle to onCreate().
//        try {
//            hotelBookingActivity = (HotelBookingActivity) Robolectric.buildActivity(HotelBookingActivity.class).withIntent(intent).create().get();
//        } catch (Exception ex) {
//            System.err.println("An InvocationTargetException was caught!");
//            Throwable cause = ex.getCause();
//            System.out.format("Invocation of %s failed because of: %s%n",
//                    this, cause.getMessage());
//        }
//
//        // hotelBookingActivity = new HotelBookingActivity();
//        //hotelBookingActivity.onCreate(null);
//    }
//
//    @Test
//    public void testHotelBookingActivityNull() {
//        Assert.assertNotNull(hotelBookingActivity);
//    }
//
//    @Test
//    public void testInitView() {
//        ActionBar actionBar = hotelBookingActivity.getActionBar();
//        Assert.assertEquals("Hotel Test", actionBar.getTitle());
//    }
//
//    @Test
//    public void testFooterReserve_labelText() {
//        final String expected = hotelBookingActivity.getString(R.string.hotel_reserve_this_room);
//        TextView reserveTxtView = (TextView) hotelBookingActivity.findViewById(R.id.footer_reserve);
//        final String actual = reserveTxtView.getText().toString();
//        Assert.assertEquals(expected, actual);
//    }
//
//    @Test
//    public void testNumberOfNights_singularLabelText() {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        HotelRate hotelRate = new HotelRate();
//        hotelRate.amount = 125.50;
//        URLInfo sellOptionsURL = new URLInfo();
//        sellOptionsURL.href = "";
//        hotelRate.sellOptions = sellOptionsURL;
//        intent.putExtra("roomSelected", hotelRate);
//        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, 1);
//        intent.putExtra("hotelName", "Hotel Test");
//        HotelBookingActivity act = null;
//        // will create a new instance with the intent and call through the life cycle to onCreate().
//        try {
//            act = Robolectric.buildActivity(HotelBookingActivity.class).withIntent(intent).create().get();
//        } catch (Exception ex) {
//            System.err.println("An InvocationTargetException was caught!");
//            Throwable cause = ex.getCause();
//            System.out.format("Invocation of %s failed because of: %s%n",
//                    this, cause.getMessage());
//        }
//        String expectedStr = Format
//                .localizeText(act.getApplicationContext(), R.string.hotel_reserve_num_of_night,
//                        1);
//        TextView numOfNightsTxtView = (TextView) act.findViewById(R.id.hotel_room_night);
//        final String actual = numOfNightsTxtView.getText().toString();
//        Assert.assertEquals(expectedStr, actual);
//    }
//
//    @Test
//    public void testNumberOfNights_pluralLabelText() {
//        String expectedStr = Format
//                .localizeText(hotelBookingActivity.getApplicationContext(), R.string.hotel_reserve_num_of_nights,
//                        numOfNights);
//        TextView numOfNightsTxtView = (TextView) hotelBookingActivity.findViewById(R.id.hotel_room_night);
//        final String actual = numOfNightsTxtView.getText().toString();
//        Assert.assertEquals(expectedStr, actual);
//    }
//}

/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>BaseActivity</code> to display a no hotel search results page.
 */
public class HotelSearchNoResults extends BaseActivity implements View.OnClickListener {

    private static final String CLS_TAG = HotelSearchNoResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_search_no_results);

        // Set the screen title.
        getSupportActionBar().setTitle(R.string.hotel_search_no_results_title);

        // Set the travel header.
        setHotelSearchTravelHeader(getIntent());
    }

    protected void setHotelSearchTravelHeader(Intent intent) {

        // Set the travel header name.
        TextView txtView = (TextView) findViewById(R.id.travel_name);
        if (txtView != null) {
            String locationStr = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
            if (locationStr == null) {
                locationStr = "";
            }
            txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                    R.string.hotel_search_travel_header_name, locationStr));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setHotelSearchTravelHeader: unable to locate travel_name text view!");
        }

        // Set the travel header date span.
        txtView = (TextView) findViewById(R.id.date_span);
        if (txtView != null) {
            Calendar checkInCal = (Calendar) intent
                    .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
            String checkInStr = "";
            if (checkInCal != null) {
                checkInStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, checkInCal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setHotelSearchTravelHeader: missing check-in calendar object!");
            }
            Calendar checkOutCal = (Calendar) intent
                    .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);
            String checkOutStr = "";
            if (checkOutCal != null) {
                checkOutStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, checkOutCal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setHotelSearchTravelHeader: missing check-in calendar object!");
            }
            txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                    R.string.hotel_search_travel_header_date_span, checkInStr, checkOutStr));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setHotelSearchTravelHeader: unable to locate date_span text view!");
        }

    }

    public void onClick(View arg0) {
        setResult(Const.RESULT_NEW_SEARCH);
        finish();
    }

}

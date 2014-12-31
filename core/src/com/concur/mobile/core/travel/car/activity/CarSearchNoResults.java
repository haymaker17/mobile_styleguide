/**
 * 
 */
package com.concur.mobile.core.travel.car.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.hotel.activity.HotelSearchNoResults;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>BaseActivity</code> for displaying a screen indicating no results.
 * 
 **/
public class CarSearchNoResults extends BaseActivity {

    private static final String CLS_TAG = HotelSearchNoResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_search_no_results);

        // Set the screen title.
        getSupportActionBar().setTitle(R.string.car_search_progress_no_cars_title);

        // Set the travel header.
        setCarSearchTravelHeader(getIntent());
    }

    protected void setCarSearchTravelHeader(Intent intent) {

        // Set the travel header name.
        TextView txtView = (TextView) findViewById(R.id.travel_name);
        if (txtView != null) {
            String locationStr = intent.getStringExtra(Const.EXTRA_TRAVEL_LOCATION);
            if (locationStr == null) {
                locationStr = "";
            }
            txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                    R.string.car_search_travel_header_name, locationStr));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCarSearchTravelHeader: unable to locate travel_name text view!");
        }

        // Set the travel header date span.
        txtView = (TextView) findViewById(R.id.date_span);
        if (txtView != null) {
            Calendar pickUpDateTime = (Calendar) intent
                    .getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR);
            String pickUpStr = "";
            if (pickUpDateTime != null) {
                pickUpStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, pickUpDateTime);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setCarSearchTravelHeader: missing pick-up calendar object!");
            }
            Calendar dropOffDateTime = (Calendar) intent
                    .getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR);
            String dropOffStr = "";
            if (dropOffDateTime != null) {
                dropOffStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, dropOffDateTime);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setCarSearchTravelHeader: missing check-in calendar object!");
            }
            txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                    R.string.car_search_travel_header_date_span, pickUpStr, dropOffStr));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCarSearchTravelHeader: unable to locate date_span text view!");
        }

    }

    public void onClick(View arg0) {
        setResult(Const.RESULT_NEW_SEARCH);
        finish();
    }

}

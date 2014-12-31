/**
 * 
 */
package com.concur.mobile.core.travel.rail.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>BaseActivity<code> to display a "no results"
 * message.
 */
public class RailSearchNoResults extends BaseActivity {

    private static final String CLS_TAG = RailSearchNoResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rail_search_no_results);

        // Set the screen title.
        getSupportActionBar().setTitle(R.string.rail_search_progress_no_trains_title);

        // Set the travel header.
        setRailSearchTravelHeader(getIntent());
    }

    protected void setRailSearchTravelHeader(Intent intent) {

        // Set the travel header name.
        TextView txtView = (TextView) findViewById(R.id.travel_name);
        if (txtView != null) {
            String fromCity = "";
            Bundle fromBundle = intent.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
            if (fromBundle != null) {
                RailStation fromStation = new RailStation(fromBundle);
                fromCity = fromStation.city;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setRailSearchTravelHeader: intent missing from location!");
            }
            String toCity = "";
            Bundle toBundle = intent.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);
            if (toBundle != null) {
                RailStation toStation = new RailStation(toBundle);
                toCity = toStation.city;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setRailSearchTravelHeader: intent missing to location!");
            }
            txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                    R.string.rail_search_travel_header_city_span, fromCity, toCity));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setRailSearchTravelHeader: unable to locate travel_name text view!");
        }

        // Set the travel header date span.
        txtView = (TextView) findViewById(R.id.date_span);
        if (txtView != null) {
            Calendar departDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);
            String departDateTimeStr = "";
            if (departDateTime != null) {
                departDateTimeStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA,
                        departDateTime);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setRailSearchTravelHeader: missing pick-up calendar object!");
            }
            Calendar returnDateTime = (Calendar) intent.getSerializableExtra(RailSearch.RET_DATETIME);
            if (returnDateTime != null) {
                String returnDateTimeStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA,
                        returnDateTime);
                txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.rail_search_travel_header_date_span, departDateTimeStr, returnDateTimeStr));
            } else {
                txtView.setText(departDateTimeStr);
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setRailSearchTravelHeader: unable to locate date_span text view!");
        }

    }

    public void onClick(View arg0) {
        setResult(Const.RESULT_NEW_SEARCH);
        finish();
    }

}

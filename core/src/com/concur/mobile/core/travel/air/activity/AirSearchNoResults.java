package com.concur.mobile.core.travel.air.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.air.activity.AirSearch.SearchMode;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

public class AirSearchNoResults extends BaseActivity implements View.OnClickListener {

    private static final String CLS_TAG = AirSearchNoResults.class.getSimpleName();

    protected SearchMode searchMode;

    protected LocationChoice departLocation;
    protected LocationChoice arriveLocation;

    protected Calendar departDateTime;
    protected Calendar returnDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_search_no_results);

        getSupportActionBar().setTitle(R.string.air_search_no_results_title);

        initValues();
        initTravelHeader();
    }

    private void initValues() {
        Intent i = getIntent();

        // Set the search mode.
        searchMode = SearchMode.None;
        String mode = i.getStringExtra(Const.EXTRA_SEARCH_MODE);
        if (mode != null) {
            searchMode = SearchMode.valueOf(mode);
        }

        // Set the depart/arrive location information.
        final Bundle departLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        final Bundle arriveLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);
        departLocation = new LocationChoice(departLocBundle);
        arriveLocation = new LocationChoice(arriveLocBundle);
        departDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);
        if (searchMode != SearchMode.OneWay) {
            returnDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_RETURN);
        }
    }

    public void onClick(View arg0) {
        setResult(Const.RESULT_NEW_SEARCH);
        finish();
    }

    /**
     * Will initialize the travel header with from/to information.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initTravelHeader() {
        final String departIATACode = departLocation.getIATACode();
        final String arriveIATACode = arriveLocation.getIATACode();

        // The travel header
        TextView tv = (TextView) findViewById(R.id.travel_name);
        tv.setText(Format.localizeText(this, R.string.segmentlist_air_fromto, new Object[] { departIATACode,
                arriveIATACode }));

        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(departDateTime.getTime()));
        if (searchMode != SearchMode.OneWay) {
            sb.append(" - ").append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(returnDateTime.getTime()));
        }
        tv = (TextView) findViewById(R.id.date_span);
        tv.setText(sb.toString());
    }

}

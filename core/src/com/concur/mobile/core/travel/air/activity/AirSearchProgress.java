package com.concur.mobile.core.travel.air.activity;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.air.activity.AirSearch.SearchMode;
import com.concur.mobile.core.travel.air.service.AirSearchReply;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

public class AirSearchProgress extends BaseActivity {

    protected Intent resultsIntent;

    protected SearchMode searchMode;

    protected LocationChoice departLocation;
    protected LocationChoice arriveLocation;

    protected Calendar departDateTime;
    protected Calendar returnDateTime;

    protected String cabinClass;
    protected boolean refundableOnly;

    private static final String CLS_TAG = AirSearchProgress.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.air_search_progress);

        initValues();
        initUI();
        startSearch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case AirSearch.RESULTS_ACTIVITY_CODE:
            setResult(resultCode, data);
            break;
        }
        finish();
    }

    protected void initValues() {
        Intent i = getIntent();

        // Setup our future intents while we are here
        resultsIntent = new Intent(this, AirSearchResultsSummary.class);

        if (i.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, i.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }

        searchMode = SearchMode.None;
        String mode = i.getStringExtra(Const.EXTRA_SEARCH_MODE);
        if (mode != null) {
            searchMode = SearchMode.valueOf(mode);
        }
        resultsIntent.putExtra(Const.EXTRA_SEARCH_MODE, searchMode.name());

        final Bundle departLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        final Bundle arriveLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);

        departLocation = new LocationChoice(departLocBundle);
        arriveLocation = new LocationChoice(arriveLocBundle);
        departDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);

        cabinClass = i.getStringExtra(Const.EXTRA_SEARCH_CABIN_CLASS);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_CABIN_CLASS, cabinClass);

        refundableOnly = i.getBooleanExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, false);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, refundableOnly);

        resultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocBundle);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocBundle);
        resultsIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);

        if (searchMode != SearchMode.OneWay) {
            returnDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_RETURN);
            resultsIntent.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateTime);
        }

    }

    protected void initUI() {
        // The header
        getSupportActionBar().setTitle(R.string.air_search_title);

        // MOB-21681 - set user's preferred time zone
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);

        final String departIATACode = departLocation.getIATACode();
        TextView tv = (TextView) findViewById(R.id.searchDepartValue);
        tv.setText(departIATACode);

        StringBuffer dt = new StringBuffer(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, departDateTime));
        dt.append(", ").append(Format.safeFormatCalendar(timeFormat, departDateTime));
        tv = (TextView) findViewById(R.id.searchDepartingDateTime);
        tv.setText(dt.toString());

        final String arriveIATACode = arriveLocation.getIATACode();
        if (searchMode == SearchMode.OneWay) {
            View v = findViewById(R.id.searchReturningLoc);
            v.setVisibility(View.GONE);
            v = findViewById(R.id.searchReturningDateTime);
            v.setVisibility(View.GONE);
        } else {
            tv = (TextView) findViewById(R.id.searchReturnValue);
            tv.setText(arriveIATACode);

            dt = new StringBuffer(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, returnDateTime));
            dt.append(", ").append(Format.safeFormatCalendar(timeFormat, returnDateTime));
            tv = (TextView) findViewById(R.id.searchReturningDateTime);
            tv.setText(dt.toString());
        }

        // The travel header
        tv = (TextView) findViewById(R.id.travel_name);
        tv.setText(com.concur.mobile.base.util.Format.localizeText(this, R.string.segmentlist_air_fromto, new Object[] {
                departIATACode, arriveIATACode }));

        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(departDateTime.getTime()));
        if (searchMode != SearchMode.OneWay) {
            sb.append(" - ").append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(returnDateTime.getTime()));
        }
        tv = (TextView) findViewById(R.id.date_span);
        tv.setText(sb.toString());

    }

    protected final IntentFilter airResultsFilter = new IntentFilter(Const.ACTION_AIR_SEARCH_RESULTS);

    protected final BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent i) {
            unregisterReceiver(this);

            String status = i.getStringExtra(Const.REPLY_STATUS);

            if (status != null && status.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                final ConcurCore app = (ConcurCore) getApplication();
                final AirSearchReply reply = app.getAirSearchResults();
                int resultCount = reply.getResultCount();
                if (resultCount > 0) {
                    setSearchResultsIntent();
                } else {
                    setNoResultsIntent();
                }
                startActivityForResult(resultsIntent, AirSearch.RESULTS_ACTIVITY_CODE);
            }
        }

    };

    protected void startSearch() {
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            // Make the call
            ConcurService svc = ((ConcurCore) getApplication()).getService();
            if (svc != null) {
                registerReceiver(receiver, airResultsFilter);
                svc.searchForFlights(departLocation.getIATACode(), arriveLocation.getIATACode(), departDateTime,
                        returnDateTime, cabinClass, refundableOnly);
            }
        }
    }

    @Override
    protected void updateOfflineHeaderBar(boolean available) {
        super.updateOfflineHeaderBar(available);
        if (!available) {
            // cancel task;
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateOfflineHeaderBar: offline mode detect!");
            resultsIntent = new Intent();
            // MOB-14907
            final Bundle departLocBundle = getIntent().getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
            final Bundle arriveLocBundle = getIntent().getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);
            resultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocBundle);
            resultsIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocBundle);
            resultsIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);
            resultsIntent.putExtra(Const.EXTRA_SEARCH_MODE, searchMode.name());
            if (searchMode != SearchMode.OneWay) {
                resultsIntent.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateTime);
            }
            resultsIntent.setClass(AirSearchProgress.this, AirSearchNoResults.class);
            // Launch the no results activity.
            startActivity(resultsIntent);
            // finish.
            finish();
        }
    }

    protected void setNoResultsIntent() {
        resultsIntent.setClass(AirSearchProgress.this, AirSearchNoResults.class);
    }

    protected void setSearchResultsIntent() {
        resultsIntent.setClass(AirSearchProgress.this, AirSearchResultsSummary.class);
    }
}

package com.concur.mobile.core.travel.air.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.air.fragment.AirPriceToBeatSearchResultsFragment;
import com.concur.mobile.core.travel.air.service.GetAirBenchmarks;
import com.concur.mobile.core.travel.data.Benchmark;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.platform.util.Format;

/**
 * Activity to launch the Air Price to Beat Search Results
 * 
 * @author RatanK
 * 
 */
public class AirPriceToBeatSearchResults extends TravelBaseActivity {

    private static final String CLS_TAG = AirPriceToBeatSearchResults.class.getSimpleName();

    private static final String SEARCH_DETAILS_FRAGMENT = "search.details.fragment";

    private AirPriceToBeatSearchResultsFragment detailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.air_price_to_beat_main);

        FragmentManager fm = getSupportFragmentManager();
        detailsFragment = (AirPriceToBeatSearchResultsFragment) fm.findFragmentByTag(SEARCH_DETAILS_FRAGMENT);
        if (detailsFragment == null) {
            detailsFragment = new AirPriceToBeatSearchResultsFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.airPriceToBeatContainer, detailsFragment, SEARCH_DETAILS_FRAGMENT);
            ft.commit();
        }

        initFragments();

    }

    private void initFragments() {

        Intent i = getIntent();

        Bundle bundle = i.getExtras();

        final Bundle departLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        final Bundle arriveLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);

        LocationChoice departLocation = new LocationChoice(departLocBundle);
        LocationChoice arriveLocation = new LocationChoice(arriveLocBundle);

        Calendar departDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);

        detailsFragment.setHeaderTxt(i.getStringExtra(Const.EXTRA_PRICE_TO_BEAT_HEADER_TXT));

        detailsFragment.setDepartLocationName(departLocation.getName());

        detailsFragment.setArriveLocationName(arriveLocation.getName());

        String dateStr = Format
                .safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, departDateTime);
        detailsFragment.setDepartDateStr(dateStr);

        detailsFragment.setSearchModeTxt(i.getStringExtra(Const.EXTRA_AIR_PRICE_TO_BEAT_ROUND_TRIP));

        // set the list items
        List<Benchmark> benchmarks = (List<Benchmark>) bundle.getSerializable(GetAirBenchmarks.AIR_BENCHMARKS);
        if (benchmarks != null && benchmarks.size() > 0) {
            List<AirBenchmarkListItem> benchmarkListItems = new ArrayList<AirBenchmarkListItem>(benchmarks.size());
            // As MWS do not send the Location as display name and at present mobile support only 1 Air Price to Beat,
            // so use the display name
            for (Benchmark bm : benchmarks) {
                benchmarkListItems.add(new AirBenchmarkListItem(bm, arriveLocation.getName()));
            }

            ListItemAdapter<AirBenchmarkListItem> listItemAdapater = new ListItemAdapter<AirBenchmarkListItem>(this,
                    benchmarkListItems);
            detailsFragment.setListItemAdapter(listItemAdapater);
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".initFragments: benchmarks is null");
        }
    }
}

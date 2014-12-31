package com.concur.mobile.core.travel.hotel.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.concur.core.R;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.HotelBenchmark;
import com.concur.mobile.core.travel.hotel.fragment.HotelPriceToBeatSearchResultsFragment;
import com.concur.mobile.core.travel.hotel.service.GetHotelBenchmarks;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ListItemAdapter;

public class HotelPriceToBeatSearchResults extends TravelBaseActivity {

    private HotelPriceToBeatSearchResultsFragment detailsFragment;

    private static final String SEARCH_DETAILS_FRAGMENT = "search.details.fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hotel_price_to_beat_main);

        FragmentManager fm = getSupportFragmentManager();
        detailsFragment = (HotelPriceToBeatSearchResultsFragment) fm.findFragmentByTag(SEARCH_DETAILS_FRAGMENT);
        if (detailsFragment == null) {
            detailsFragment = new HotelPriceToBeatSearchResultsFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.hotelPriceToBeatContainer, detailsFragment, SEARCH_DETAILS_FRAGMENT);
            ft.commit();
        }

        initFragments();

    }

    private void initFragments() {
        Intent i = getIntent();

        Bundle bundle = i.getExtras();

        detailsFragment.setHeaderTxt(i.getStringExtra(Const.EXTRA_PRICE_TO_BEAT_HEADER_TXT));

        String location = bundle.getString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
        detailsFragment.setLocation(location);

        String monthOfStay = bundle.getString(Const.EXTRA_HOTEL_PRICE_TO_BEAT_MONTH_OF_STAY);
        detailsFragment.setMonthOfStay(monthOfStay);

        String distanceWithUnits = bundle.getString(Const.EXTRA_HOTEL_PRICE_TO_BEAT_DIST_WITH_UNITS);
        detailsFragment.setDistance(distanceWithUnits);

        // set the list items
        List<HotelBenchmark> benchmarks = (List<HotelBenchmark>) bundle
                .getSerializable(GetHotelBenchmarks.HOTEL_BENCHMARKS);
        if (benchmarks != null && benchmarks.size() > 0) {
            List<HotelBenchmarkListItem> benchmarkListItems = new ArrayList<HotelBenchmarkListItem>(benchmarks.size());
            for (HotelBenchmark bm : benchmarks) {
                benchmarkListItems.add(new HotelBenchmarkListItem(bm));
            }

            ListItemAdapter<HotelBenchmarkListItem> listItemAdapater = new ListItemAdapter<HotelBenchmarkListItem>(
                    this, benchmarkListItems);
            detailsFragment.setListItemAdapter(listItemAdapater);
        }
    }
}

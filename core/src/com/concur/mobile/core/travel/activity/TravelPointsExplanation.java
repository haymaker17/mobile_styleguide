package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.data.HotelBenchmark;
import com.concur.mobile.core.travel.hotel.activity.HotelBenchmarkListItem;
import com.concur.mobile.core.travel.hotel.fragment.HotelPriceToBeatSearchResultsFragment;
import com.concur.mobile.core.view.ListItemAdapter;

public class TravelPointsExplanation extends BaseActivity {

    private HotelPriceToBeatSearchResultsFragment detailsFragment;

    private static final String SEARCH_DETAILS_FRAGMENT = "search.details.fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityStarted = getIntent();

        int titleResId = activityStarted.getIntExtra(TravelBaseActivity.EXTRA_TITLE_RESOURCE_ID_KEY,
                R.string.segment_travel_points_price_to_beat);
        int contentResId = activityStarted.getIntExtra(TravelBaseActivity.EXTRA_CONTENT_RESOURCE_ID_KEY,
                R.string.travel_points_p2b_explanation_desc);

        if (activityStarted.getBooleanExtra(TravelBaseActivity.EXTRA_SHOW_PRICE_TO_BEAT_KEY, false)) {
            setContentView(R.layout.hotel_price_to_beat_main);

            // initialize the fragment
            initFragments(titleResId, contentResId,
                    activityStarted.getStringExtra(TravelBaseActivity.EXTRA_FORMATTED_MIN_PRICE_TO_BEAT_KEY),
                    activityStarted.getStringExtra(TravelBaseActivity.EXTRA_FORMATTED_MAX_PRICE_TO_BEAT_KEY));
        } else {
            setContentView(R.layout.travel_points_description);

            // set the header
            getSupportActionBar().setTitle(titleResId);

            // set the content
            TextView contentView = (TextView) findViewById(R.id.content);
            contentView.setText(contentResId);
        }

        // TODO - log flurry event here
    }

    private void initFragments(int titleResId, int contentResId, String formattedMinBenchmarkPrice,
            String formattedMaxBenchmarkPrice) {
        // create the fragment
        FragmentManager fm = getSupportFragmentManager();
        detailsFragment = (HotelPriceToBeatSearchResultsFragment) fm.findFragmentByTag(SEARCH_DETAILS_FRAGMENT);
        if (detailsFragment == null) {
            detailsFragment = new HotelPriceToBeatSearchResultsFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.hotelPriceToBeatContainer, detailsFragment, SEARCH_DETAILS_FRAGMENT);
            ft.commit();
        }

        // set the flag to switch the layouts in the fragment
        detailsFragment.setShowingPriceToBeatListExplanation(true);

        // set the text for title
        detailsFragment.setTitleStr(getText(titleResId).toString());

        // set the text for header
        boolean hasMinPriceToBeat = false;
        boolean hasMaxPriceToBeat = false;
        // show the price to beat header text only in case we have a value for price to beat
        if (!TextUtils.isEmpty(formattedMinBenchmarkPrice)) {
            hasMinPriceToBeat = true;
        }

        if (!TextUtils.isEmpty(formattedMaxBenchmarkPrice)) {
            hasMaxPriceToBeat = true;
        }

        String priceToBeatHdrText = null;
        if (hasMinPriceToBeat && hasMaxPriceToBeat) {
            // show the price to beat range values
            StringBuilder formattedMinPriceToBeatColored = new StringBuilder("<b><font color=#0000FF>");
            formattedMinPriceToBeatColored.append(formattedMinBenchmarkPrice).append("</font></b>");

            StringBuilder formattedMaxPriceToBeatColored = new StringBuilder("<b><font color=#0000FF>");
            formattedMaxPriceToBeatColored.append(formattedMaxBenchmarkPrice).append("</font></b>");

            priceToBeatHdrText = Format.localizeText(ConcurCore.getContext(), contentResId, new Object[] {
                    formattedMinPriceToBeatColored, formattedMaxPriceToBeatColored });

            detailsFragment.setHeaderTxt(priceToBeatHdrText);
        } else {
            String formattedPriceToBeat = formattedMinBenchmarkPrice == null ? formattedMaxBenchmarkPrice
                    : formattedMinBenchmarkPrice;
            if (formattedPriceToBeat != null) {
                StringBuilder formattedPriceToBeatColored = new StringBuilder("<b><font color=#1f4272>");
                formattedPriceToBeatColored.append(formattedPriceToBeat).append("</font></b>");
                priceToBeatHdrText = Format.localizeText(ConcurCore.getContext(), contentResId,
                        new Object[] { formattedPriceToBeatColored });
                detailsFragment.setHeaderTxt(priceToBeatHdrText);
            }
        }

        // set the list items
        ConcurCore core = (ConcurCore) getApplication();
        List<HotelBenchmark> benchmarks = core.getHotelSearchResults().hotelBenchmarks;
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
package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;

public class HotelSearchResultFilterFragment extends PlatformFragmentV1 {

    // private View mainView;
    private HotelSearchResultsFilterListener callBackListener;
    private String nameToFilter;
    private String starRatingtoFilter;
    private Double distanceToFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        final View mainView = inflater.inflate(R.layout.hotel_search_result_filter_fragment, container, false);

        // hotel star rating filter
        RadioButton button = (RadioButton) mainView.findViewById(R.id.filter_all_stars_button);
        button.setChecked(true);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                starRatingtoFilter = null;
            }
        });
        button = (RadioButton) mainView.findViewById(R.id.filter_3_star_button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                starRatingtoFilter = "3";
            }
        });
        button = (RadioButton) mainView.findViewById(R.id.filter_4_star_button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                starRatingtoFilter = "4";
            }
        });
        button = (RadioButton) mainView.findViewById(R.id.filter_5_star_button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                starRatingtoFilter = "5";
            }
        });

        // hotel distance filter
        button = (RadioButton) mainView.findViewById(R.id.filter_distance_all_button);
        button.setChecked(true);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                distanceToFilter = null;
            }
        });
        button = (RadioButton) mainView.findViewById(R.id.filter_distance_5_button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                distanceToFilter = new Double(5);
            }
        });
        button = (RadioButton) mainView.findViewById(R.id.filter_distance_15_button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                distanceToFilter = new Double(15);
            }
        });
        button = (RadioButton) mainView.findViewById(R.id.filter_distance_25_button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                distanceToFilter = new Double(25);
            }
        });

        mainView.findViewById(R.id.filterNow).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final int id = v.getId();
                if (id == R.id.filterNow) {
                    // hotel name containing filter
                    EditText editTxtView = (EditText) mainView.findViewById(R.id.hotelNameToSearchId);
                    if (editTxtView != null && editTxtView.getText() != null) {
                        nameToFilter = editTxtView.getText().toString().trim();
                    }
                    callBackListener.filterResults(starRatingtoFilter, distanceToFilter, nameToFilter);
                }
            }
        });

        return mainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelSearchResultsFilterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchResultsFilterListener");
        }

    }

    // @Override
    // public void onPause() {
    // super.onPause();
    //
    // if (baseActivity != null) {
    // RetainerFragment retainer = baseActivity.getRetainer();
    // if (retainer != null) {
    // // Store the listener
    // if (listener != null) {
    // retainer.put(ON_CLICK_LISTENER_KEY, listener);
    // }
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: retainer is null!");
    // }
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: baseActivity is null!");
    // }
    // }
    //
    // @Override
    // public void onResume() {
    // super.onResume();
    // restoreReceivers();
    // }

    // Container Activity must implement this call back interface
    public interface HotelSearchResultsFilterListener {

        public void filterResults(String starRating, Double distance, String nameContaining);
    }
}

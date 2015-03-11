package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.mobile.platform.travel.search.hotel.HotelPropertyId;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.util.Format;

/**
 * 
 * 
 * @author RatanK
 * 
 */
public class HotelSearchResultFragment extends PlatformFragmentV1 {

    public static final String CLS_TAG = HotelSearchResultFragment.class.getSimpleName();

    public String location;
    public String durationOfStayForDisplayInHeader;

    private boolean progressbarVisible;
    private ListView hotelListView;
    private View mainView;

    public ListView getHotelListView() {
        return hotelListView;
    }

    HotelSearchResultsFragmentListener callBackListener;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.hotel_search_results_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        if (item.getItemId() == R.id.map) {
            callBackListener.onMapsClicked();
            return true;
        } else if (item.getItemId() == R.id.voice) {
            Toast.makeText(getActivity().getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        mainView = inflater.inflate(R.layout.hotel_search_result_fragment, container, false);

        hotelListView = (ListView) mainView.findViewById(R.id.search_result_list_view);

        setHasOptionsMenu(true);

        // set the header with location and duration of stay
        View headerView = mainView.findViewById(R.id.result_header);
        ((TextView) headerView.findViewById(R.id.locationText)).setText(location);
        ((TextView) headerView.findViewById(R.id.dateText)).setText(durationOfStayForDisplayInHeader);
        headerView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                callBackListener.onHeaderClicked();
            }
        });

        showProgressBar(false);

        setActionBar();

        callBackListener.fragmentReady();

        return mainView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        Log.d(Const.LOG_TAG, " ***** HotelSearchResultFragment, in onSaveInstanceState *****  ");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        Log.d(Const.LOG_TAG, " ***** HotelSearchResultFragment, in onPause *****  ");

        // retainer.put(STATE_HOTEL_LIST_ITEMS_KEY, hotelListItems);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // if (retainer.contains(STATE_HOTEL_LIST_ITEMS_KEY)) {
        // hotelListItems = (List<HotelSearchResultListItem>) retainer.get(STATE_HOTEL_LIST_ITEMS_KEY);
        // }

        // Log.d(Const.LOG_TAG, " ***** HotelSearchResultFragment, in onResume *****  hotelListItems = "
        // + (hotelListItems != null ? hotelListItems.size() : 0));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelSearchResultsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchResultsFragmentListener");
        }

    }

    private void setActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(R.string.hotel_search_resutls_title);

    }

    public void updateUI(ListItemAdapter<HotelSearchResultListItem> listItemAdapater, int nuomOfHotels,
            String toastMessage) {
        if (nuomOfHotels > 0) {
            hotelListView.setAdapter(listItemAdapater);
            showNumberOfResultsInFooter(nuomOfHotels);
        }
        if (toastMessage != null) {
            Toast.makeText(getActivity().getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    public void showProgressBar(boolean isRates) {
        if (!progressbarVisible) {
            View progressBar = mainView.findViewById(R.id.hotel_search_progress);
            progressbarVisible = true;
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            View progressBarMsg = mainView.findViewById(R.id.hotel_search_progress_msg);
            if (isRates) {
                ((TextView) progressBarMsg).setText(R.string.hotel_rates_progress_message);
            }
            progressBarMsg.setVisibility(View.VISIBLE);
            progressBarMsg.bringToFront();
        }
    }

    public void hideProgressBar() {
        if (progressbarVisible) {
            View progressBar = mainView.findViewById(R.id.hotel_search_progress);
            progressbarVisible = false;
            progressBar.setVisibility(View.GONE);
            View progressBarMsg = mainView.findViewById(R.id.hotel_search_progress_msg);
            progressBarMsg.setVisibility(View.GONE);
        }
    }

    public void showNegativeView() {
        hideProgressBar();
        View emptyResultView = mainView.findViewById(R.id.search_no_result_view);
        emptyResultView.setVisibility(View.VISIBLE);
        emptyResultView.bringToFront();
    }

    public void hideNegativeView() {
        View emptyResultView = mainView.findViewById(R.id.search_no_result_view);
        emptyResultView.setVisibility(View.GONE);
    }

    private void showNumberOfResultsInFooter(int resultsSize) {
        View footerView = mainView.findViewById(R.id.results_footer);
        if (footerView != null) {
            ((TextView) footerView.findViewById(R.id.results_size)).setText(Format.localizeText(getActivity()
                    .getApplicationContext(), R.string.hotel_search_resutls_size, resultsSize));
            footerView.setVisibility(View.VISIBLE);
        }
    }

    public void showSortAndFilterIconsInFooter() {

        View footerView = mainView.findViewById(R.id.results_footer);
        if (footerView != null) {
            // show the sort icon and add sort options
            View view = footerView.findViewById(R.id.sort);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }

            // show the filter icon and add filter items
            view = footerView.findViewById(R.id.filter);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // show the filter dialog fragment
                        callBackListener.onFilterClicked();
                    }
                });
            }
        }
    }

    // private

    public void refreshUIListItems(List<HotelSearchResultListItem> hotelListItems,
            ListItemAdapter<HotelSearchResultListItem> listItemAdapater) {

        if (hotelListItems != null && hotelListItems.size() > 0) {
            hotelListView.setAdapter(listItemAdapater);
            Log.d(Const.LOG_TAG, " ***** .refreshUIListItem updated hotelListItems = " + hotelListItems.size());
            for (HotelSearchResultListItem hotelListItem : hotelListItems) {
                refreshView(hotelListItem, listItemAdapater);
            }
        }

    }

    /**
     * Will refresh each visible view contained in a list view that match on <code>HotelChoice.propertyId</code>.
     * 
     * @param propertyId
     *            contains the propertyId that should be refreshed.
     */
    public void refreshView(HotelSearchResultListItem updatedHotelListItem,
            ListItemAdapter<HotelSearchResultListItem> listItemAdapater) {
        int start = hotelListView.getFirstVisiblePosition();
        for (int i = start, j = hotelListView.getLastVisiblePosition(); i <= j; i++) {
            HotelSearchResultListItem listItem = listItemAdapater.getItem(i);
            // NOTE: Need to check for 'listItem' not being null as the last visible position within the list
            // could be a list footer, which accounts for a visible position, but not reflecting any data
            // within the adapter.

            if (listItem != null && listItem.getHotel() != null) {
                // listItem.getHotel().getPropertyIds().contains(updatedHotelListItem.getHotel().getPropertyIds())) {
                boolean refresh = false;
                for (HotelPropertyId hotelPropertyId1 : listItem.getHotel().propertyIds) {
                    for (HotelPropertyId hotelPropertyId2 : updatedHotelListItem.getHotel().propertyIds) {
                        if (hotelPropertyId1.propertyId.equals(hotelPropertyId2.propertyId)) {
                            refresh = true;
                            break;
                        }
                    }
                    if (refresh) {
                        // no need to check for other porperty ids
                        break;
                    }
                }

                if (refresh) {
                    listItem.getHotel().lowestRate = updatedHotelListItem.getHotel().lowestRate;
                    listItem.getHotel().currencyCode = updatedHotelListItem.getHotel().currencyCode;
                    listItem.getHotel().distanceUnit = updatedHotelListItem.getHotel().distanceUnit;

                    View view = hotelListView.getChildAt(i - start);
                    listItemAdapater.getView(i, view, hotelListView);

                    // TODO - remove this log statement
                    Log.d(Const.LOG_TAG, " *** trying to refresh " + listItem.getHotel().name);

                    break;
                }
            }
        }
    }

    // Container Activity must implement this call back interface
    public interface HotelSearchResultsFragmentListener {

        public void fragmentReady();

        public void onHeaderClicked();

        public void onFilterClicked();

        public void onMapsClicked();

    }
}

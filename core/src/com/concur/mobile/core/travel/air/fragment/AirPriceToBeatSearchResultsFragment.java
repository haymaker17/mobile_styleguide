package com.concur.mobile.core.travel.air.fragment;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.travel.air.activity.AirBenchmarkListItem;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;
import com.concur.mobile.core.view.ListItemAdapter;

/**
 * Fragment to display the Air Price to Beat search results
 * 
 * @author RatanK
 * 
 */
public class AirPriceToBeatSearchResultsFragment extends BaseFragment {

    private String departLocationName;
    private String arriveLocationName;
    private String departDateStr;

    private ListItemAdapter<AirBenchmarkListItem> listItemAdapater;

    private String headerTxt;
    private String searchModeTxt;

    public void setHeaderTxt(String headerTxt) {
        this.headerTxt = headerTxt;
    }

    public void setSearchModeTxt(String searchModeTxt) {
        this.searchModeTxt = searchModeTxt;
    }

    public void setDepartLocationName(String departLocationName) {
        this.departLocationName = departLocationName;
    }

    public void setArriveLocationName(String arriveLocationName) {
        this.arriveLocationName = arriveLocationName;
    }

    public void setDepartDateStr(String departDateStr) {
        this.departDateStr = departDateStr;
    }

    public void setListItemAdapter(ListItemAdapter<AirBenchmarkListItem> listItemAdapater) {
        this.listItemAdapater = listItemAdapater;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        View view = inflater.inflate(R.layout.air_price_to_beat_result_details_fragment, container, false);

        // set up the title
        getBaseActivity().getSupportActionBar().setTitle(R.string.general_search_results);

        TextView txtView = (TextView) view.findViewById(R.id.details_text_view);
        txtView.setText(headerTxt);

        // styles for label and values
        TextAppearanceSpan labelStyle = new TextAppearanceSpan(getActivity(), R.style.ListCellSubHeaderTextBold);
        TextAppearanceSpan valueStyle = new TextAppearanceSpan(getActivity(), R.style.SubHeaderText);
        StyleableSpannableStringBuilder spanStrBldr = null;

        // departure
        TextView txtViewDepart = (TextView) view.findViewById(R.id.details_text_departloc);
        String departLabel = (String) getText(R.string.air_price_to_beat_search_departure);
        spanStrBldr = styleLabelAndValue(spanStrBldr, labelStyle, valueStyle, departLabel, departLocationName);
        txtViewDepart.setText(spanStrBldr);

        // arrival
        TextView txtViewArrive = (TextView) view.findViewById(R.id.details_text_arriveloc);
        String arriveLabel = (String) getText(R.string.air_price_to_beat_search_arrival);
        spanStrBldr = styleLabelAndValue(spanStrBldr, labelStyle, valueStyle, arriveLabel, arriveLocationName);
        txtViewArrive.setText(spanStrBldr);

        // departure date
        TextView txtViewDepartDate = (TextView) view.findViewById(R.id.details_text_departDate);
        String departDateLabel = (String) getText(R.string.air_price_to_beat_search_departure_date);
        spanStrBldr = styleLabelAndValue(spanStrBldr, labelStyle, valueStyle, departDateLabel, departDateStr);
        txtViewDepartDate.setText(spanStrBldr);

        // label for the list view
        TextView txtViewLabel = (TextView) view.findViewById(R.id.details_text_price_to_beat_label);
        txtViewLabel.setText(searchModeTxt);

        // set the list item adapter
        ListView benchmarksList = (ListView) view.findViewById(R.id.price_to_beat_list_view);
        benchmarksList.setAdapter(listItemAdapater);

        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TRAVEL_DEPARTURE_LOC, departLocationName);
        params.put(Flurry.PARAM_NAME_TRAVEL_ARRIVAL_LOC, arriveLocationName);
        params.put(Flurry.PARAM_NAME_TRAVEL_DEPARTURE_DATE, departDateStr);

        EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                Flurry.EVENT_NAME_VIEWED_AIR_PRICE_TO_BEAT_RESULTS, params);

        return view;
    }

    private StyleableSpannableStringBuilder styleLabelAndValue(StyleableSpannableStringBuilder spanStrBldr,
            TextAppearanceSpan labelStyle, TextAppearanceSpan valueStyle, String labelStr, String valueStr) {
        spanStrBldr = new StyleableSpannableStringBuilder();
        spanStrBldr.appendWithStyle(labelStyle, labelStr);
        spanStrBldr.append(' ');
        spanStrBldr.appendWithStyle(valueStyle, valueStr);
        return spanStrBldr;
    }
}

package com.concur.mobile.core.travel.hotel.fragment;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.text.Html;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.travel.hotel.activity.HotelBenchmarkListItem;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.StyleableSpannableStringBuilder;
import com.concur.mobile.core.view.ListItemAdapter;

/**
 * Fragment to display the Hotel Price to Beat search results
 * 
 * @author RatanK
 * 
 */
public class HotelPriceToBeatSearchResultsFragment extends BaseFragment {

    private String location;
    private String monthOfStay;
    private String distance;
    private String titleStr;
    private boolean showingPriceToBeatListExplanation;

    private ListItemAdapter<HotelBenchmarkListItem> listItemAdapater;

    private String headerTxt;

    public void setHeaderTxt(String headerTxt) {
        this.headerTxt = headerTxt;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMonthOfStay(String monthOfStay) {
        this.monthOfStay = monthOfStay;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setListItemAdapter(ListItemAdapter<HotelBenchmarkListItem> listItemAdapater) {
        this.listItemAdapater = listItemAdapater;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = null;
        if (isShowingPriceToBeatListExplanation()) {
            // view for Price-to-Beat explanation for the range of values in the Hotel search
            view = onPriceToBeatExplanationCreateView(inflater, container);
        } else {
            // view for Price-to-Beat generator results
            view = onPriceToBeatGeneratorCreateView(inflater, container);
        }

        // label for the list view
        TextView txtViewLabel = (TextView) view.findViewById(R.id.details_text_price_to_beat_label);
        txtViewLabel.setVisibility(View.VISIBLE);
        StringBuilder sb = new StringBuilder();
        sb.append(getText(R.string.home_action_hotel_price_to_beat));
        sb.append(" (");
        sb.append(getText(R.string.hotel_price_per_night));
        sb.append(")");
        txtViewLabel.setText(sb);

        // set the list item adapter
        ListView benchmarksList = (ListView) view.findViewById(R.id.price_to_beat_list_view);
        benchmarksList.setVisibility(View.VISIBLE);
        benchmarksList.setAdapter(listItemAdapater);

        return view;
    }

    protected View onPriceToBeatGeneratorCreateView(LayoutInflater inflater, ViewGroup container) {
        // inflate the details fragment
        View view = inflater.inflate(R.layout.hotel_price_to_beat_result_details_fragment, container, false);

        // set up the title
        getBaseActivity().getSupportActionBar().setTitle(R.string.general_search_results);

        TextView txtView = (TextView) view.findViewById(R.id.details_text_view);
        txtView.setText(headerTxt);

        // styles for label and values
        TextAppearanceSpan labelStyle = new TextAppearanceSpan(getActivity(), R.style.ListCellSubHeaderTextBold);
        TextAppearanceSpan valueStyle = new TextAppearanceSpan(getActivity(), R.style.SubHeaderText);
        StyleableSpannableStringBuilder spanStrBldr = null;

        // Location
        TextView txtViewLoc = (TextView) view.findViewById(R.id.details_text_loc);
        String locLabel = (String) getText(R.string.hotel_search_label_location);
        spanStrBldr = styleLabelAndValue(spanStrBldr, labelStyle, valueStyle, locLabel, location);
        txtViewLoc.setText(spanStrBldr);

        // Month of stay
        TextView txtViewMonthOfStay = (TextView) view.findViewById(R.id.details_text_month_of_stay);
        String monthOfStayLabel = (String) getText(R.string.hotel_price_to_beat_search_month_of_stay);
        spanStrBldr = styleLabelAndValue(spanStrBldr, labelStyle, valueStyle, monthOfStayLabel, monthOfStay);
        txtViewMonthOfStay.setText(spanStrBldr);

        // Distance
        TextView txtViewDistance = (TextView) view.findViewById(R.id.details_text_distance);
        String distanceLabel = (String) getText(R.string.general_distance);
        spanStrBldr = styleLabelAndValue(spanStrBldr, labelStyle, valueStyle, distanceLabel, distance);
        txtViewDistance.setText(spanStrBldr);

        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_LOCATION, location);
        params.put(Flurry.PARAM_NAME_RADIUS, distance);
        params.put(Flurry.PARAM_NAME_MONTH, monthOfStay);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                Flurry.EVENT_NAME_VIEWED_HOTEL_PRICE_TO_BEAT_RESULTS, params);

        return view;
    }

    protected View onPriceToBeatExplanationCreateView(LayoutInflater inflater, ViewGroup container) {
        // inflate the details fragment
        View view = inflater.inflate(R.layout.travel_points_description, container, false);

        // set up the title
        getBaseActivity().getSupportActionBar().setTitle(getTitleStr());

        TextView txtView = (TextView) view.findViewById(R.id.content);
        txtView.setText(Html.fromHtml(headerTxt));

        return view;
    }

    private StyleableSpannableStringBuilder styleLabelAndValue(StyleableSpannableStringBuilder spanStrBldr,
            TextAppearanceSpan labelStyle, TextAppearanceSpan valueStyle, String labelStr, String valueStr) {
        spanStrBldr = new StyleableSpannableStringBuilder();
        spanStrBldr.appendWithStyle(labelStyle, labelStr + ":");
        spanStrBldr.append(' ');
        spanStrBldr.appendWithStyle(valueStyle, valueStr);
        return spanStrBldr;
    }

    public boolean isShowingPriceToBeatListExplanation() {
        return showingPriceToBeatListExplanation;
    }

    public void setShowingPriceToBeatListExplanation(boolean showingPriceToBeatListExplanation) {
        this.showingPriceToBeatListExplanation = showingPriceToBeatListExplanation;
    }

    public String getTitleStr() {
        return titleStr;
    }

    public void setTitleStr(String titleStr) {
        this.titleStr = titleStr;
    }
}

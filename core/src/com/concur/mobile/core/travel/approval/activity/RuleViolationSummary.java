package com.concur.mobile.core.travel.approval.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.data.RuleViolation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;

/**
 * Activity for showing the trip rule violation summary
 * 
 * @author RatanK
 * 
 */
public class RuleViolationSummary extends BaseActivity {

    public static final String CLS_TAG = RuleViolationSummary.class.getSimpleName();

    /** list item type for rule violation */
    private static final int RULE_VIOLATION_LIST_ITEM_VIEW_TYPE = 0;
    /** list item type for header */
    private static final int HEADER_VIEW_TYPE = 1;

    protected ListItemAdapter<ListItem> listItemAdapter;

    String itinLocator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.trip_rule_violation);

        // set header
        getSupportActionBar().setTitle(getHeaderTitle());

        buildView();

        // Flurry Notification.
        EventTracker.INSTANCE.track(Flurry.CATEGORY_TRIP_APPROVAL,
                Flurry.EVENT_NAME_VIEWED_TRIP_RULE_VIOLATION_SUMMARY);
    }

    protected String getHeaderTitle() {
        return getText(R.string.trip_rule_violation_summary).toString();
    }

    protected void buildView() {
        Intent intent = getIntent();

        // get the approval header related details
        String travellerName = intent.getStringExtra(Const.EXTRA_TRAVELLER_NAME);
        String tripName = intent.getStringExtra(Const.EXTRA_TRIP_NAME);
        String totalTripCost = intent.getStringExtra(Const.EXTRA_TOTAL_TRIP_COST);
        String message = intent.getStringExtra(Const.EXTRA_TRIP_APPROVAL_MESSAGE);
        itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);

        // initialize trip approve related header
        initTripApprovalHeader(travellerName, tripName, totalTripCost, message);

        // configure the rule violation list
        configureListItems();
    }

    protected void initTripApprovalHeader(final String travellerName, final String tripName,
            final String totalTripCost, final String message) {
        View tripHeaderView = findViewById(R.id.trip_approver_header);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_employee_name)).setText(travellerName);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_cost_amount)).setText(totalTripCost);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_trip_name)).setText(tripName);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_message)).setText(message);
    }

    protected void configureListItems() {
        ListItemAdapter<ListItem> listItemAdapter = new ListItemAdapter<ListItem>(this, getListItems());
        ListView listView = (ListView) findViewById(R.id.trip_rule_violations_list);
        if (listView != null) {
            listView.setAdapter(listItemAdapter);
        }
    }

    /**
     * Retrieve the trip rule violations from the app object
     * 
     * @return
     */
    protected List<RuleViolation> getTripRuleViolations() {
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        return core.getTripRuleViolations();
    }

    protected List<ListItem> getListItems() {
        List<ListItem> listItems = new ArrayList<ListItem>();
        // get the rule violations
        List<RuleViolation> ruleViolations = getTripRuleViolations();
        String headerStr = null;
        for (RuleViolation ruleViolation : ruleViolations) {
            switch (ruleViolation.getSegmentType()) {
            case Itinerary:
                headerStr = getText(R.string.trip_rule_violation_itinerary).toString();
                break;
            case Air:
                headerStr = getText(R.string.trip_rule_violation_flight).toString();
                break;
            case Car:
                headerStr = getText(R.string.trip_rule_violation_car).toString();
                break;
            case Hotel:
                headerStr = getText(R.string.trip_rule_violation_hotel).toString();
                break;
            case Rail:
                headerStr = getText(R.string.trip_rule_violation_rail).toString();
                break;
            default:
                // unknown segment type
                break;
            }
            if (headerStr != null) {
                RuleHeaderListItem hdrListItem = new RuleHeaderListItem(headerStr, HEADER_VIEW_TYPE);
                // HeaderListItem hdrListItem = new HeaderListItem(headerStr, HEADER_VIEW_TYPE);
                listItems.add(hdrListItem);
                listItems.add(new RuleViolationListItem(ruleViolation, RULE_VIOLATION_LIST_ITEM_VIEW_TYPE));
            }
        }
        return listItems;
    }

    // custom header list item class to change the font of the header text
    class RuleHeaderListItem extends HeaderListItem {

        public RuleHeaderListItem(String header, int listItemViewType) {
            super(header, listItemViewType);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.expense.ListItem#buildView(android.content.Context, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View buildView(Context context, View convertView, ViewGroup parent) {
            View ruleHeaderView = super.buildView(context, convertView, parent);
            TextView txtView = (TextView) ruleHeaderView.findViewById(R.id.list_section_header);
            txtView.setTextAppearance(context, R.style.ListCellHeaderText);
            return ruleHeaderView;
        }
    }
}

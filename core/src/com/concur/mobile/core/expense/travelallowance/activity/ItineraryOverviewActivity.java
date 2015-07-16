package com.concur.mobile.core.expense.travelallowance.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryOverviewListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.util.Const;

/**
 * Created by Michael Becherer on 16-Jul-15.
 */
public class ItineraryOverviewActivity extends BaseActivity {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = ItineraryOverviewActivity.class
            .getSimpleName();

    /**
     * The reference to the Itinerary Controller holding the list of itineraries
     */
    private TravelAllowanceItineraryController itineraryController;

    /**
     * The list adapter
     */
    private ItineraryOverviewListAdapter adapter;

    /**
     * The expense report key this activity is dealing with
     */
    private String expenseReportKey;

    /**
     * The expense report name this activity is dealing with
     */
    private String expenseReportName;

    /**
     * The layout this activity is dealing with
     */
    private static final int LAYOUT_ID = R.layout.ta_itinerary_overview_activity;


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConcurCore app = (ConcurCore) getApplication();
        this.itineraryController = app.getTaItineraryController();

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            this.expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
            this.expenseReportName = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
        }

        setContentView(LAYOUT_ID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("@Itineraries@");
        }

        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            adapter = new ItineraryOverviewListAdapter(this);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryOverviewListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.util.Const;

/**
 * Created by Michael Becherer on 16-Jul-15.
 */
public class ItineraryOverviewActivity extends BaseActivity implements IControllerListener {

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
     * The indicator, whether the expense report is submitted or not.
     * True, if submitted.
     */
    private boolean expenseReportIsSubmitted;

    /**
     * The layout this activity is dealing with
     */
    private static final int LAYOUT_ID = R.layout.ta_itinerary_overview_activity;

    /**
     * The request code for the update activity
     */
    private static final int REQUEST_VIEW_TA_ITINERARY_UPDATE = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConcurCore app = (ConcurCore) getApplication();
        this.itineraryController = app.getTaItineraryController();
        this.itineraryController.registerListener(this);

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            this.expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
            this.expenseReportName = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
        }

        if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED)) {
            this.expenseReportIsSubmitted = getIntent().getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false);
        }

        setContentView(LAYOUT_ID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.ta_itineraries);
        }


        FloatingActionButton createItineraryFAB = (FloatingActionButton) findViewById(R.id.createItineraryFAB);
        if (createItineraryFAB != null) {
            if (expenseReportIsSubmitted) {
                createItineraryFAB.setVisibility(View.GONE);
            } else {
                createItineraryFAB.setVisibility(View.VISIBLE);
            }
            createItineraryFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ItineraryOverviewActivity.this, ItineraryUpdateActivity.class);
                    Itinerary itin = new Itinerary();
                    itin.setName(expenseReportName);
                    itin.setExpenseReportID(expenseReportKey);
                    intent.putExtra(BundleId.ITINERARY, itin);
                    startActivity(intent);
                }
            });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.itineraryController != null) {
            this.itineraryController.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            adapter = new ItineraryOverviewListAdapter(this);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (view == null) {
                        return;
                    }
                    Itinerary itinerary = itineraryController.getItineraryList().get(position);
                    Intent intent = new Intent(ItineraryOverviewActivity.this, ItineraryUpdateActivity.class);
                    intent.putExtra(BundleId.ITINERARY, itinerary);
                    startActivity(intent);
                }
            });
        }
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (!this.expenseReportIsSubmitted) {
            menu.add("@DELETE@");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Itinerary itin = (Itinerary) adapter.getItem(info.position);
        itineraryController.executeDeleteItinerary(itin);


        return true;
    }


    @Override
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {
        if (action == ControllerAction.DELETE) {
            if (isSuccess) {
                this.adapter.clear();
                this.adapter.addAll(itineraryController.getItineraryList());
                this.adapter.notifyDataSetChanged();
                Toast.makeText(ItineraryOverviewActivity.this, R.string.general_delete_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ItineraryOverviewActivity.this, R.string.general_delete_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

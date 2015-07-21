package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryOverviewListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.IServiceRequestListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.service.AbstractItineraryDeleteRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.util.Const;

/**
 * Created by Michael Becherer on 16-Jul-15.
 */
public class ItineraryOverviewActivity extends BaseActivity implements IServiceRequestListener {

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
            actionBar.setTitle("@List of editable Itineraries@");
        }


        FloatingActionButton createItineraryFAB = (FloatingActionButton) findViewById(R.id.createItineraryFAB);
        if (createItineraryFAB !=null){
            createItineraryFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ItineraryOverviewActivity.this, ItineraryUpdateActivity.class);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expenseReportKey);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_NAME, expenseReportName);
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
        this.itineraryController.unregisterListener(this);
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
                    CompactItinerary compactItinerary = itineraryController.getCompactItineraryList().get(position);
                    Intent intent = new Intent(ItineraryOverviewActivity.this, ItineraryUpdateActivity.class);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expenseReportKey);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_NAME, expenseReportName);
                    intent.putExtra(Const.EXTRA_ITINERARY_KEY, compactItinerary.getItineraryID());
                    startActivity(intent);
                }
            });
        }
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.add("@DELETE@");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        CompactItinerary itin = (CompactItinerary) adapter.getItem(info.position);

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                boolean isSuccess = resultData.getBoolean(AbstractItineraryDeleteRequest.IS_SUCCESS, false);
                if (isSuccess) {
                    Toast.makeText(ItineraryOverviewActivity.this, "@Success@", Toast.LENGTH_SHORT).show();
                    ItineraryOverviewActivity.this.itineraryController.registerListener(ItineraryOverviewActivity.this);
                    ItineraryOverviewActivity.this.itineraryController.refreshItineraries(
                            ItineraryOverviewActivity.this.expenseReportKey, false);
                } else {
                    Message msg = (Message) resultData
                            .getSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE);
                    if (msg != null) {
                        Toast.makeText(ItineraryOverviewActivity.this, "@Failed@", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Toast.makeText(ItineraryOverviewActivity.this, "@Failed@", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestCancel(Bundle resultData) {

            }

            @Override
            public void cleanup() {

            }
        });

        DeleteItineraryRequest deleteRequest = new DeleteItineraryRequest(this, receiver, itin.getItineraryID());
        deleteRequest.execute();

        return true;
    }

    @Override
    public void onRequestSuccess(String controllerTag) {
        this.adapter.clear();
        this.adapter.addAll(itineraryController.getCompactItineraryList());
        this.adapter.notifyDataSetChanged();
//        ListView listView = (ListView) findViewById(R.id.list_view);
//        listView.setAdapter(this.adapter);
    }

    @Override
    public void onRequestFail(String controllerTag) {
        Toast.makeText(ItineraryOverviewActivity.this, "@List reftresh Failed@", Toast.LENGTH_SHORT).show();
    }
}

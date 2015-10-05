package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.adapter.AssignableItineraryListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.ServiceRequestListenerFragment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.util.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by D049515 on 07.09.2015.
 */
public class AssignableItineraryListActivity extends TravelAllowanceBaseActivity implements IFragmentCallback{

    private static final String CLASS_TAG = AssignableItineraryListActivity.class.getSimpleName();

    /**
     * Tags for fragments
     */
    private static final String TAG_REFRESH_ASSIGNABLE_ITIN_LISTENER = "refreshAssignableItinServiceRequest";
    private static final String TAG_ASSIGN_ITIN_LISTENER = "assignItinServiceRequest";

    /**
     * Message strings for fragment callbacks
     */
    protected static final String MSG_REFRESH_ASSIN_ITIN_SUCCESS = "refreshAssignableItinSuccessMsg";
    protected static final String MSG_REFRESH_ASSIN_ITIN_FAILED = "refreshAssignableItinFailedMsg";

    protected static final String MSG_ASSIGN_ITIN_SUCCESS = "assignItinSuccessMsg";
    protected static final String MSG_ASSIGN_ITIN_FAILED = "assignItinFailedMsg";

    /**
     * Member fields
     */
    private List<AssignableItinerary> assignableItineraryList;

    private String expenseReportKey;

    private boolean itinRefreshDone = false;
    private boolean taRefreshDone = false;

    private AssignableItineraryListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ta_assignable_itin_list_activity);

        expenseReportKey = getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY);

        initializeToolbar(R.string.ta_travel_allowances);

        TextView tv = (TextView) findViewById(R.id.tv_list_title);
        String s = tv.getText().toString();
        tv.setText(s.toUpperCase(Locale.getDefault()));

        ConcurCore app = (ConcurCore) getApplication();
        assignableItineraryList = app.getTaController().getTaItineraryController().getAssignableItineraryList(expenseReportKey);

        ListView listView = (ListView) findViewById(R.id.listView);
        this.adapter = new AssignableItineraryListAdapter(this, new ArrayList<AssignableItinerary>(assignableItineraryList));
        listView.setAdapter(adapter);
        registerViewListener();


        if (savedInstanceState == null) {
            showProgressDialog();
            refreshAssignableItineraries(true);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
        setResult(RESULT_OK, resultIntent);
    }

    private void registerViewListener() {
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssignableItinerary itin = adapter.getItem(position);
                assignItinerary(itin);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssignableItineraryListActivity.this, ItineraryUpdateActivity.class);
                intent.putExtras(getIntent());
                startActivityForResult(intent, REQUEST_CODE_CREATE_ITINERARY);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_INSTANCE_STATE_ID_ITIN_REFRESH_DONE, itinRefreshDone);
        outState.putBoolean(SAVED_INSTANCE_STATE_ID_FIXED_TA_REFRESH_DONE, taRefreshDone);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.itinRefreshDone = savedInstanceState.getBoolean(SAVED_INSTANCE_STATE_ID_ITIN_REFRESH_DONE);
        this.taRefreshDone = savedInstanceState.getBoolean(SAVED_INSTANCE_STATE_ID_FIXED_TA_REFRESH_DONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CREATE_ITINERARY && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, TravelAllowanceActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
            this.startActivity(intent);
            this.finish();
        }
    }

    private void refreshAssignableItineraries(boolean needRequestMessage) {
        ServiceRequestListenerFragment f;
        if (needRequestMessage) {
            f =  getServiceRequestListenerFragment(TAG_REFRESH_ASSIGNABLE_ITIN_LISTENER, MSG_REFRESH_ASSIN_ITIN_SUCCESS, MSG_REFRESH_ASSIN_ITIN_FAILED);
        } else {
            f =  getServiceRequestListenerFragment("refresh.assignable.itin.no.response.needed", "", "");
        }

        itineraryController.refreshAssignableItineraries(expenseReportKey, f);
    }


    private void assignItinerary(AssignableItinerary itinerary) {
        ServiceRequestListenerFragment f = getServiceRequestListenerFragment(TAG_ASSIGN_ITIN_LISTENER,
                MSG_ASSIGN_ITIN_SUCCESS, MSG_ASSIGN_ITIN_FAILED);

        showProgressDialog();
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "assignItinerary", "Task Start"));
        itineraryController.assignItinerary(expenseReportKey, itinerary.getItineraryID(), f);
    }


    private void refreshListAdapter() {
        ListView listView = (ListView) findViewById(R.id.listView);
        AssignableItineraryListAdapter adapter = (AssignableItineraryListAdapter) listView.getAdapter();
        adapter.clear();
        ConcurCore app = (ConcurCore) getApplication();
        TravelAllowanceItineraryController controller = app.getTaController().getTaItineraryController();
        this.assignableItineraryList = controller.getAssignableItineraryList(expenseReportKey);
        adapter.addAll(assignableItineraryList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public synchronized void handleFragmentMessage(String fragmentMessage, Bundle extras) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleFragmentMessage", fragmentMessage));

        boolean isSuccess = true;
        if (extras != null) {
            isSuccess = extras.getBoolean(BundleId.IS_SUCCESS, true);
        }

        if (MSG_REFRESH_ASSIN_ITIN_SUCCESS.equals(fragmentMessage) || MSG_REFRESH_ASSIN_ITIN_FAILED.equals(fragmentMessage)) {
            refreshListAdapter();
            dismissProgressDialog();
        }

        if (MSG_ASSIGN_ITIN_SUCCESS.equals(fragmentMessage) || MSG_ASSIGN_ITIN_FAILED.equals(fragmentMessage)) {
            if (!isSuccess ||  MSG_ASSIGN_ITIN_FAILED.equals(fragmentMessage)) {
                dismissProgressDialog();
                Toast.makeText(this, R.string.adding_not_possible, Toast.LENGTH_SHORT).show();
                return;
            }
            refreshAssignableItineraries(false);
            refreshItineraries(expenseReportKey, false);
            refreshFixedTravelAllowances(expenseReportKey);
        }


        if (MSG_REFRESH_ITIN_FINISHED.equals(fragmentMessage)) {
            itinRefreshDone = true;
            onTaDataRefreshDone();
        }

        if (MSG_REFRESH_TA_FINISHED.equals(fragmentMessage)) {
            taRefreshDone = true;
            onTaDataRefreshDone();
        }

    }

    private synchronized void onTaDataRefreshDone() {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onTaDataRefreshDone", "ItinDone:" + itinRefreshDone + " TADone:" + taRefreshDone));
        if (taRefreshDone && itinRefreshDone) {
            dismissProgressDialog();
            taRefreshDone = false;
            itinRefreshDone = false;
            Intent intent = new Intent(this, TravelAllowanceActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
            this.startActivity(intent);
            this.finish();
        }
    }

}

/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.report.data.ExpenseReportApprover;
import com.concur.mobile.core.expense.report.service.ApproverSearchReply;
import com.concur.mobile.core.expense.report.service.ApproverSearchRequest;
import com.concur.mobile.core.receiver.NetworkActivityReceiver;
import com.concur.mobile.core.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.SpinnerItem;

/**
 * An activity supporting interactive approver search.
 * 
 * @author Chris N. Diaz
 */
public class ApproverSearchDialogFragment extends DialogFragment implements INetworkActivityListener {

    private static final String CLS_TAG = ListSearch.class.getSimpleName();

    private static final String APPROVER_SEARCH_REPLY_KEY = "approver.search.reply";

    private static final String APPROVER_SEARCH_RECEIVER_KEY = "approver.search.receiver";

    private static final String CURRENT_SEARCH_BY_FIELD_NAME = "expense.current.search.by.field.name";

    private static final String EXTRA_CURRENT_SEARCH_TEXT = "expense.approver.search.text";

    private static final long TEXT_SEARCH_DELAY = 750L;

    private static final int SEARCH_APPROVER_BY_FIELD_DIALOG = 1;

    // Spinner items for selecting the "search by" criteria.
    private static final SpinnerItem[] searchByFieldItems = new SpinnerItem[] {
            new SpinnerItem(ApproverSearchRequest.FIELD_LAST_NAME, R.string.approver_search_by_last),
            new SpinnerItem(ApproverSearchRequest.FIELD_FIRST_NAME, R.string.approver_search_by_first),
            new SpinnerItem(ApproverSearchRequest.FIELD_EMAIL_ADDRESS, R.string.approver_search_by_email),
            new SpinnerItem(ApproverSearchRequest.FIELD_LOGIN_ID, R.string.approver_search_by_login) };

    protected int colorWhiteStripe;
    protected int colorBlueStripe;

    protected String currentSearchText;
    protected EditText searchText;

    protected TextView searchByField;
    protected SpinnerItem currentSearchByFieldName;
    protected ListView searchResultsList;

    protected int minSearchLength = 3;

    protected Handler searchDelayHandler;
    protected Runnable searchDelayRunnable;

    protected String reportKey;
    protected ExpenseReportApprover defaultApprover;

    // Contains a reference to a receiver to start/stop search progress
    // indicator.
    private NetworkActivityReceiver networkActivityReceiver;

    // Contains the filter used to register the above receiver.
    private IntentFilter networkActivityFilter;

    // Contains the broadcast receiver to handle list search results.
    private ApproverSearchReceiver approverSearchReceiver;

    // Contains a reference to a filter used to register the list search
    // receiver.
    private final IntentFilter approverSearchFilter = new IntentFilter(Const.ACTION_EXPENSE_APPROVER_SEARCH_UPDATED);

    // A reference to an outstanding request.
    private ApproverSearchRequest approverSearchRequest;

    // Contains a cached approver search response of the empty string and with
    // 'isMRU' set to 'true'.
    protected ApproverSearchReply mruApproverSearchReply;

    // Map of non-configuration state values.
    private HashMap<String, Object> nonConfigMap;

    private OnDialogFragmentResultListener mOnDialogFragmentResultListener = null;
    private final Intent mIntent = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;

        view = inflater.inflate(R.layout.expense_report_approver_search, container);
        getDialog().getWindow().requestFeature(STYLE_NO_TITLE);
        configureControls(view);

        return view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        colorWhiteStripe = getResources().getColor(R.color.ListStripeWhite);
        colorBlueStripe = getResources().getColor(R.color.ListStripeBlue);

        searchDelayHandler = new Handler();
        searchDelayRunnable = new DelayedSearch();

        // Initialize some search parameters.
        if (bundle != null) {
            // Initialize from a previously saved state.
            if (bundle.containsKey(CURRENT_SEARCH_BY_FIELD_NAME)) {
                currentSearchByFieldName = (SpinnerItem) bundle.getSerializable(CURRENT_SEARCH_BY_FIELD_NAME);
            }
            if (bundle.containsKey(EXTRA_CURRENT_SEARCH_TEXT)) {
                currentSearchText = bundle.getString(EXTRA_CURRENT_SEARCH_TEXT);
            }
            if (bundle.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY)) {
                reportKey = bundle.getString(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY);
            }
            if (bundle.containsKey(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER)) {
                defaultApprover = (ExpenseReportApprover) bundle
                        .getSerializable(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER);
            }

        } else {
            // Initialize from the launch intent.
            // Intent intent = getIntent();
            if (mIntent.hasExtra(CURRENT_SEARCH_BY_FIELD_NAME)) {
                currentSearchByFieldName = (SpinnerItem) mIntent.getSerializableExtra(CURRENT_SEARCH_BY_FIELD_NAME);
            }
            if (mIntent.hasExtra(EXTRA_CURRENT_SEARCH_TEXT)) {
                currentSearchText = mIntent.getStringExtra(EXTRA_CURRENT_SEARCH_TEXT);
            }
            if (mIntent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY)) {
                reportKey = mIntent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY);
            }
            if (mIntent.hasExtra(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER)) {
                defaultApprover = (ExpenseReportApprover) mIntent
                        .getSerializableExtra(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER);
            }
        }

        // Restore the non-configuration map.
        if (getActivity().getLastNonConfigurationInstance() instanceof HashMap) {
            nonConfigMap = (HashMap<String, Object>) getActivity().getLastNonConfigurationInstance();

            // Restore 'listSearchReceiver'.
            if (nonConfigMap != null && nonConfigMap.containsKey(APPROVER_SEARCH_RECEIVER_KEY)) {
                approverSearchReceiver = (ApproverSearchReceiver) nonConfigMap.get(APPROVER_SEARCH_RECEIVER_KEY);
                if (approverSearchReceiver != null) {
                    approverSearchReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: nonConfigMap contains a null approver search receiver!");
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    // @Override
    protected Dialog showDialog(int id) {
        Dialog dlg = null;

        switch (id) {
        case SEARCH_APPROVER_BY_FIELD_DIALOG: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
            dlgBldr.setCancelable(true);
            ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(getActivity(),
                    android.R.layout.simple_spinner_item, searchByFieldItems) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    return super.getDropDownView(position, convertView, parent);
                }
            };

            listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Get the currently selected item.
            int selectedItem = -1;
            if (currentSearchByFieldName != null) {
                for (int i = 0; i < searchByFieldItems.length; i++) {
                    if (currentSearchByFieldName.id.equals(searchByFieldItems[i].id)) {
                        selectedItem = i;
                        break;
                    }
                }
            }

            dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    boolean changed = currentSearchByFieldName.id != searchByFieldItems[which].id;

                    currentSearchByFieldName = searchByFieldItems[which];
                    searchByField.setText(currentSearchByFieldName.name);
                    dialog.dismiss();

                    // Update the search.
                    if (changed) {
                        SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                        adapter.clearListItems();
                        doSearch(currentSearchText, 0L);
                    }
                }
            });

            dlgBldr.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
            dlg = dlgBldr.create();

            break;
        }
        default: {
            ConcurCore ConcurCore = (ConcurCore) getActivity().getApplication();
            dlg = ConcurCore.createDialog(getActivity(), id);
            break;
        }
        } // end-switch

        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Write out any passed in search parameters.
        if (currentSearchByFieldName != null) {
            outState.putSerializable(CURRENT_SEARCH_BY_FIELD_NAME, currentSearchByFieldName);
        }
        if (currentSearchText != null) {
            outState.putString(EXTRA_CURRENT_SEARCH_TEXT, currentSearchText);
        }
        if (reportKey != null) {
            outState.putString(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY, reportKey);
        }
        if (defaultApprover != null) {
            outState.putSerializable(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER, defaultApprover);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Store current search text.
        if (searchText != null) {
            currentSearchText = searchText.getText().toString();
        }

        if (nonConfigMap == null) {
            nonConfigMap = new HashMap<String, Object>();
        }

        // Store the current approver search reply.
        ConcurCore ConcurCore = (ConcurCore) getActivity().getApplication();
        ApproverSearchReply approverSearchReply = ConcurCore.getApproverSearchResults();
        if (approverSearchReply != null) {
            nonConfigMap.put(APPROVER_SEARCH_REPLY_KEY, approverSearchReply);
        }
        // Store 'approverSearchReceiver'.
        if (approverSearchReceiver != null) {
            nonConfigMap.put(APPROVER_SEARCH_RECEIVER_KEY, approverSearchReceiver);
            // Clear out the activity, it will be reset in the 'onCreate' method of a new activity.
            approverSearchReceiver.setActivity(null);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        ApproverSearchReply lastApproverSearchReply = null;
        if (nonConfigMap != null) {
            // Recover the last search list response.
            if (nonConfigMap.containsKey(APPROVER_SEARCH_REPLY_KEY)) {
                lastApproverSearchReply = (ApproverSearchReply) nonConfigMap.get(APPROVER_SEARCH_REPLY_KEY);
                if (lastApproverSearchReply == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: nonConfigMap contains null last approver search value!");
                }
            }
        }

        // Check for previous list.
        if (lastApproverSearchReply != null && lastApproverSearchReply.results != null
                && currentSearchByFieldName != null) {

            // Fill the list with the results from the last response.
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
            adapter.updateListItems(filterItems(lastApproverSearchReply.results, currentSearchByFieldName.id,
                    currentSearchText));

        } else if (defaultApprover != null) {
            // To be certain we have the correct default approver,
            // we should search by login id.
            doSearch(defaultApprover.loginId, searchByFieldItems[3], 0L);
            // Need to update the fields.
            if (searchText != null) {
                searchText.setText(defaultApprover.loginId);
            }
            if (searchByField != null) {
                searchByField.setText(searchByFieldItems[3].name);
            }
        } else {
            // Immediately kick-off a blank string search,
            // which will search for all approvers.
            doSearch("", 0L);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    public void onStart() {
        super.onStart();

        getActivity().registerReceiver(networkActivityReceiver, networkActivityFilter);

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStop()
     */
    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(networkActivityReceiver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #isNetworkRequestInteresting(int)
     */
    @Override
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        return (networkMsgType == Const.MSG_EXPENSE_APPROVER_SEARCH_REQUEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStarted(int)
     */
    @Override
    public void networkActivityStarted(int networkMsgType) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStopped(int)
     */
    @Override
    public void networkActivityStopped(int networkMsgType) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #getNetworkActivityText(java.lang.String)
     */
    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        return defaultText;
    }

    protected void configureControls(View view) {

        // Register the network activity receiver.
        networkActivityReceiver = new NetworkActivityReceiver(getActivity(), this);
        networkActivityFilter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
        networkActivityFilter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);

        // Grab all our controls
        searchText = (EditText) view.findViewById(R.id.listSearchEdit);
        String hintText = Format.localizeText(getActivity(), R.string.attendee_search_hint_text, minSearchLength);
        searchText.setHint(Html.fromHtml("<small><small>" + hintText + "</small></small>"));
        searchResultsList = (ListView) view.findViewById(R.id.listSearchResults);

        // Search By field.
        TextView searchByTitle = (TextView) view.findViewById(R.id.search_approver_by_field).findViewById(
                R.id.field_name);
        searchByTitle.setText(R.string.approver_search_by);
        searchByField = (TextView) view.findViewById(R.id.search_approver_by_field).findViewById(R.id.field_value);
        view.findViewById(R.id.search_approver_by_field).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(SEARCH_APPROVER_BY_FIELD_DIALOG).show();
            }
        });

        // Set any previously typed text.
        if (nonConfigMap != null) {
            if (nonConfigMap.containsKey(EXTRA_CURRENT_SEARCH_TEXT)) {
                currentSearchText = (String) nonConfigMap.get(EXTRA_CURRENT_SEARCH_TEXT);
                if (currentSearchText != null) {
                    searchText.setText(currentSearchText);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".configureControls: nonConfigMap has null search text value!");
                }
            }

            if (nonConfigMap.containsKey(CURRENT_SEARCH_BY_FIELD_NAME)) {
                currentSearchByFieldName = (SpinnerItem) nonConfigMap.get(CURRENT_SEARCH_BY_FIELD_NAME);
            }
        } else {
            // Default to search by Last Name.
            currentSearchByFieldName = searchByFieldItems[0];
        }

        // Set the search by field text.
        if (currentSearchByFieldName != null) {
            searchByField.setText(currentSearchByFieldName.name);
        }

        // Listen for typing in the search filter
        searchText.addTextChangedListener(new SearchTextWatcher());

        // Get the list ready
        searchResultsList.setAdapter(new SearchResultsAdapter(getActivity()));
        searchResultsList.setOnItemClickListener(new SearchResultClickListener());

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - start
    // ///////////////////////////////////////////////////////////////////////////

    class SearchTextWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {

            if (s.length() >= minSearchLength) {
                doSearch(s.toString(), TEXT_SEARCH_DELAY);
            } else if (s.length() == 0 && mruApproverSearchReply != null) {
                // Display the cached results.
                searchDelayHandler.removeCallbacks(searchDelayRunnable);
                SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                adapter.updateListItems(mruApproverSearchReply.results);
            } else if (s.length() == 0) {
                // Do search for all approvers.
                doSearch("", 0L);
            } else {
                searchDelayHandler.removeCallbacks(searchDelayRunnable);
                SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                adapter.clearListItems();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    }

    class DelayedSearch implements Runnable {

        @Override
        public void run() {
            if (ConcurCore.isConnected()) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()
                        .getApplicationContext());
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                ConcurCore app = (ConcurCore) getActivity().getApplication();
                // Register a receiver to handle the request.
                registerApproverSearchReceiver();
                approverSearchRequest = app.getService().sendApproverSearchRequest(userId, reportKey,
                        currentSearchByFieldName.id, currentSearchText);
                if (approverSearchRequest != null) {
                    // Set the request on the receiver.
                    approverSearchReceiver.setRequest(approverSearchRequest);
                } else {
                    // Unregister the receiver.
                    unregisterApproverSearchReceiver();
                }
            }
        }
    }

    protected void doSearch(String search, SpinnerItem searchByField, long delay) {
        currentSearchText = search;
        searchDelayHandler.removeCallbacks(searchDelayRunnable);

        if (searchByField != null) {
            currentSearchByFieldName = searchByField;
        }

        // If we have a cached MRU response of the empty string (in 'onStart'),
        // / then use it; otherwise let the search go to the server.
        if (search.length() == 0 && mruApproverSearchReply != null) {
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
            adapter.updateListItems(mruApproverSearchReply.results);
        } else {
            if (delay > 0L) {
                searchDelayHandler.postDelayed(searchDelayRunnable, delay);
            } else {
                searchDelayHandler.post(searchDelayRunnable);
            }
        }
    }

    protected void doSearch(String search, long delay) {
        doSearch(search, null, delay);
    }

    /**
     * Will filter <code>items</code> based on <code>filterText</code>.
     * 
     * @param approvers
     *            the list of items to filter.
     * @param fieldName
     *            the name of the search field (e.g. FIRST_NAME, LAST_NAME, etc).
     * @param filterText
     *            the filter text.
     * @return a list of filtered items or <code>null</code> if no items match.
     */
    private List<ExpenseReportApprover> filterItems(List<ExpenseReportApprover> approvers, String fieldName,
            String filterText) {
        List<ExpenseReportApprover> retVal = null;
        // List<ExpenseReportApprover> retVal = new
        // ArrayList<ExpenseReportApprover>();
        // if( filterText != null && filterText.length() > 0 ) {
        // if( attendees != null ) {
        // String lowerCaseFilterText = filterText.toLowerCase();
        // HashMap<String,ExpenseReportApprover> liKeyItemMap = new
        // HashMap<String,ExpenseReportApprover>();
        // for( ExpenseReportApprover approver: approvers ) {
        //
        // if( listItem.text != null &&
        // listItem.text.toLowerCase().startsWith(lowerCaseFilterText) &&
        // !liKeyItemMap.containsKey(listItem.key)) {
        // retVal.add(listItem);
        // liKeyItemMap.put(listItem.key, listItem);
        // }
        // }
        // }
        // } else {
        // retVal = approver;
        // }
        retVal = approvers;
        return retVal;
    }

    /**
     * Called when this user cannot search for the default approver. This method will then disable the search field and add a few
     * more widgets to allow the user to use the default approver.
     */
    private void enableDefaultApproverOnlySelection() {

        if (defaultApprover != null) {

            // Disable the search fields.
            getView().findViewById(R.id.search_approver_by_field).setClickable(false);
            getView().findViewById(R.id.search_approver_by_field).findViewById(R.id.field_image)
                    .setVisibility(View.GONE);
            searchByField.setTextColor(getResources().getColor(R.color.light_gray));
            searchText.setEnabled(false);
            searchResultsList.setEnabled(false);
            searchResultsList.setVisibility(View.GONE);

            // Show the field to display the default approver.
            getView().findViewById(R.id.default_approver_layout).setVisibility(View.VISIBLE);
            View defaultApproverView = getView().findViewById(R.id.default_approver_field);
            TextView defaultApproverField = (TextView) defaultApproverView.findViewById(R.id.field_value);

            // Set the default approver name.
            String text = defaultApprover.lastName + ", " + defaultApprover.firstName;
            defaultApproverField.setText(text);

            // Set the title and email
            TextView defaultApproverTitle = (TextView) defaultApproverView.findViewById(R.id.field_name);
            text = getString(R.string.approver_search_default_approver);
            if (defaultApprover.email != null) {
                text += " (" + defaultApprover.email + ")";
            }
            defaultApproverTitle.setText(text);

            // Add listener to use the default approver.
            defaultApproverView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i(Const.LOG_TAG, CLS_TAG + " - Submitting using default approver.");

                    Intent i = new Intent();
                    i.putExtra(Const.EXTRA_EXPENSE_REPORT_SELECTED_APPROVER, defaultApprover);
                    if (mOnDialogFragmentResultListener != null) {
                        mOnDialogFragmentResultListener.onDialogFragmentResult(Activity.RESULT_OK, i);
                    }
                    dismiss();
                    // setResult(RESULT_OK, i);
                    // finish();
                }
            });

        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Will register the approver search receiver and set the <code>approverSearchReceiver</code> member.
     */
    private void registerApproverSearchReceiver() {
        if (approverSearchReceiver == null) {
            approverSearchReceiver = new ApproverSearchReceiver(this);
            getActivity().getApplicationContext().registerReceiver(approverSearchReceiver, approverSearchFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerApproverSearchReceiver: approverSearchReceiver is not null!");
        }
    }

    /**
     * Will unregister an approver search receiver and clear the <code>approverSearchReceiver</code> reference.
     */
    private void unregisterApproverSearchReceiver() {
        if (approverSearchReceiver != null) {
            getActivity().getApplicationContext().unregisterReceiver(approverSearchReceiver);
            approverSearchReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterApproverSearchReceiver: approverSearchReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> that handles the result of an approver search.
     * 
     * @author Chris N. Diaz
     */
    static class ApproverSearchReceiver extends BroadcastReceiver {

        private static String CLS_TAG = ApproverSearchDialogFragment.CLS_TAG + "."
                + ApproverSearchReceiver.class.getSimpleName();

        // A reference to the activity.
        private ApproverSearchDialogFragment activity;

        // A reference to the exchange rate request.
        private ApproverSearchRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ApproverSearchReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ApproverSearchReceiver(ApproverSearchDialogFragment activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(ApproverSearchDialogFragment activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.approverSearchRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getActivity().getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the approver search request object associated with this broadcast receiver.
         * 
         * @param request
         *            the approver search request object associated with this broadcast receiver.
         */
        void setRequest(ApproverSearchRequest request) {
            this.request = request;
        }

        /**
         * Receive notification that something has been updated. This method may be called any number of times while the Activity
         * is running.
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterApproverSearchReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {

                                    ConcurCore ConcurCore = (ConcurCore) activity.getActivity().getApplication();
                                    ApproverSearchReply approverSearchReply = ConcurCore.getApproverSearchResults();
                                    if (approverSearchReply != null && approverSearchReply.results != null) {
                                        if (approverSearchReply.results.size() > 0) {
                                            SearchResultsAdapter adapter = (SearchResultsAdapter) activity.searchResultsList
                                                    .getAdapter();
                                            adapter.updateListItems(activity.filterItems(approverSearchReply.results,
                                                    activity.currentSearchByFieldName.id, activity.currentSearchText));
                                            // If 'isMRU' is 'true' and the
                                            // query is the empty string and we
                                            // have not cached this response,
                                            // then hold onto it.
                                            if (approverSearchReply.query != null
                                                    && approverSearchReply.query.length() == 0
                                                    && activity.mruApproverSearchReply == null) {

                                                activity.mruApproverSearchReply = approverSearchReply;
                                            }
                                        } else {
                                            // If this is the initial search for
                                            // default approver and there are no
                                            // results, then we need to disable
                                            // the UI and allow selecting of
                                            // default approver.
                                            if (activity.defaultApprover != null
                                                    && activity.defaultApprover.loginId
                                                            .equals(activity.currentSearchText)) {

                                                activity.enableDefaultApproverOnlySelection();
                                            }
                                        }
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    activity.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                activity.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                            activity.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear the request reference.
                activity.approverSearchRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    class SearchResultClickListener implements AdapterView.OnItemClickListener {

        private final String CLS_TAG = ApproverSearchDialogFragment.CLS_TAG
                + SearchResultClickListener.class.getSimpleName();

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent();
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
            ExpenseReportApprover selectedApprover = adapter.getItem(position);
            if (selectedApprover != null) {
                i.putExtra(Const.EXTRA_EXPENSE_REPORT_SELECTED_APPROVER, selectedApprover);
                if (mOnDialogFragmentResultListener != null) {
                    mOnDialogFragmentResultListener.onDialogFragmentResult(Activity.RESULT_OK, i);
                }
                dismiss();
                // setResult(RESULT_OK, i);
                // finish();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: selectedItem is null!");
            }
        }
    }

    class SearchResultsAdapter extends BaseAdapter {

        private final Context context;
        private List<ExpenseReportApprover> approverItems;
        private final List<ExpenseReportApprover> emptyApproverItems = new ArrayList<ExpenseReportApprover>(1);

        public SearchResultsAdapter(Context context) {
            this.context = context;
            approverItems = emptyApproverItems;
        }

        public void updateListItems(List<ExpenseReportApprover> approverItems) {
            if (approverItems == null) {
                this.approverItems = emptyApproverItems;
            } else {
                this.approverItems = approverItems;
            }
            notifyDataSetChanged();
        }

        public void clearListItems() {
            approverItems = emptyApproverItems;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return approverItems.size();
        }

        @Override
        public ExpenseReportApprover getItem(int position) {
            return approverItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout row = (RelativeLayout) convertView;

            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                row = (RelativeLayout) inflater.inflate(R.layout.expense_list_search_row, null);
            }

            ExpenseReportApprover approverItem = getItem(position);
            String text = approverItem.lastName + ", " + approverItem.firstName;
            if (approverItem.email != null) {
                text += " (" + approverItem.email + ")";
            }

            TextView tv = (TextView) row.findViewById(R.id.listItemName);
            tv.setText(text);

            if ((position % 2) == 0) {
                tv.setBackgroundColor(colorBlueStripe);
            } else {
                tv.setBackgroundColor(colorWhiteStripe);
            }

            return row;
        }

    }

    public void setOnDialogFragmentResultListener(OnDialogFragmentResultListener listener) {
        mOnDialogFragmentResultListener = listener;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - end
    // ///////////////////////////////////////////////////////////////////////////

}

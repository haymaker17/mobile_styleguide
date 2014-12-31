/**
 * 
 */
package com.concur.mobile.gov.expense.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.charge.data.CategoryListItem;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.receiver.NetworkActivityReceiver;
import com.concur.mobile.core.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.service.GovSearchListRequest;
import com.concur.mobile.gov.expense.service.GovSearchListResponse;

/**
 * An extension of <code>Activity</code> for performing interactive list searching.
 * 
 * NOTE: This class is modeled after the LocationSearch class in the travel package.
 * 
 * @author AndrewK
 */
public class GovListSearch extends Activity implements INetworkActivityListener {

    private static final String CLS_TAG = GovListSearch.class.getSimpleName();

    protected static final String MRU_LIST_RESPONSE_KEY = "mru.list.response";

    protected static final String SEARCH_LIST_RESPONSE_KEY = "search.list.response";

    protected static final String SEARCH_TEXT_KEY = "search.text";

    protected static final String LIST_SEARCH_RECEIVER_KEY = "list.search.receiver";

    protected static final String EXTERNAL_REQUEST_KEY = "external.request";

    protected static final long TEXT_SEARCH_DELAY = 750L;

    protected static final int DIALOG_FETCHING_EXTERNAL_LIST_KEY = 0;

    protected int colorWhiteStripe;
    protected int colorBlueStripe;
    protected int category;

    protected EditText searchText;
    protected ListView searchResultsList;

    protected String currentSearchText;
    protected String searchTitle;

    protected int minSearchLength = 3;

    protected Handler searchDelayHandler;
    protected Runnable searchDelayRunnable;

    protected Boolean showCodes;

    protected String fieldId;
    protected String expDesc;
    protected String ftCode;
    protected String listKey;
    protected String parentLiKey;
    protected String[] excludeKeys;

    /**
     * Contains whether or not the outstanding list search request is for obtaining an LiKey value for an external list item (one
     * provided via a connector).
     */
    protected boolean externalRequest;
    /**
     * Contains the external ID of the selected list item.
     */
    protected String externalId;

    // Contains a reference to a receiver to start/stop search progress indicator.
    protected NetworkActivityReceiver networkActivityReceiver;
    // Contains the filter used to register the above receiver.
    protected IntentFilter networkActivityFilter;

    // Contains the broadcast receiver to handle list search results.
    private GovListSearchReceiver listSearchReceiver;

    // Contains a reference to a filter used to register the list search receiver.
    private IntentFilter listSearchFilter = new IntentFilter(Const.ACTION_EXPENSE_SEARCH_LIST_UPDATED);

    // A reference to an outstanding request.
    private GovSearchListRequest listSearchRequest;

    // Contains a cached search list response of the empty string and with 'isMRU' set to 'true'.
    protected GovSearchListResponse mruListResponse;

    // The searchable static list if provided
    protected ArrayList<ListItem> searchableStaticList;

    protected HashMap<String, Object> nonConfigMap;

    protected Intent resultIntent = new Intent();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.expense_list_search);

        colorWhiteStripe = getResources().getColor(R.color.ListStripeWhite);
        colorBlueStripe = getResources().getColor(R.color.ListStripeBlue);
        // category =getResources().getColor(R.color.light_gray);
        category = getResources().getColor(R.color.SectionHeaderBackground);
        searchDelayHandler = new Handler();
        searchDelayRunnable = new GovDelayedSearch();

        // Default to not show codes. This satisfies Citi per MOB-8326.
        showCodes = false;

        // Initialize some search parameters.
        if (savedInstanceState != null) {
            // Initialize from a previously saved state.

            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID)) {
                fieldId = savedInstanceState.getString(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID);
            }
            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE)) {
                ftCode = savedInstanceState.getString(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE);
            }
            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_LIST_KEY)) {
                listKey = savedInstanceState.getString(Const.EXTRA_EXPENSE_LIST_SEARCH_LIST_KEY);
            }
            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_PARENT_LI_KEY)) {
                parentLiKey = savedInstanceState.getString(Const.EXTRA_EXPENSE_LIST_SEARCH_PARENT_LI_KEY);
            }
            if (savedInstanceState.containsKey(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_DESC)) {
                expDesc = savedInstanceState.getString(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_DESC);
            }
            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE)) {
                searchTitle = savedInstanceState.getString(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE);
            }
            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_EXCLUDE_KEYS)) {
                excludeKeys = savedInstanceState.getStringArray(Const.EXTRA_EXPENSE_LIST_SEARCH_EXCLUDE_KEYS);
            }
            if (savedInstanceState.containsKey(Const.EXTRA_COMBO_BOX_INLINE_TEXT)) {
                currentSearchText = savedInstanceState.getString(Const.EXTRA_COMBO_BOX_INLINE_TEXT);
            }
            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST)) {
                searchableStaticList = (ArrayList<ListItem>) savedInstanceState
                    .getSerializable(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST);
            }
            externalRequest = savedInstanceState.getBoolean(EXTERNAL_REQUEST_KEY);

            showCodes = savedInstanceState.getBoolean(Const.EXTRA_EXPENSE_LIST_SHOW_CODES);
        } else {
            // Initialize from the launch intent.
            Intent intent = getIntent();
            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID)) {
                fieldId = intent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE)) {
                ftCode = intent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_LIST_KEY)) {
                listKey = intent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_LIST_KEY);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_PARENT_LI_KEY)) {
                parentLiKey = intent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_PARENT_LI_KEY);
            }
            if (intent.hasExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_DESC)) {
                expDesc = intent.getStringExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_DESC);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE)) {
                searchTitle = intent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_EXCLUDE_KEYS)) {
                excludeKeys = intent.getStringArrayExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_EXCLUDE_KEYS);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST)) {
                searchableStaticList = (ArrayList<ListItem>) intent
                    .getSerializableExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST);
            }

            showCodes = intent.getBooleanExtra(Const.EXTRA_EXPENSE_LIST_SHOW_CODES, false);
        }

        restoreNonConfigMap();

        configureControls();
    }

    protected void restoreNonConfigMap() {
        // Restore the non-configuration map.
        if (getLastNonConfigurationInstance() instanceof HashMap) {
            nonConfigMap = (HashMap<String, Object>) getLastNonConfigurationInstance();

            // Restore 'listSearchReceiver'.
            if (nonConfigMap != null && nonConfigMap.containsKey(LIST_SEARCH_RECEIVER_KEY)) {
                listSearchReceiver = (GovListSearchReceiver) nonConfigMap.get(LIST_SEARCH_RECEIVER_KEY);
                if (listSearchReceiver != null) {
                    listSearchReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: nonConfigMap contains a null list search receiver!");
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DIALOG_FETCHING_EXTERNAL_LIST_KEY: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(R.string.dlg_expense_list_search_selection_progress_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (listSearchRequest != null) {
                        listSearchRequest.cancel();
                        finish();
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        default: {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            dialog = ConcurCore.createDialog(this, id);
            break;
        }
        }
        if (dialog == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".onCreateDialog: ConcurCore.onCreateDialog did not create a dialog for id '" + id + "'.");
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {

        HashMap<String, Object> nonConfigMap = new HashMap<String, Object>();
        // Store 'mruListResponse'.
        if (mruListResponse != null) {
            nonConfigMap.put(MRU_LIST_RESPONSE_KEY, mruListResponse);
        }
        // Store current search text.
        if (searchText != null) {
            nonConfigMap.put(SEARCH_TEXT_KEY, searchText.getText().toString());
        }
        // Store the current search list response.
        GovAppMobile app = (GovAppMobile) getApplication();
        GovSearchListResponse searchListResponse = app.getGovSearchListResponse();
        if (searchListResponse != null) {
            nonConfigMap.put(SEARCH_LIST_RESPONSE_KEY, searchListResponse);
        }
        // Store 'listSearchReceiver'.
        if (listSearchReceiver != null) {
            nonConfigMap.put(LIST_SEARCH_RECEIVER_KEY, listSearchReceiver);
            // Clear out the activity, it will be reset in the 'onCreate' method of
            // a new activity.
            listSearchReceiver.setActivity(null);
        }
        return nonConfigMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Write out any passed in search parameters.
        if (fieldId != null) {
            outState.putString(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, fieldId);
        }
        if (ftCode != null) {
            outState.putString(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE, ftCode);
        }
        if (listKey != null) {
            outState.putString(Const.EXTRA_EXPENSE_LIST_SEARCH_LIST_KEY, listKey);
        }
        if (parentLiKey != null) {
            outState.putString(Const.EXTRA_EXPENSE_LIST_SEARCH_PARENT_LI_KEY, parentLiKey);
        }
        if (expDesc != null) {
            outState.putString(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_DESC, expDesc);
        }
        if (searchTitle != null) {
            outState.putString(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, searchTitle);
        }
        if (excludeKeys != null) {
            outState.putStringArray(Const.EXTRA_EXPENSE_LIST_SEARCH_EXCLUDE_KEYS, excludeKeys);
        }
        if (searchableStaticList != null) {
            outState.putSerializable(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST, searchableStaticList);
        }
        outState.putBoolean(EXTERNAL_REQUEST_KEY, externalRequest);
        outState.putBoolean(Const.EXTRA_EXPENSE_LIST_SHOW_CODES, showCodes);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(networkActivityReceiver, networkActivityFilter);

        GovSearchListResponse lastSearchListResponse = null;
        String lastSearchText = null;
        if (nonConfigMap != null) {
            // Recover any MRU list response.
            if (nonConfigMap.containsKey(MRU_LIST_RESPONSE_KEY)) {
                mruListResponse = (GovSearchListResponse) nonConfigMap.get(MRU_LIST_RESPONSE_KEY);
                if (mruListResponse == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: nonConfigMap contains null mru list response value!");
                }
            }
            // Recover the last search list response.
            if (nonConfigMap.containsKey(SEARCH_LIST_RESPONSE_KEY)) {
                lastSearchListResponse = (GovSearchListResponse) nonConfigMap.get(SEARCH_LIST_RESPONSE_KEY);
                if (lastSearchListResponse == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: nonConfigMap contains null last search response value!");
                }
            }
            // Recover last search text.
            if (nonConfigMap.containsKey(SEARCH_TEXT_KEY)) {
                lastSearchText = (String) nonConfigMap.get(SEARCH_TEXT_KEY);
                if (lastSearchText == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: nonConfigMap contains null last search text value!");
                }
            }
        }

        // Check for previous list.
        if (lastSearchListResponse != null) {
            if (lastSearchListResponse.tmFomField != null) {
                ArrayList<ListItem> list = lastSearchListResponse.tmFomField.getSearchableStaticList();
                if (list != null && !list.isEmpty()) {
                    // Fill the list with the results from the last response.
                    SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                    adapter.updateListItems(filterItems(list, currentSearchText, true));
                }
            }
        } else if (currentSearchText != null && currentSearchText.length() > 0) {
            doSearch(currentSearchText, 0L);
        } else {
            // If this list is an MRU list, then immediately kick-off a blank string search.
            doSearch("", 0L);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(networkActivityReceiver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#isNetworkRequestInteresting(int)
     */
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        return (networkMsgType == Const.MSG_EXPENSE_SEARCH_LIST_REQUEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#networkActivityStarted(int)
     */
    public void networkActivityStarted(int networkMsgType) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#networkActivityStopped(int)
     */
    public void networkActivityStopped(int networkMsgType) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#getNetworkActivityText(java.lang.String)
     */
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        return defaultText;
    }

    protected void configureControls() {

        // Register the network activity receiver.
        networkActivityReceiver = new NetworkActivityReceiver(this, this);
        networkActivityFilter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
        networkActivityFilter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);

        // Grab all our controls
        searchText = (EditText) findViewById(R.id.listSearchEdit);
        if (currentSearchText != null) {
            searchText.setText(currentSearchText);
            resultIntent.putExtra(Const.EXTRA_COMBO_BOX_ACTION, Const.COMBO_BOX_INLINE_TEXT);
            resultIntent.putExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT, currentSearchText);
        }
        searchResultsList = (ListView) findViewById(R.id.listSearchResults);

        // Set any previously typed text.
        if (nonConfigMap != null) {
            if (nonConfigMap.containsKey(SEARCH_TEXT_KEY)) {
                currentSearchText = (String) nonConfigMap.get(SEARCH_TEXT_KEY);
                if (currentSearchText != null) {
                    searchText.setText(currentSearchText);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".configureControls: nonConfigMap has null search text value!");
                }
            } else if (currentSearchText != null && currentSearchText.length() > 0) {
                searchText.setText(currentSearchText);
            }
        }

        // Set the title.
        TextView txtView = (TextView) findViewById(R.id.title);
        if (txtView != null) {
            String txtTitle = getText(R.string.general_search).toString();
            if (searchTitle != null) {
                StringBuilder strBldr = new StringBuilder(searchTitle);
                strBldr.append(' ');
                strBldr.append(txtTitle);
                txtTitle = strBldr.toString();
            }
            txtView.setText(txtTitle);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureControls: can't locate title text view!");
        }

        // Listen for typing in the search filter
        searchText.addTextChangedListener(new SearchTextWatcher());

        // Get the list ready
        searchResultsList.setAdapter(new SearchResultsAdapter(this, showCodes));
        searchResultsList.setOnItemClickListener(new SearchResultClickListener());

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - start
    // ///////////////////////////////////////////////////////////////////////////

    class SearchTextWatcher implements TextWatcher {

        public void afterTextChanged(Editable s) {

            resultIntent.putExtra(Const.EXTRA_COMBO_BOX_ACTION, Const.COMBO_BOX_INLINE_TEXT);
            resultIntent.putExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT, s.toString());

            if (s.length() >= minSearchLength) {
                doSearch(s.toString(), TEXT_SEARCH_DELAY);
            } else if (s.length() == 0 && mruListResponse != null) {
                // Display the cached results.
                searchDelayHandler.removeCallbacks(searchDelayRunnable);
                if (mruListResponse.tmFomField != null) {
                    ArrayList<ListItem> list = mruListResponse.tmFomField.resultList;
                    if (list != null && !list.isEmpty()) {
                        SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                        adapter.updateListItems(list);
                    }
                }
            } else {
                searchDelayHandler.removeCallbacks(searchDelayRunnable);
                SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();

                // If using a searchable static list then just display it again
                if (searchableStaticList != null) {
                    adapter.updateListItems(searchableStaticList);
                } else {
                    adapter.clearListItems();
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    }

    class GovDelayedSearch implements Runnable {

        public void run() {
            if (GovAppMobile.isConnected()) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String userId = prefs.getString(Const.PREF_USER_ID, null);
                GovAppMobile app = (GovAppMobile) getApplication();

                // If we are using a searchable static list (local) then don't do any server requests
                if (currentSearchText == null || currentSearchText.length() <= minSearchLength) {
                    SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                    adapter.updateListItems(filterItems(searchableStaticList, currentSearchText, false));
                } else {
                    // Register a receiver to handle the request.
                    registerListSearchReceiver();

                    listSearchRequest = app.getService().sendGovSearchListRequest(userId, currentSearchText, fieldId, expDesc);

                    if (listSearchRequest != null) {
                        // Set the request on the receiver.
                        listSearchReceiver.setRequest(listSearchRequest);
                        // If this is an external request, then show a dialog.
                        if (externalRequest) {
                            showDialog(DIALOG_FETCHING_EXTERNAL_LIST_KEY);
                        }
                    } else {
                        // Unregister the receiver.
                        unregisterListSearchReceiver();
                        // TODO: Present an error dialog.
                    }
                }
            }
        }
    }

    protected void doSearch(String search, long delay) {
        searchDelayHandler.removeCallbacks(searchDelayRunnable);
        currentSearchText = search;
        // If we have a cached MRU response of the empty string (in 'onStart'), then use it; otherwise
        // let the search go to the server.
        if (search.length() == 0 && mruListResponse != null) {
            if (mruListResponse.tmFomField != null) {
                ArrayList<ListItem> list = mruListResponse.tmFomField.resultList;
                if (list != null && !list.isEmpty()) {
                    SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                    adapter.updateListItems(list);
                }
            }
        } else {
            if (delay > 0L) {
                searchDelayHandler.postDelayed(searchDelayRunnable, delay);
            } else {
                searchDelayHandler.post(searchDelayRunnable);
            }
        }
    }

    /**
     * Will filter <code>items</code> based on <code>filterText</code>.
     * 
     * @param items
     *            the list of items to filter.
     * @param filterText
     *            the filter text.
     * @return a list of filtered items or <code>null</code> if no items match.
     */
    private List<ListItem> filterItems(List<ListItem> items, String filterText, boolean isIncludeNone) {
        List<ListItem> retVal = new ArrayList<ListItem>();

        // If using a searchable static list, don't add none
        if (searchableStaticList == null && isIncludeNone) {
            ListItem none = new ListItem();
            none.text = this.getText(R.string.general_none).toString();
            retVal.add(0, none);
        }

        if (filterText != null && filterText.length() > 0) {
            if (items != null) {
                String lowerCaseFilterText = filterText.toLowerCase();
                HashMap<String, ListItem> liKeyItemMap = new HashMap<String, ListItem>();
                for (ListItem listItem : items) {
                    if (!(listItem instanceof CategoryListItem)) {
                        boolean useCode = Preferences.shouldShowListCodes();
                        String compareText;
                        if (useCode && listItem.code != null) {
                            compareText = listItem.code;
                        } else {
                            compareText = listItem.text;
                        }
                        if (compareText != null && compareText.toLowerCase().contains(lowerCaseFilterText)
                            && !liKeyItemMap.containsKey(listItem.key)) {
                            retVal.add(listItem);
                            liKeyItemMap.put(listItem.key, listItem);
                        }
                    }
                }
            }
        } else {
            retVal = items;
        }
        return retVal;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Will register the list search receiver and set the <code>listSearchReceiver</code> member.
     */
    private void registerListSearchReceiver() {
        if (listSearchReceiver == null) {
            listSearchReceiver = new GovListSearchReceiver(this);
            getApplicationContext().registerReceiver(listSearchReceiver, listSearchFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerListSearchReceiver: listSearchReceiver is not null!");
        }
    }

    /**
     * Will unregister a list search receiver and clear the <code>listSearchReceiver</code> reference.
     */
    private void unregisterListSearchReceiver() {
        if (listSearchReceiver != null) {
            getApplicationContext().unregisterReceiver(listSearchReceiver);
            listSearchReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterListSearchReceiver: listSearchReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> that handles the result of a list search.
     * 
     * @author andy
     */
    class GovListSearchReceiver extends BroadcastReceiver {

        private final String CLS_TAG = GovListSearch.CLS_TAG + "." + GovListSearchReceiver.class.getSimpleName();
        // A reference to the activity.
        private GovListSearch activity;

        // A reference to the exchange rate request.
        private GovSearchListRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ListSearchReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        GovListSearchReceiver(GovListSearch activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(GovListSearch activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.listSearchRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the list search request object associated with this broadcast receiver.
         * 
         * @param request
         *            the list search request object associated with this broadcast receiver.
         */
        void setRequest(GovSearchListRequest request) {
            this.request = request;
        }

        /**
         * Receive notification that something has been updated. This method may be called any number of times while the Activity
         * is running.
         */
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                if (activity.externalRequest) {
                    activity.dismissDialog(DIALOG_FETCHING_EXTERNAL_LIST_KEY);
                }

                // Unregister the receiver.
                activity.unregisterListSearchReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                    Const.REPLY_STATUS_SUCCESS)) {
                                    GovAppMobile app = (GovAppMobile) activity.getApplication();
                                    GovSearchListResponse searchListResponse = app.getGovSearchListResponse();
                                    SearchResultsAdapter adapter = (SearchResultsAdapter) activity.searchResultsList
                                        .getAdapter();

                                    if (searchListResponse != null) {
                                        if (searchListResponse.tmFomField != null) {
                                            ArrayList<ListItem> listItems = searchListResponse.tmFomField.getSearchableStaticList();
                                            if (listItems != null) {
                                                if (!activity.externalRequest) {
                                                    // Filter out any exclude items if necessary
                                                    List<ListItem> items;
                                                    if (activity.excludeKeys != null) {
                                                        items = new ArrayList<ListItem>(listItems.size());
                                                        int size = activity.excludeKeys.length;
                                                        for (ListItem li : listItems) {
                                                            boolean keep = true;
                                                            for (int i = 0; i < size; i++) {
                                                                if (activity.excludeKeys[i].equals(li.key)) {
                                                                    keep = false;
                                                                    break;
                                                                }
                                                            }
                                                            if (keep) {
                                                                items.add(li);
                                                            }
                                                        }

                                                        // Stuff the new list back into the response since the whole response is
                                                        // cached below
                                                        listItems = (ArrayList<ListItem>) items;
                                                    } else {
                                                        // TODO MRU
                                                        items = new ArrayList<ListItem>(listItems);
                                                        // remove dups from all kind of search listitems.
                                                        LinkedHashMap<String, ListItem> lhm = new LinkedHashMap<String, ListItem>();
                                                        for (ListItem listItem : items) {

                                                            if (!(lhm.containsKey(listItem.key))) {
                                                                lhm.put(listItem.key, listItem);
                                                            }

                                                        }// end of for
                                                        items.clear();
                                                        Iterator<String> itr = lhm.keySet().iterator();
                                                        while (itr.hasNext()) {
                                                            items.add(lhm.get(itr.next()));
                                                        }// end of while
                                                    }

                                                    items = activity.filterItems(items, activity.currentSearchText, true);
                                                    SharedPreferences prefs = PreferenceManager
                                                        .getDefaultSharedPreferences(activity);
                                                    String userId = prefs.getString(Const.PREF_USER_ID, null);

                                                    List<ListItem> mruList = null;
                                                    if (userId != null) {
                                                        mruList = getDBListForCurListItem(userId, fieldId);
                                                        mruList = activity.filterItems(mruList, activity.currentSearchText,
                                                            false);
                                                    } else {
                                                        mruList = null;
                                                        Log.e(Const.LOG_TAG,
                                                            CLS_TAG
                                                                + ".filterPerCommonCurrencies: user id is null; can not perform mru");
                                                    }

                                                    if (mruList != null && !mruList.isEmpty()) {
                                                        // find out likey and put it into database.
                                                        List<ListItem> resultMru = new ArrayList<ListItem>(mruList.size());
                                                        for (ListItem mru : mruList) {
                                                            for (ListItem listItem : items) {
                                                                if (mru.code.equalsIgnoreCase(listItem.code)) {
                                                                    mru = listItem;
                                                                    resultMru.add(mru);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        if (resultMru != null && !resultMru.isEmpty()) {
                                                            CategoryListItem mruCategory = new CategoryListItem(activity
                                                                .getText(R.string.expense_type_category_mru).toString());
                                                            resultMru.add(0, mruCategory);
                                                            CategoryListItem otherCat = new CategoryListItem(activity.getText(
                                                                R.string.other).toString());
                                                            items.add(0, otherCat);
                                                            items.addAll(0, resultMru);
                                                        }
                                                    }

                                                    adapter.updateListItems(items);
                                                    // update cache for roatation
                                                    listItems = (ArrayList<ListItem>) items;
                                                } else {
                                                    // External request. Just complete the selection.
                                                    ListItem selectedItem = searchListResponse.tmFomField.getSearchableStaticList().get(0);
                                                    activity.resultIntent.putExtra(
                                                        Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY, selectedItem.key);
                                                    activity.resultIntent.putExtra(Const.EXTRA_COMBO_BOX_ACTION,
                                                        Const.COMBO_BOX_LIST_SELECTION);
                                                    activity.resultIntent.putExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT,
                                                        activity.currentSearchText);
                                                    activity.resultIntent
                                                        .putExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CODE,
                                                            selectedItem.code);
                                                    activity.setResult(RESULT_OK, activity.resultIntent);
                                                    activity.finish();
                                                }
                                            } else {
                                                // Clear the list since there may be previous results in there.
                                                adapter.updateListItems(null);
                                                if (activity.externalRequest) {
                                                    Log.e(Const.LOG_TAG, CLS_TAG
                                                        + ".onReceive: fetching external list item failed!");
                                                }
                                            }
                                        } else {
                                            Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".onReceive: govlistresponse.tmFormField is null!");
                                        }
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".onReceive: govlistresponse is null!");
                                    }
                                } else {
                                    if (request != null && !request.isCanceled()) {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                    }
                                }
                            } else {
                                if (request != null && !request.isCanceled()) {
                                    Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                            + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                    activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                CLS_TAG + ".onReceive: service request error -- "
                                    + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear the request reference.
                activity.listSearchRequest = null;

            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    private List<ListItem> getDBListForCurListItem(String userId, String fieldId) {
        if (userId == null) {
            return null;
        }
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        List<ListItem> dblist = expEntCache.getListItemFromDB(userId, fieldId);
        return dblist;
    }

    class SearchResultClickListener implements AdapterView.OnItemClickListener {

        private final String CLS_TAG = GovListSearch.CLS_TAG + SearchResultClickListener.class.getSimpleName();

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
            ListItem selectedItem = adapter.getItem(position);
            if (selectedItem != null) {
                if (selectedItem.external != null && selectedItem.external == true) {
                    externalRequest = true;
                    externalId = selectedItem.code;
                    searchDelayHandler.removeCallbacks(searchDelayRunnable);
                    searchDelayHandler.post(searchDelayRunnable);
                } else {
                    resultIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY, selectedItem.key);
                    resultIntent.putExtra(Const.EXTRA_COMBO_BOX_ACTION, Const.COMBO_BOX_LIST_SELECTION);
                    resultIntent.putExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT, currentSearchText);
                    resultIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CODE, selectedItem.code);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: selectedItem is null!");
            }
        }
    }

    protected class SearchResultsAdapter extends BaseAdapter {

        private Context context;
        private boolean showCodes;
        private List<ListItem> listItems;
        private final List<ListItem> emptyListItems = new ArrayList<ListItem>(1);
        private StringBuilder strBldr = new StringBuilder();

        public SearchResultsAdapter(Context context, boolean showCodes) {
            this.context = context;
            this.showCodes = showCodes;
            listItems = emptyListItems;
        }

        public void updateListItems(List<ListItem> listItems) {
            if (listItems == null) {
                this.listItems = emptyListItems;
            } else {
                this.listItems = listItems;
            }
            notifyDataSetChanged();
        }

        public void clearListItems() {
            listItems = emptyListItems;
            notifyDataSetChanged();
        }

        public int getCount() {
            return listItems.size();
        }

        public ListItem getItem(int position) {
            return listItems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            boolean enabled = false;
            ListItem item = listItems.get(position);
            enabled = !((item instanceof CategoryListItem));
            return enabled;
        };

        @Override
        public int getViewTypeCount() {
            return 2;
        };

        @Override
        public int getItemViewType(int position) {
            ListItem item = listItems.get(position);
            int returnValue = 0;
            if (item instanceof CategoryListItem) {
                returnValue = 1;
            }
            return returnValue;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout row = (RelativeLayout) convertView;

            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                row = (RelativeLayout) inflater.inflate(R.layout.expense_list_search_row, null);
            }

            ListItem listItem = getItem(position);
            TextView tv = (TextView) row.findViewById(R.id.listItemName);
            if (listItem.code != null && listItem.code.length() > 0) {
                strBldr.setLength(0);
                if (showCodes) {
                    // Show the codes if so told. This satisfies Pfizer per MOB-10224.
                    strBldr.append('(');
                    strBldr.append(listItem.code);
                    strBldr.append(") ");
                }
                strBldr.append(listItem.text);
                tv.setText(strBldr.toString());
            } else {
                tv.setText(listItem.text);
            }

            if ((position % 2) == 0) {
                tv.setBackgroundColor(colorBlueStripe);
                if (listItem instanceof CategoryListItem) {
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                }
            } else {
                tv.setBackgroundColor(colorWhiteStripe);
                if (listItem instanceof CategoryListItem) {
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
            return row;
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - end
    // ///////////////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean retVal = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            // If we have an outstanding request, cancel it.
            if (listSearchRequest != null) {
                // Unregister the receiver.
                if (listSearchReceiver != null) {
                    unregisterListSearchReceiver();
                }
                // Cancel the request.
                listSearchRequest.cancel();
            }
            // Set the result, even though the activity was canceled.
            setResult(RESULT_CANCELED, resultIntent);
            retVal = super.onKeyDown(keyCode, event);
            break;
        }
        default: {
            retVal = super.onKeyDown(keyCode, event);
            break;
        }
        }
        return retVal;
    }

}

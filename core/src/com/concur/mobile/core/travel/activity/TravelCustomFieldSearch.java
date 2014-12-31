package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.TravelCustomFieldValueSpinnerItem;
import com.concur.mobile.core.travel.service.SearchTravelCustomFieldValues;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.core.view.SpinnerItem;

/**
 * 
 * @author RatanK
 * 
 */
public class TravelCustomFieldSearch extends BaseActivity {

    public static final String CLS_TAG = TravelCustomFieldSearch.class.getSimpleName();

    /** list item type */
    private static final int CUSTOM_FIELD_LIST_ITEM_VIEW_TYPE = 0;
    private static final String EXTRA_SEARCH_RESULTS_LIST = "searchResultsList";
    private static final String EXTRA_STATIC_LIST_CACHE = "staticListCache";
    private static final String EXTRA_SEARCH_RESULTS_RECEIVER = "searchResultsReceiver";
    private static final int TRAVEL_CUSTOM_FIELDS_PROGRESS_DIALOG = TravelBaseActivity.DIALOG_ID_BASE + 1;
    private static final int TRAVEL_CUSTOM_FIELDS_FAILURE_DIALOG = TravelBaseActivity.DIALOG_ID_BASE + 2;

    private boolean getFromIntent;
    protected String currentSearchText;
    protected String attributeId;
    protected EditText searchText;
    protected String searchTitle;
    private List<ListItem> staticList;
    private List<ListItem> staticListCache;
    private List<ListItem> resultsList;
    private TravelCustomFieldValueSpinnerItem prvSelectedItem;

    BaseAsyncResultReceiver searchResultsReceiver;
    // TravelCustomFieldSearchListener searchResultsListener;
    ListItemAdapter<ListItem> listItemAdapter;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFromIntent = true;

        setContentView(R.layout.expense_list_search);

        // Restore any receivers.
        restoreReceivers();

        if (retainer != null) {
            // Initialize from a previously saved state
            if (retainer.contains(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE)) {
                searchTitle = (String) retainer.get(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE);
                attributeId = (String) retainer.get(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID);
            }
            if (retainer.contains(EXTRA_SEARCH_RESULTS_LIST)) {
                resultsList = (List<ListItem>) retainer.get(EXTRA_SEARCH_RESULTS_LIST);
            }
            if (retainer.contains(Const.EXTRA_SEARCH_SELECTED_ITEM)) {
                prvSelectedItem = (TravelCustomFieldValueSpinnerItem) retainer.get(Const.EXTRA_SEARCH_SELECTED_ITEM);
            }
            if (retainer.contains(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST)) {
                getFromIntent = false;
                staticList = (List<ListItem>) retainer.get(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST);
            }
            if (retainer.contains(EXTRA_STATIC_LIST_CACHE)) {
                staticListCache = (List<ListItem>) retainer.get(EXTRA_STATIC_LIST_CACHE);
            }
        }

        buildView();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (retainer != null) {
            if (searchResultsReceiver != null) {
                searchResultsReceiver.setListener(null);
                retainer.put(EXTRA_SEARCH_RESULTS_RECEIVER, searchResultsReceiver);
            }
            retainer.put(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, searchTitle);
            retainer.put(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, attributeId);
            if (resultsList != null) {
                retainer.put(EXTRA_SEARCH_RESULTS_LIST, resultsList);
            }
            if (prvSelectedItem != null) {
                retainer.put(Const.EXTRA_SEARCH_SELECTED_ITEM, prvSelectedItem);
            }
            if (staticList != null) {
                retainer.put(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST, staticList);
            }
            if (staticListCache != null) {
                retainer.put(EXTRA_STATIC_LIST_CACHE, staticListCache);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        super.onStop();
        unRegisterReceivers();
    }

    protected void restoreReceivers() {
        if (retainer != null) {
            // Check if due to an orientation change, there's a saved search results receiver
            if (retainer.contains(EXTRA_SEARCH_RESULTS_RECEIVER)) {
                searchResultsReceiver = (BaseAsyncResultReceiver) retainer.get(EXTRA_SEARCH_RESULTS_RECEIVER);
                searchResultsReceiver.setListener(new TravelCustomFieldSearchListener());
            }
        }
    }

    protected void unRegisterReceivers() {
        if (searchResultsReceiver != null) {
            searchResultsReceiver.setListener(null);
        }
        searchResultsReceiver = null;
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dlg = null;
        switch (id) {
        case TRAVEL_CUSTOM_FIELDS_PROGRESS_DIALOG: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(R.string.dlg_travel_retrieve_custom_fields_progress_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            dlg = progDlg;
            dlg.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    unRegisterReceivers();
                }
            });
            break;
        }
        case TRAVEL_CUSTOM_FIELDS_FAILURE_DIALOG: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_travel_booking_info_title);
            dlgBldr.setMessage(R.string.travel_booking_info_unavailable_message);
            dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    unRegisterReceivers();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        }
        return dlg;
    }

    protected void buildView() {
        if (getFromIntent) {
            Intent intent = getIntent();
            searchTitle = intent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE);
            attributeId = intent.getStringExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID);

            if (intent.hasExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST)) {
                List<SpinnerItem> sItemList = (ArrayList<SpinnerItem>) intent
                        .getSerializableExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST);
                if (sItemList != null && sItemList.size() > 0) {
                    if (intent.hasExtra(Const.EXTRA_SEARCH_SELECTED_ITEM)) {
                        prvSelectedItem = (TravelCustomFieldValueSpinnerItem) intent
                                .getSerializableExtra(Const.EXTRA_SEARCH_SELECTED_ITEM);
                    }
                    staticList = convertSpinnerItemArrayToList(sItemList);
                }
            }
        }

        if (staticListCache == null) {
            staticListCache = new ArrayList<ListItem>(staticList);
        }

        // set the title
        TextView txtView = (TextView) findViewById(R.id.title);
        if (txtView != null) {
            txtView.setText(searchTitle);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't locate title text view!");
        }

        // set the list items
        if (resultsList != null && resultsList.size() > 0) {
            // show the search results list
            configureListItems(resultsList);
        } else {
            // show the static list
            configureStaticListWithPrevSelection(staticList);
        }

        // set controls
        configureControls();
    }

    protected void configureControls() {
        // Grab all our controls
        searchText = (EditText) findViewById(R.id.listSearchEdit);

        // Listen for typing in the search filter
        searchText.addTextChangedListener(new SearchTextWatcher());

        // Listen for typing in the search filter
        // MOB-8910 HTC keyboard doesnt not support IME option instead it support Enter.
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || ((event != null) && ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))) {

                    doSearch();
                    return true;
                }
                return false;
            }
        });

        // Install a touch-listener that determines whether the right compound drawable has been
        // clicked on. This will kick-off a search.
        searchText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Is the search glass showing?
                Drawable searchGlass = searchText.getCompoundDrawables()[2];
                if (searchGlass == null)
                    return false;
                // Start search only for up touches.
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                // Detect whether the touch event
                if (event.getX() > searchText.getWidth() - searchText.getPaddingRight()
                        - searchGlass.getIntrinsicWidth()) {
                    // Kick-off the search.
                    doSearch();
                    return true;
                }
                return false;
            }

        });
    }

    protected void doSearch() {
        currentSearchText = searchText.getText().toString();
        if (currentSearchText != null) {
            if (ConcurCore.isConnected()) {
                if (searchResultsReceiver == null) {
                    searchResultsReceiver = new BaseAsyncResultReceiver(new Handler());
                    searchResultsReceiver.setListener(new TravelCustomFieldSearchListener());
                }

                new SearchTravelCustomFieldValues(this, 1, searchResultsReceiver, attributeId, currentSearchText)
                        .execute();

                showDialog(TRAVEL_CUSTOM_FIELDS_PROGRESS_DIALOG);
                // SearchProgressDialogHandler.show(this);
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    // /**
    // * An extension of <code>DialogFragmentHandler</code> to handle showing/dismissing the search dialog fragment.
    // */
    // static class SearchProgressDialogHandler extends DialogFragmentHandler {
    //
    // private static final String DIALOG_FRAGMENT_ID = "SearchProgress";
    //
    // // Contains the fragment tag for the fragment passed into the <code>show</code> method.
    // // This tag value is used to look-up the fragment below when this callback handler needs
    // // to refer to the current fragment.
    // private String fragTag;
    //
    // public static void show(Fragment frag) {
    // // Construct the callback handler.
    // SearchProgressDialogHandler dlgHndlr = new SearchProgressDialogHandler();
    // // Construct and show the dialog fragment.
    // String message = frag.getText(R.string.dlg_travel_retrieve_custom_fields_progress_message).toString();
    // ProgressDialogFragment dlgFrag = DialogFragmentFactory.getProgressDialog(message, true, true, dlgHndlr);
    // dlgFrag.show(frag.getActivity().getSupportFragmentManager(), DIALOG_FRAGMENT_ID);
    // // Hang onto the tag of <code>frag</code>.
    // dlgHndlr.fragTag = frag.getTag();
    // }
    //
    // public static void dismiss(Fragment frag) {
    // dismiss(frag.getActivity().getSupportFragmentManager(), DIALOG_FRAGMENT_ID);
    // }
    //
    // public void onCancel(AsyncReplyListener listener) {
    // if (listener != null) {
    // listener.cleanup();
    // }
    // }
    // }

    protected void clearListItems() {
        listItemAdapter.getItems().clear();
        listItemAdapter.notifyDataSetChanged();
    }

    /**
     * Will update the list UI with list of items
     */
    protected void updateListUI() {
        TravelCustomField customField = getTravelCustomField();
        if (customField == null || customField.getFieldValues().size() == 0) {
            clearListItems();
        } else {
            List<TravelCustomFieldValueSpinnerItem> fldValues = customField.getFieldValues();
            if (fldValues != null && fldValues.size() > 0) {
                resultsList = new ArrayList<ListItem>(fldValues.size());
                for (TravelCustomFieldValueSpinnerItem spItem : fldValues) {
                    resultsList.add(new TravelCustomFieldListItem(spItem, CUSTOM_FIELD_LIST_ITEM_VIEW_TYPE));
                }
                configureListItems(resultsList);
            }
        }
    }

    /**
     * show the cached static list
     */
    protected void updateListUIWithStatic() {
        configureStaticListWithPrevSelection(staticListCache);
    }

    protected void configureStaticListWithPrevSelection(List<ListItem> listItems) {
        moveSelectedItemToTopOfList(prvSelectedItem, listItems);
        configureListItems(listItems);
    }

    protected void configureListItems(List<ListItem> listItems) {
        ListView listView = (ListView) findViewById(R.id.listSearchResults);
        if (listView != null) {

            // configure the colour stripes
            configureColourStripes(listItems);

            // create a new list and provide it to adapter, otherwise clearListItems will clear the original list
            listItemAdapter = new ListItemAdapter<ListItem>(this, new ArrayList<ListItem>(listItems));
            listView.setAdapter(listItemAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TravelCustomFieldListItem listItem = (TravelCustomFieldListItem) listItemAdapter.getItem(position);

                    // send back the selected item to the caller...goes to TravelBaseActivity onActivityResult()...which invokes
                    // the SearchListFormFieldView.onActivityResult
                    if (listItem != null && listItem.getSelectedItem() != null) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Const.EXTRA_SEARCH_SELECTED_ITEM, listItem.getSelectedItem());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: selectedItem is null!");
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureListItems: cannot locate ListView listSearchResults");
        }
    }

    protected TravelCustomField getTravelCustomField() {
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        return core.getTravelCustomField();
    }

    private List<ListItem> convertSpinnerItemArrayToList(List<SpinnerItem> sItemList) {
        List<ListItem> listItems = new ArrayList<ListItem>();
        for (SpinnerItem item : sItemList) {
            TravelCustomFieldValueSpinnerItem spItem = (TravelCustomFieldValueSpinnerItem) item;
            listItems.add(new TravelCustomFieldListItem(spItem, CUSTOM_FIELD_LIST_ITEM_VIEW_TYPE));
        }
        return listItems;
    }

    private void moveSelectedItemToTopOfList(TravelCustomFieldValueSpinnerItem prvSelectedItem, List<ListItem> listItems) {
        if (prvSelectedItem != null) {
            TravelCustomFieldListItem prvSelectedListItem = new TravelCustomFieldListItem(prvSelectedItem,
                    CUSTOM_FIELD_LIST_ITEM_VIEW_TYPE);

            // remove if exists in the current list
            if (listItems.contains(prvSelectedListItem)) {
                listItems.remove(prvSelectedListItem);
            }

            // add at the top
            listItems.add(0, prvSelectedListItem);
        }
    }

    /**
     * provide colour strips to the list items
     * 
     * @param listItems
     */
    private void configureColourStripes(List<ListItem> listItems) {
        int count = 1;
        int colourId = -1;
        for (ListItem item : listItems) {
            if ((count % 2) == 0) {
                colourId = getResources().getColor(R.color.ListStripeBlue);
            } else {
                colourId = getResources().getColor(R.color.ListStripeWhite);
            }
            ((TravelCustomFieldListItem) item).setColourId(colourId);
            count++;
        }
    }

    class SearchTextWatcher implements TextWatcher {

        public void afterTextChanged(Editable s) {
            // Display the cached static list if the search text is cleared by the user
            if (s.length() == 0) {
                updateListUIWithStatic();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

        }
    }

    /**
     * Listener used for displaying the search result list
     */
    private class TravelCustomFieldSearchListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestSuccess in TravelCustomFieldSearchListener...");
            dismissDialog(TRAVEL_CUSTOM_FIELDS_PROGRESS_DIALOG);
            // SearchProgressDialogHandler.dismiss(activity.getSupportFragmentManager().findFragmentByTag(
            // SearchProgressDialogHandler.DIALOG_FRAGMENT_ID));
            updateListUI();
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestFail in TravelCustomFieldSearchListener...");
            dismissDialog(TRAVEL_CUSTOM_FIELDS_PROGRESS_DIALOG);
            showDialog(TRAVEL_CUSTOM_FIELDS_FAILURE_DIALOG);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestCancel in TravelCustomFieldSearchListener...");
            dismissDialog(TRAVEL_CUSTOM_FIELDS_PROGRESS_DIALOG);
        }

        @Override
        public void cleanup() {
            searchResultsReceiver.setListener(null);
            searchResultsReceiver = null;
        }
    }

}

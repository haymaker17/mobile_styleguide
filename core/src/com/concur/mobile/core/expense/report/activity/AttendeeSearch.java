/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.report.data.AttendeeSearchField;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.service.AttendeeFormRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSaveReply;
import com.concur.mobile.core.expense.report.service.AttendeeSaveRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSearchFieldsRequest;
import com.concur.mobile.core.expense.report.service.AttendeeSearchReply;
import com.concur.mobile.core.expense.report.service.AttendeeSearchRequest;
import com.concur.mobile.core.expense.report.service.ExtendedAttendeeSearchReply;
import com.concur.mobile.core.expense.report.service.ExtendedAttendeeSearchRequest;
import com.concur.mobile.core.expense.service.SearchListRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.util.ReportUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.core.view.SpinnerItem;

/**
 * An activity supporting interactive attendee search.
 */
public class AttendeeSearch extends BaseActivity {

    private static final String CLS_TAG = AttendeeSearch.class.getSimpleName();

    // Store key values.
    private static final String ATTENDEE_SEARCH_MODE_KEY = "attendee.search.mode";

    // Favorites store keys.
    private static final String ATTENDEE_FAVORITES_SEARCH_TEXT_KEY = "search.text";
    private static final String ATTENDEE_FAVORITES_LIST_ITEMS_KEY = "attendee.favorites.results";

    // Advanced store keys.
    private static final String ATTENDEE_ADVANCED_VIEW_MODE_KEY = "attendee.advanced.view.mode";
    private static final String ATTENDEE_ADVANCED_LIST_ITEMS_KEY = "attendee.advanced.results";
    private static final String ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEM = "attendee.advanced.attendee.type.item";
    private static final String ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEMS = "attendee.advanced.attendee.type.items";
    private static final String ATTENDEE_ADVANCED_CHECKED_ATTENDEES = "attendee.advanced.checked.attendees";
    private static final String ATTENDEE_ADVANCED_EXTERNAL_ATTENDEE_SAVE_LIST_KEY = "attendee.advanced.external.attendee.save.list";
    private static final String ATTENDEE_ADVANCED_EXTERNAL_ATTENDEE_SAVE_INDEX_KEY = "attendee.advanced.external.attendee.save.index";
    private static final String ATTENDEE_ADVANCED_ATTENDEE_TYPE_FORM_KEY = "attendee.advanced.attendee.type.form";

    // Receiver store keys.
    private static final String ATTENDEE_TYPE_RECEIVER_KEY = "attendee.type.receiver";
    private static final String ATTENDEE_FIELDS_RECEIVER_KEY = "attendee.fields";
    private static final String ATTENDEE_ADVANCED_SEARCH_RECEIVER_KEY = "advanced.attendee.search.receiver";
    private static final String ATTENDEE_FAVORITES_SEARCH_RECEIVER_KEY = "attendee.favorites.search.receiver";
    private static final String ATTENDEE_FORM_RECEIVER_KEY = "attendee.form.receiver";
    private static final String ATTENDEE_SAVE_RECEIVER_KEY = "attendee.save.receiver";

    private static final int DIALOG_SELECT_ATTENDEE_TYPE = 1;
    private static final int DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_PROGRESS = 2;
    private static final int DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_FAILED = 3;
    private static final int DIALOG_NO_ATTENDEE_TYPES = 4;
    private static final int DIALOG_SEARCH_ATTENDEES_PROGRESS = 5;
    private static final int DIALOG_SEARCH_ATTENDEES_FAILED = 6;
    private static final int DIALOG_NO_ATTENDEE_SEARCH_RESULTS = 7;
    private static final int DIALOG_ADD_ATTENDEES_PROGRESS = 8;
    private static final int DIALOG_ADD_ATTENDEES_FAILED = 9;

    // Defines the view mode.
    public static enum AdvancedViewMode {
        None, Fields, Results
    };

    protected AdvancedViewMode advancedViewMode;

    // Defines the search modes.
    public static enum SearchMode {
        None, Favorites, Advanced
    };

    protected SearchMode searchMode;
    protected Button modeFavorites;
    protected Button modeAdvanced;
    protected Button searchButton;

    protected EditText searchText;
    protected String searchTextStr;

    protected String currentSearchText;

    protected int minSearchLength = 3;

    // Contains a reference to the favorites search results list view.
    protected ListView favoritesResultsList;
    // Contains a reference to the favorites search adapter.
    protected ListItemAdapter<AttendeeListItem> favoritesSearchAdapter;
    // Contains a reference to the advanced search field view.
    protected View advancedSearchFieldsView;
    // Contains a reference to the advanced search results list view.
    protected ListView advancedResultsList;
    // Contains a reference to the advanced search adapter.
    protected ListItemAdapter<AttendeeListItem> advancedSearchAdapter;
    // Contains a reference to the advanced search results message.
    protected TextView advancedResultsMessage;
    // Contains a hashset of attendees representing checked attendees (advanced
    // search supports multi-selection).
    protected HashSet<ExpenseReportAttendee> checkedAttendees;
    // Contains a listener to handle check/un-check events on attendees.
    protected OnCheckChange checkChangeListener;
    // Contains a reference to a view pager permitting the end-user to switch
    // between advanced fields/results views.
    protected ViewPager advancedFieldsResultsPager;
    // Contains a reference to a pager adapter to provide the pager views.
    protected AdvancedFieldsResultsPagerAdapter advancedFieldsResultsPagerAdapter;
    // Contains a refernece to a pager view listener to detect change in views.
    protected AdvancedFieldsResultsPagerListener advancedFieldsResultsPagerListener;

    // Contains the attendee type information receiver.
    private AttendeeTypeReceiver attendeeTypeReceiver;
    // Contains the filter used to register the attendee type receiver.
    private IntentFilter attendeeTypeFilter;
    // Contains a reference to an outstanding request to retrieve attendee type
    // information.
    private SearchListRequest attendeeTypeRequest;

    /*
     * NOTE: The two receivers below, AttendeeFormReceiver/AttendeeSaveReceiver are used when the end-user selects 1+ attendees
     * with external ID's. The client must: 1. Download the form based on the selected attendeeType 2. For each selected attendee
     * with an external ID, copy the form field values from the selected attendee into the corresponding fields in the downloaded
     * form and make a call to save the attendee. 3. The result of the "save attendee" request is the attendee that will be
     * returned to the calling activity in place of the attendee that was returned from the extended search. 4. If there are
     * duplicates, "use the attendee with the same externalId or the first duplicate" from Yiwen's comments in MOB-9693. 5. If any
     * of the "save attendees" fails, then display an error dialog and stay on the advanced results screen.
     */
    // Contains the attendee form receiver.
    private AttendeeFormReceiver attendeeFormReceiver;
    // Contains the filter used to register the attendee form receiver.
    private IntentFilter attendeeFormFilter;
    // Contains an outstanding attendee form request.
    private AttendeeFormRequest attendeeFormRequest;
    // Contains a reference to the attendee type form used in saving selected
    // attendees with external ID's.
    private ExpenseReportAttendee attendeeTypeForm;
    // Contains the attendee save receiver.
    private AttendeeSaveReceiver attendeeSaveReceiver;
    // Contains the filter used to register the attendee save receiver.
    private IntentFilter attendeeSaveFilter;
    // Contains an outstanding attendee save request.
    private AttendeeSaveRequest attendeeSaveRequest;
    // Contains the list of selected attendees from an advanced search.
    // This reference is maintained due to needing to perform a "save" operation
    // on selected attendees that have an external ID.
    protected List<ExpenseReportAttendee> advancedSearchSelectedAttendees;
    // Contains the index into <code>advancedSearchSelectedAttendees</code> that
    // is currently being
    // saved.
    protected Integer advancedSearchCurrentAttendeeSaveIndex;

    // Contains the broadcast receiver to handle list search results.
    private FavoritesAttendeeSearchReceiver favoritesAttendeeSearchReceiver;
    // Contains a reference to a filter used to register the list search
    // receiver.
    private final IntentFilter favoritesAttendeeSearchFilter = new IntentFilter(
            Const.ACTION_EXPENSE_ATTENDEE_SEARCH_UPDATED);
    // A reference to an outstanding request.
    private AttendeeSearchRequest favoritesAttendeeSearchRequest;

    // Contains the broadcast receiver to handle list search results.
    private AdvancedAttendeeSearchReceiver advancedAttendeeSearchReceiver;
    // Contains a reference to a filter used to register the list search
    // receiver.
    private final IntentFilter advancedAttendeeSearchFilter = new IntentFilter(
            Const.ACTION_EXPENSE_EXTENDED_ATTENDEE_SEARCH_UPDATED);
    // A reference to an outstanding request.
    private ExtendedAttendeeSearchRequest advancedAttendeeSearchRequest;

    // Contains the broadcast receiver to handle attendee search fields results.
    private AttendeeSearchFieldReceiver attendeeSearchFieldReceiver;
    // Contains a reference to a filter used to register the attendee search
    // field receiver.
    private final IntentFilter attendeeSearchFieldFilter = new IntentFilter(
            Const.ACTION_EXPENSE_ATTENDEE_SEARCH_FIELDS_DOWNLOADED);
    // Contains a reference to an outstanding request.
    private AttendeeSearchFieldsRequest attendeeSearchFieldRequest;

    // Contains the list of attendee keys to be excluded from the search.
    protected List<String> excAtnKeys;

    // Contains the list of attendee external IDs to be excluded from the
    // search.
    protected List<String> excAtnExtIds;

    // Contains a map from an attendee type key value to a list of constructed
    // form field view objects.
    protected Map<String, List<FormFieldView>> atnTypeFrmFldViewMap;
    // Contains the list of attendee type spinner items.
    protected SpinnerItem[] attendeeTypeItems;
    // Contains the currently selected attendee type.
    protected SpinnerItem curAttendeeTypeItem;

    // Contains a reference to a form field view listener.
    private AdvancedAttendeeSearchFormFieldViewListener frmFldViewListener;
    // Contains a reference to the expense report cache.
    protected IExpenseReportCache expRepCache;
    // Contains a reference to the expense report.
    protected ExpenseReport expRep;
    // Contains a reference to a detailed expense report entry.
    protected ExpenseReportEntryDetail expRepEntDet;

    // Contains a reference to the last saved instance state.
    protected Bundle lastSavedInstanceState;

    protected static final int ATTENDEE_ADVANCE_PREVIEW_REQ_CODE = 1000;

    protected void showFavoriteSelectionPrompt() {
        View view = findViewById(R.id.footer_text_one);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    protected void hideFavoriteSelectionPrompt() {
        View view = findViewById(R.id.footer_text_one);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    protected void showSearchButton() {
        View view = findViewById(R.id.footer_button_one);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    protected void hideSearchButton() {
        View view = findViewById(R.id.footer_button_one);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    protected void selectModeButton(View v) {
        if (!v.isSelected()) {
            // Only do something if we are clicking a different button
            final int id = v.getId();
            if (id == R.id.attendee_search_favorites) {
                modeFavorites.setSelected(true);
                modeAdvanced.setSelected(false);
                searchMode = SearchMode.Favorites;
                hideSearchButton();
                if (favoritesResultsList.getCount() > 0) {
                    showFavoriteSelectionPrompt();
                } else {
                    hideFavoriteSelectionPrompt();
                }
            } else if (id == R.id.attendee_search_advanced) {
                modeFavorites.setSelected(false);
                modeAdvanced.setSelected(true);
                searchMode = SearchMode.Advanced;
                hideFavoriteSelectionPrompt();
                showSearchButton();
            } else {
                modeFavorites.setSelected(true);
                modeAdvanced.setSelected(false);
                searchMode = SearchMode.Favorites;
                hideSearchButton();
                if (favoritesResultsList.getCount() > 0) {
                    showFavoriteSelectionPrompt();
                } else {
                    hideFavoriteSelectionPrompt();
                }
            }
        }
        updateUIForMode();
    }

    protected void updateUIForMode() {
        switch (searchMode) {
        case Favorites: {
            flipToFavorites();
            break;
        }
        case Advanced: {
            flipToAdvanced();
            if (!isAttendeeTypeLoaded()) {
                sendAttendeeTypeRequest();
            } else if (!isAttendeeSearchFieldLoaded()) {
                sendAttendeeSearchFieldRequest(true);
            }
            break;
        }
        default: {
            flipToFavorites();
            break;
        }
        }
    }

    /**
     * Determines whether the appropriate set of attendee types have been loaded.
     * 
     * @return returns whether or not the appropriate set of attendee types have been loaded.
     */
    protected boolean isAttendeeTypeLoaded() {
        return (getAttendeeTypes() != null);
    }

    /**
     * Gets the list of attendee types appropriate for the report policy key and expense key.
     * 
     * @return the list of attendee types appropriate for the report policy key and expense key.
     */
    protected List<ListItem> getAttendeeTypes() {
        List<ListItem> attendeeTypes = null;
        ConcurCore concurCore = getConcurCore();
        IExpenseReportCache expRepCache = concurCore.getExpenseActiveCache();
        ExpenseType expType = ExpenseType.findExpenseType(expRep.polKey, expRepEntDet.expKey);
        attendeeTypes = expRepCache.getAttendeeTypes(expType, expRepCache.getSearchAttendeeTypes());
        return attendeeTypes;
    }

    /**
     * Determines whether the set of attendee search fields have been loaded.
     * 
     * @return returns whether or not the set of attendee search fields have been loaded.
     */
    protected boolean isAttendeeSearchFieldLoaded() {
        return (getAttendeeSearchFields() != null);
    }

    protected List<AttendeeSearchField> getAttendeeSearchFields() {
        List<AttendeeSearchField> attSrchFlds = null;
        IExpenseReportCache expRepActCache = getConcurCore().getExpenseActiveCache();
        attSrchFlds = expRepActCache.getAttendeeSearchFields();
        return attSrchFlds;
    }

    public void onClick(View view) {
        final int id = view.getId();

        if (id == R.id.attendee_search_favorites || id == R.id.attendee_search_advanced) {
            selectModeButton(view);
        } else if (id == R.id.footer_button_one) {
            switch (searchMode) {
            case Advanced: {
                switch (advancedViewMode) {
                case Fields: {
                    if (curAttendeeTypeItem != null) {
                        List<FormFieldView> frmFldViews = atnTypeFrmFldViewMap.get(curAttendeeTypeItem.id);
                        if (frmFldViews != null) {
                            // Iterate over the various form field view objects
                            // and commit their values to
                            // the underlying ExpenseReportFormField objects.
                            List<ExpenseReportFormField> frmFlds = new ArrayList<ExpenseReportFormField>(
                                    frmFldViews.size());
                            for (FormFieldView frmFldView : frmFldViews) {
                                frmFldView.commit();
                                frmFlds.add(frmFldView.getFormField());
                            }
                            sendAdvancedAttendeeSearchRequest(curAttendeeTypeItem.id, frmFlds, excAtnKeys,
                                    expRepEntDet.expKey, expRep.polKey, expRepEntDet.reportEntryKey);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: frmFldViews is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: curAttendeeTypeItem is null!");
                    }
                    break;
                }
                case Results: {
                    if (!checkedAttendees.isEmpty()) {
                        advancedSearchCurrentAttendeeSaveIndex = -1;
                        advancedSearchSelectedAttendees = new ArrayList<ExpenseReportAttendee>();
                        Iterator<ExpenseReportAttendee> atdIterator = checkedAttendees.iterator();
                        int iterIndex = -1;
                        while (atdIterator.hasNext()) {
                            ExpenseReportAttendee expRepAtt = atdIterator.next();
                            ++iterIndex;
                            advancedSearchSelectedAttendees.add(expRepAtt);
                            // If the attendee already has a key, then it's
                            // already been saved within the system and
                            // the client doesn't need to call 'SaveAttendee' to
                            // obtain a key.
                            if (expRepAtt.atnKey == null || expRepAtt.atnKey.length() == 0) {
                                if (expRepAtt.externalId != null && expRepAtt.externalId.length() > 0) {
                                    if (advancedSearchCurrentAttendeeSaveIndex == -1) {
                                        advancedSearchCurrentAttendeeSaveIndex = iterIndex;
                                    }
                                }
                            }
                        }
                        if (advancedSearchCurrentAttendeeSaveIndex != -1) {
                            // At least one attendee has an external ID, so
                            // start the process of constructing
                            // an attendee form in order to call the
                            // 'SaveAttendee' end-point to obtain an attendee
                            // key.
                            ExpenseReportAttendee expRepAtt = advancedSearchSelectedAttendees
                                    .get(advancedSearchCurrentAttendeeSaveIndex);
                            ConcurCore ConcurCore = getConcurCore();
                            ExtendedAttendeeSearchReply attendeeSearchReply = ConcurCore
                                    .getExtendedAttendeeSearchResults();
                            List<ExpenseReportFormField> colDefs = null;
                            if (attendeeSearchReply != null) {
                                colDefs = attendeeSearchReply.columnDefinitions;
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: attendee search reply is null!");
                            }
                            fillFieldsFromColumnDefinitionInfo(expRepAtt, colDefs);
                            sendAttendeeSaveRequest(expRepAtt);
                        } else {
                            // No attendees with external ID's, so just return
                            // the selected list.
                            ConcurCore concurCore = getConcurCore();
                            concurCore.setSelectedAttendees(advancedSearchSelectedAttendees);
                            Intent data = new Intent();
                            data.putExtra(Flurry.PARAM_NAME_VIA, Flurry.PARAM_VALUE_ADVANCED_SEARCH);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    }
                    break;
                }
                }
                break;
            }
            case Favorites: {
                currentSearchText = searchText.getText().toString();
                sendFavoritesAttendeeSearchRequest();
                break;
            }
            case None: {
                // No-op.
                break;
            }
            }
        }
    }

    /**
     * Will update an expense report attendee field information with values from a set of column definitions.
     * 
     * @param expRepAtt
     *            contains the expense report attendee.
     * @param columnDefs
     *            contains a list of form fields describing
     */
    protected void fillFieldsFromColumnDefinitionInfo(ExpenseReportAttendee expRepAtt,
            List<ExpenseReportFormField> columnDefs) {

        if (columnDefs == null || columnDefs.size() == 0) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".fillFieldsFromColumnDefinitionInfo: column definitions are null!");
        }
        if (expRepAtt != null) {
            List<ExpenseReportFormField> dstFrmFlds = expRepAtt.getFormFields();
            if (dstFrmFlds != null) {
                for (ExpenseReportFormField dstFrmFld : dstFrmFlds) {
                    ExpenseReportFormField srcFrmFld = FormUtil.findFieldById(columnDefs, dstFrmFld.getId());
                    if (srcFrmFld != null) {
                        // Copy over field schema from attendee entry form.
                        dstFrmFld.setDataType(srcFrmFld.getDataType());
                        dstFrmFld.setControlType(srcFrmFld.getControlType());
                        // Copy over default value, if current value is empty
                        if ((dstFrmFld.getValue() == null || dstFrmFld.getValue().length() == 0)
                                && (dstFrmFld.getLiKey() == null || dstFrmFld.getLiKey().length() == 0)) {
                            dstFrmFld.setValue(srcFrmFld.getValue());
                            dstFrmFld.setLiKey(srcFrmFld.getLiKey());
                        }
                    } else if (dstFrmFld.getId().equalsIgnoreCase(ExpenseReportFormField.INSTANCE_COUNT_FIELD_ID)
                            || dstFrmFld.getId().equalsIgnoreCase(ExpenseReportFormField.ATTENDEE_TYPE_KEY_FIELD_ID)
                            || dstFrmFld.getId().equalsIgnoreCase(ExpenseReportFormField.CURRENCY_KEY_FIELD_ID)) {
                        // Default to list data type.
                        dstFrmFld.setDataType(ExpenseReportFormField.DataType.LIST);
                        // Set the control type.
                        if (dstFrmFld.getId().equalsIgnoreCase(ExpenseReportFormField.INSTANCE_COUNT_FIELD_ID)) {
                            dstFrmFld.setControlType(ExpenseReportFormField.ControlType.EDIT);
                        } else {
                            dstFrmFld.setControlType(ExpenseReportFormField.ControlType.PICK_LIST);
                        }
                    } else {
                        // Default to data type 'varchar' and control type
                        // 'edit'.
                        dstFrmFld.setDataType(ExpenseReportFormField.DataType.VARCHAR);
                        dstFrmFld.setControlType(ExpenseReportFormField.ControlType.EDIT);
                    }
                    // Set the attendee type key as the list item key.
                    if (dstFrmFld.getId().equalsIgnoreCase(ExpenseReportFormField.ATTENDEE_TYPE_KEY_FIELD_ID)) {
                        dstFrmFld.setLiKey(expRepAtt.atnTypeKey);
                    }
                    // Set RW access.
                    dstFrmFld.setAccessType(ExpenseReportFormField.AccessType.RW);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".fillFieldsFromColumnDefinitionInfo: attendee dst form fields are null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".fillFieldsFromColumnDefinitionInfo: attendee is null!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.attendee_search);
        initValues(savedInstanceState);

        lastSavedInstanceState = savedInstanceState;
        if (isServiceAvailable()) {
            initUI();
        } else {
            buildViewDelay = true;
        }
    }

    @Override
    protected void onServiceAvailable() {
        super.onServiceAvailable();
        if (buildViewDelay) {
            buildViewDelay = false;
            initUI();
        }
    }

    @SuppressWarnings("unchecked")
    protected void initValues(Bundle savedInstanceState) {

        atnTypeFrmFldViewMap = new HashMap<String, List<FormFieldView>>();

        // Initialize some search parameters.
        if (savedInstanceState != null) {
            String mode = savedInstanceState.getString(ATTENDEE_SEARCH_MODE_KEY);
            if (mode != null) {
                searchMode = SearchMode.valueOf(mode);
            }

            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_KEYS)) {
                String[] excAtnKeyStrs = savedInstanceState
                        .getStringArray(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_KEYS);
                excAtnKeys = new ArrayList<String>(excAtnKeyStrs.length);
                for (int i = 0; i < excAtnKeyStrs.length; ++i) {
                    excAtnKeys.add(excAtnKeyStrs[i]);
                }
            }

            if (savedInstanceState.containsKey(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_EXTERNAL_IDS)) {
                String[] excAtnExtIdsStrs = savedInstanceState
                        .getStringArray(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_EXTERNAL_IDS);
                excAtnExtIds = new ArrayList<String>(excAtnExtIdsStrs.length);
                for (int i = 0; i < excAtnExtIdsStrs.length; ++i) {
                    excAtnExtIds.add(excAtnExtIdsStrs[i]);
                }
            }

        } else {
            searchMode = SearchMode.None;
            // Initialize from the launch intent.
            Intent intent = getIntent();

            // Initialize the list of exclusion attendee keys.
            if (intent.hasExtra(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_KEYS)) {
                String[] excAtnKeyStrs = intent.getStringArrayExtra(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_KEYS);
                excAtnKeys = new ArrayList<String>(excAtnKeyStrs.length);
                for (int i = 0; i < excAtnKeyStrs.length; ++i) {
                    excAtnKeys.add(excAtnKeyStrs[i]);
                }
            }

            // Initialize the list of exclusion attendee external IDs.
            if (intent.hasExtra(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_EXTERNAL_IDS)) {
                String[] excAtnExtIdsStrs = intent
                        .getStringArrayExtra(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_EXTERNAL_IDS);
                excAtnExtIds = new ArrayList<String>(excAtnExtIdsStrs.length);
                for (int i = 0; i < excAtnExtIdsStrs.length; ++i) {
                    excAtnExtIds.add(excAtnExtIdsStrs[i]);
                }
            }
        }

        if (retainer != null) {
            // Recover search mode.
            if (retainer.contains(ATTENDEE_SEARCH_MODE_KEY)) {
                searchMode = (SearchMode) retainer.get(ATTENDEE_SEARCH_MODE_KEY);
            }
            // Recover advanced view mode.
            if (retainer.contains(ATTENDEE_ADVANCED_VIEW_MODE_KEY)) {
                advancedViewMode = (AdvancedViewMode) retainer.get(ATTENDEE_ADVANCED_VIEW_MODE_KEY);
            }
            // Recover favorites search text.
            if (retainer.contains(ATTENDEE_FAVORITES_SEARCH_TEXT_KEY)) {
                searchTextStr = (String) retainer.get(ATTENDEE_FAVORITES_SEARCH_TEXT_KEY);
            }
            // Recover checked attendees map.
            if (retainer.contains(ATTENDEE_ADVANCED_CHECKED_ATTENDEES)) {
                checkedAttendees = (HashSet<ExpenseReportAttendee>) retainer.get(ATTENDEE_ADVANCED_CHECKED_ATTENDEES);
            }
            // Set up a new OnChangeListener
            checkChangeListener = new OnCheckChange();

            // Recover favorites attendee list items.
            if (retainer.contains(ATTENDEE_FAVORITES_LIST_ITEMS_KEY)) {
                List<ExpenseReportAttendee> attendees = (List<ExpenseReportAttendee>) retainer
                        .get(ATTENDEE_FAVORITES_LIST_ITEMS_KEY);
                if (attendees != null) {
                    List<AttendeeListItem> atdListItems = new ArrayList<AttendeeListItem>(attendees.size());
                    for (ExpenseReportAttendee attendee : attendees) {
                        atdListItems.add(new AttendeeListItem(attendee, checkedAttendees, checkChangeListener,
                                AttendeeListItem.FAVORITE_SEARCH_ATTENDEE_LIST_ITEM));
                    }
                    favoritesSearchAdapter = new ListItemAdapter<AttendeeListItem>(this, atdListItems);
                }
            }
            // Store advanced attendee list items.
            if (retainer.contains(ATTENDEE_ADVANCED_LIST_ITEMS_KEY)) {
                List<ExpenseReportAttendee> attendees = (List<ExpenseReportAttendee>) retainer
                        .get(ATTENDEE_ADVANCED_LIST_ITEMS_KEY);
                if (attendees != null) {
                    List<AttendeeListItem> atdListItems = new ArrayList<AttendeeListItem>(attendees.size());
                    for (ExpenseReportAttendee attendee : attendees) {
                        atdListItems.add(new AttendeeListItem(attendee, checkedAttendees, checkChangeListener,
                                AttendeeListItem.ADVANCED_SEARCH_ATTENDEE_LIST_ITEM));
                    }
                    advancedSearchAdapter = new ListItemAdapter<AttendeeListItem>(this, atdListItems);
                }
            }
            // Recover advanced current attendee type spinner selection.
            if (retainer.contains(ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEM)) {
                curAttendeeTypeItem = (SpinnerItem) retainer.get(ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEM);
            }
            // Recover advanced attendee type spinner items.
            if (retainer.contains(ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEMS)) {
                attendeeTypeItems = (SpinnerItem[]) retainer.get(ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEMS);
            }
            // Recover list of external attendees requiring save and current
            // save index.
            if (retainer.contains(ATTENDEE_ADVANCED_EXTERNAL_ATTENDEE_SAVE_LIST_KEY)) {
                advancedSearchSelectedAttendees = (List<ExpenseReportAttendee>) retainer
                        .get(ATTENDEE_ADVANCED_EXTERNAL_ATTENDEE_SAVE_LIST_KEY);
                advancedSearchCurrentAttendeeSaveIndex = (Integer) retainer
                        .get(ATTENDEE_ADVANCED_EXTERNAL_ATTENDEE_SAVE_INDEX_KEY);
            }
            // Recover the attendee type form.
            if (retainer.contains(ATTENDEE_ADVANCED_ATTENDEE_TYPE_FORM_KEY)) {
                attendeeTypeForm = (ExpenseReportAttendee) retainer.get(ATTENDEE_ADVANCED_ATTENDEE_TYPE_FORM_KEY);
            }
        }
        restoreReceivers();
    }

    /**
     * Will flip the view to display the attendee favorites search view.
     */
    private void flipToFavorites() {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.attendee_search_favorites_advanced_flipper);
        vf.setDisplayedChild(0);
    }

    /**
     * Will flip the view to display the attendee advanced search view.
     */
    private void flipToAdvanced() {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.attendee_search_favorites_advanced_flipper);
        vf.setDisplayedChild(1);
    }

    /**
     * Will flip the view to display the attendee advanced search fields view.
     */
    private void flipToAdvancedFields() {
        if (advancedFieldsResultsPager != null) {
            advancedFieldsResultsPager.setCurrentItem(AdvancedFieldsResultsPagerListener.FIELDS_SEARCH_VIEW_POSITION);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".flipToAdvancedFields: advancedFieldsResultsPager is null!");
        }
    }

    /**
     * Will flip the view to display the attendee advanced search results view.
     */
    private void flipToAdvancedResults() {
        if (advancedFieldsResultsPager != null) {
            advancedFieldsResultsPager.setCurrentItem(AdvancedFieldsResultsPagerListener.RESULTS_SEARCH_VIEW_POSITION);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".flipToAdvancedResults: advancedFieldsResultsPager is null!");
        }
    }

    /**
     * Will configure whether the advanced list or advanced message view is displayed based on the advanced search result adapter.
     */
    private void configureAdvancedSearchResultsView() {
        if (advancedSearchAdapter.getCount() == 0) {
            advancedResultsList.setVisibility(View.GONE);
            advancedResultsMessage.setVisibility(View.VISIBLE);
        } else {
            advancedResultsMessage.setVisibility(View.GONE);
            advancedResultsList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Will configure the label and state of the footer button based on the advanced search results being visible.
     */
    private void configureAdvancedSearchResultsFooterButton() {
        String addAttendeeLabel = Format.localizeText(this, R.string.attendee_advanced_selection_prompt,
                checkedAttendees.size());
        searchButton.setText(addAttendeeLabel);
        if (!checkedAttendees.isEmpty()) {
            searchButton.setEnabled(true);
        } else {
            searchButton.setEnabled(false);
        }
    }

    /**
     * Will initialize the list of attendee types from which the end-user can select.
     */
    protected void initAttendeeTypeSelectionItems() {

        List<ListItem> attendeeTypes = getAttendeeTypes();
        List<AttendeeSearchField> attSrchFlds = getAttendeeSearchFields();
        if (attendeeTypes != null && attSrchFlds != null) {
            attendeeTypeItems = new SpinnerItem[attendeeTypes.size()];
            int i = 0;
            for (ListItem listItem : attendeeTypes) {
                attendeeTypeItems[i++] = new SpinnerItem(listItem.key, listItem.text);
            }
        }
    }

    /**
     * Will examine the 'FirstName', 'LastName', 'CompanyName' and 'ExternalId' fields in 'saveAttendee' and if they don't have a
     * value for the fields, then copy it from 'selectedAttendee'.
     * 
     * @param saveAttendee
     *            contains a reference to the attendee whose fields may be filled in by corresponding values from
     *            'selectedAttendee'.
     * @param selectedAttendee
     *            contains a reference to the attendee selected from an advanced search and contributing values to 'saveAttendee'.
     */
    protected void fillFirstLastCompanyExternalIdFields(ExpenseReportAttendee saveAttendee,
            ExpenseReportAttendee selectedAttendee) {
        // FirstName
        ExpenseReportFormField frmFld = FormUtil.findFieldById(saveAttendee.getFormFields(),
                ExpenseReportAttendee.FIRST_NAME_FIELD_ID);
        if (frmFld != null) {
            if (frmFld.getValue() == null || frmFld.getValue().length() == 0) {
                frmFld.setValue(selectedAttendee.firstName);
            }
        }
        // LastName
        frmFld = FormUtil.findFieldById(saveAttendee.getFormFields(), ExpenseReportAttendee.LAST_NAME_FIELD_ID);
        if (frmFld != null) {
            if (frmFld.getValue() == null || frmFld.getValue().length() == 0) {
                frmFld.setValue(selectedAttendee.lastName);
            }
        }
        // Company Name
        frmFld = FormUtil.findFieldById(saveAttendee.getFormFields(), ExpenseReportAttendee.COMPANY_NAME_FIELD_ID);
        if (frmFld != null) {
            if (frmFld.getValue() == null || frmFld.getValue().length() == 0) {
                frmFld.setValue(selectedAttendee.company);
            }
        }
        // ExternalId.
        frmFld = FormUtil.findFieldById(saveAttendee.getFormFields(), ExpenseReportAttendee.EXTERNAL_FIELD_ID);
        if (frmFld != null) {
            if (frmFld.getValue() == null || frmFld.getValue().length() == 0) {
                frmFld.setValue(selectedAttendee.externalId);
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
        // Check whether there a form field view should handle the dialog
        // creation.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            dialog = frmFldViewListener.getCurrentFormFieldView().onCreateDialog(id);
        } else {
            switch (id) {
            case DIALOG_NO_ATTENDEE_SEARCH_RESULTS: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.general_search_results);
                dlgBldr.setMessage(R.string.attendee_search_results_empty_message);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = dlgBldr.create();
                break;
            }
            case DIALOG_SELECT_ATTENDEE_TYPE: {
                if (attendeeTypeItems != null && attendeeTypeItems.length > 0) {

                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setCancelable(true);
                    ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                            android.R.layout.simple_spinner_item, attendeeTypeItems) {

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            return super.getDropDownView(position, convertView, parent);
                        }
                    };

                    listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // Get the currently selected item.
                    int selectedItem = -1;
                    if (curAttendeeTypeItem != null) {
                        for (int i = 0; i < attendeeTypeItems.length; i++) {
                            if (curAttendeeTypeItem.id.equals(attendeeTypeItems[i].id)) {
                                selectedItem = i;
                                break;
                            }
                        }
                    }

                    dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curAttendeeTypeItem = attendeeTypeItems[which];
                            updateAttendeeTypeFieldValue();
                            removeDialog(DIALOG_SELECT_ATTENDEE_TYPE);
                            layoutAttendeeFieldViews();
                            searchButton.setEnabled(true);
                        }
                    });

                    dlgBldr.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            removeDialog(DIALOG_SELECT_ATTENDEE_TYPE);
                        }
                    });
                    dialog = dlgBldr.create();
                }
                break;
            }
            case DIALOG_NO_ATTENDEE_TYPES: {
                break;
            }
            case DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_PROGRESS: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(this.getText(R.string.dlg_retrieve_attendee_search_fields_progress_message));
                progDlg.setIndeterminate(true);
                progDlg.setCancelable(true);
                dialog = progDlg;
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Cancel any outstanding request.
                        if (attendeeSearchFieldRequest != null) {
                            attendeeSearchFieldRequest.cancel();
                        }
                    }
                });
                break;
            }
            case DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_FAILED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.dlg_retrieve_attendee_search_fields_failed_title);
                dlgBldr.setMessage(R.string.dlg_retrieve_attendee_search_fields_failed_message);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = dlgBldr.create();
                break;
            }
            case DIALOG_SEARCH_ATTENDEES_PROGRESS: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(getText(R.string.searching_for_attendees));
                progDlg.setIndeterminate(true);
                progDlg.setCancelable(true);
                dialog = progDlg;
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        switch (searchMode) {
                        case Advanced: {
                            if (advancedAttendeeSearchRequest != null) {
                                advancedAttendeeSearchRequest.cancel();
                            }
                            break;
                        }
                        case Favorites: {
                            if (favoritesAttendeeSearchRequest != null) {
                                favoritesAttendeeSearchRequest.cancel();
                            }
                            break;
                        }
                        case None: {
                            break;
                        }
                        }
                    }
                });
                break;
            }
            case DIALOG_SEARCH_ATTENDEES_FAILED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.dlg_attendee_search_failed_title);
                dlgBldr.setMessage("");
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = dlgBldr.create();
                break;
            }
            case DIALOG_ADD_ATTENDEES_PROGRESS: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(getText(R.string.dlg_add_attendees_progress_message));
                progDlg.setIndeterminate(true);
                progDlg.setCancelable(true);
                dialog = progDlg;
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (attendeeFormRequest != null) {
                            attendeeFormRequest.cancel();
                        } else if (attendeeSaveRequest != null) {
                            attendeeSaveRequest.cancel();
                        }
                    }
                });
                break;
            }
            case DIALOG_ADD_ATTENDEES_FAILED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.dlg_attendee_search_failed_title);
                dlgBldr.setMessage("");
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = dlgBldr.create();
                break;
            }
            default: {
                dialog = super.onCreateDialog(id);
                break;
            }
            }
            if (dialog == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onCreateDialog: ConcurCore.onCreateDialog did not create a dialog for id '" + id + "'.");
            }
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        // Check whether a form field view will handle the dialog preparation.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            frmFldViewListener.getCurrentFormFieldView().onPrepareDialog(id, dialog);
        } else {
            switch (id) {
            case DIALOG_SEARCH_ATTENDEES_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case DIALOG_ADD_ATTENDEES_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            default: {
                super.onPrepareDialog(id, dialog);
                break;
            }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check whether a form field view should handle the activity result.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()) {
            frmFldViewListener.getCurrentFormFieldView().onActivityResult(requestCode, resultCode, data);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        if (excAtnKeys != null) {
            outState.putStringArray(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_KEYS, excAtnKeys.toArray(new String[0]));
        }

        if (excAtnExtIds != null) {
            outState.putStringArray(Const.EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_EXTERNAL_IDS,
                    excAtnExtIds.toArray(new String[0]));
        }

        // Store any form field information.
        FormUtil.storeFormFieldState(frmFldViewListener, outState, true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (retainer != null) {

            // Store search mode.
            if (searchMode != null) {
                retainer.put(ATTENDEE_SEARCH_MODE_KEY, searchMode);
            }
            // Store favorites search text.
            if (searchText != null) {
                Editable searchTextEditable = searchText.getText();
                if (searchTextEditable != null) {
                    retainer.put(ATTENDEE_FAVORITES_SEARCH_TEXT_KEY, searchTextEditable.toString());
                }
            }
            // Store favorites attendee list items.
            if (favoritesSearchAdapter != null) {
                List<AttendeeListItem> atdLstItems = favoritesSearchAdapter.getItems();
                if (atdLstItems != null) {
                    List<ExpenseReportAttendee> attendees = new ArrayList<ExpenseReportAttendee>(atdLstItems.size());
                    for (AttendeeListItem atdListItem : atdLstItems) {
                        attendees.add(atdListItem.getAttendee());
                    }
                    retainer.put(ATTENDEE_FAVORITES_LIST_ITEMS_KEY, attendees);
                }
            }
            // Store advanced view mode.
            if (advancedViewMode != null) {
                retainer.put(ATTENDEE_ADVANCED_VIEW_MODE_KEY, advancedViewMode);
            }
            // Store advanced attendee list items.
            if (advancedSearchAdapter != null) {
                List<AttendeeListItem> atdLstItems = advancedSearchAdapter.getItems();
                if (atdLstItems != null) {
                    List<ExpenseReportAttendee> attendees = new ArrayList<ExpenseReportAttendee>(atdLstItems.size());
                    for (AttendeeListItem atdListItem : atdLstItems) {
                        attendees.add(atdListItem.getAttendee());
                    }
                    retainer.put(ATTENDEE_ADVANCED_LIST_ITEMS_KEY, attendees);
                }
            }
            // Store advanced current attendee type spinner selection.
            if (curAttendeeTypeItem != null) {
                retainer.put(ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEM, curAttendeeTypeItem);
            }
            // Store advanced attendee type spinner items.
            if (attendeeTypeItems != null) {
                retainer.put(ATTENDEE_ADVANCED_ATTENDEE_TYPE_ITEMS, attendeeTypeItems);
            }
            // Store checked attendees map.
            if (checkedAttendees != null) {
                retainer.put(ATTENDEE_ADVANCED_CHECKED_ATTENDEES, checkedAttendees);
            }

            // Store attendee favorites search receiver.
            if (favoritesAttendeeSearchReceiver != null) {
                retainer.put(ATTENDEE_FAVORITES_SEARCH_RECEIVER_KEY, favoritesAttendeeSearchReceiver);
                favoritesAttendeeSearchReceiver.setActivity(null);
            }
            // Store attendee advanced search receiver.
            if (advancedAttendeeSearchReceiver != null) {
                retainer.put(ATTENDEE_ADVANCED_SEARCH_RECEIVER_KEY, advancedAttendeeSearchReceiver);
                advancedAttendeeSearchReceiver.setActivity(null);
            }
            // Store attendee advanced search fields receiver.
            if (attendeeSearchFieldReceiver != null) {
                retainer.put(ATTENDEE_FIELDS_RECEIVER_KEY, attendeeSearchFieldReceiver);
                attendeeSearchFieldReceiver.setActivity(null);
            }
            // Store attendee type receiver.
            if (attendeeTypeReceiver != null) {
                retainer.put(ATTENDEE_TYPE_RECEIVER_KEY, attendeeTypeReceiver);
                attendeeTypeReceiver.setActivity(null);
            }
            // Save 'AttendeeFormReceiver'.
            if (attendeeFormReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                attendeeFormReceiver.setActivity(null);
                // Add to the retainer
                retainer.put(ATTENDEE_FORM_RECEIVER_KEY, attendeeFormReceiver);
            }
            // Save 'AttendeeSaveReceiver'.
            if (attendeeSaveReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                attendeeSaveReceiver.setActivity(null);
                // Add to the retainer
                retainer.put(ATTENDEE_SAVE_RECEIVER_KEY, attendeeSaveReceiver);
            }
            // Store any form field information.
            FormUtil.retainFormFieldState(frmFldViewListener, retainer);
            // Store a list of attendees with external ID's that require saving
            // and the current save index.
            if (advancedSearchSelectedAttendees != null && advancedSearchSelectedAttendees.size() > 0) {
                retainer.put(ATTENDEE_ADVANCED_EXTERNAL_ATTENDEE_SAVE_LIST_KEY, advancedSearchSelectedAttendees);
                retainer.put(ATTENDEE_ADVANCED_EXTERNAL_ATTENDEE_SAVE_INDEX_KEY, advancedSearchCurrentAttendeeSaveIndex);
            }
            // Save attendee type form.
            if (attendeeTypeForm != null) {
                retainer.put(ATTENDEE_ADVANCED_ATTENDEE_TYPE_FORM_KEY, attendeeTypeForm);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: retainer is null!");
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

    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore favorites attendee search receiver.
            if (retainer.contains(ATTENDEE_FAVORITES_SEARCH_RECEIVER_KEY)) {
                favoritesAttendeeSearchReceiver = (FavoritesAttendeeSearchReceiver) retainer
                        .get(ATTENDEE_FAVORITES_SEARCH_RECEIVER_KEY);
                if (favoritesAttendeeSearchReceiver != null) {
                    favoritesAttendeeSearchReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".onCreate: retainer contains a null favorites attendee search receiver!");
                }
            }
            // Restore advanced attendee search receiver.
            if (retainer.contains(ATTENDEE_ADVANCED_SEARCH_RECEIVER_KEY)) {
                advancedAttendeeSearchReceiver = (AdvancedAttendeeSearchReceiver) retainer
                        .get(ATTENDEE_ADVANCED_SEARCH_RECEIVER_KEY);
                if (advancedAttendeeSearchReceiver != null) {
                    advancedAttendeeSearchReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".onCreate: retainer contains a null advanced attendee search receiver!");
                }
            }
            // Restore attendee fields search receiver.
            if (retainer.contains(ATTENDEE_FIELDS_RECEIVER_KEY)) {
                attendeeSearchFieldReceiver = (AttendeeSearchFieldReceiver) retainer.get(ATTENDEE_FIELDS_RECEIVER_KEY);
                if (attendeeSearchFieldReceiver != null) {
                    attendeeSearchFieldReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".onCreate: retainer contains a null attendee search field receiver!");
                }
            }
            // Restore attendee types receiver.
            if (retainer.contains(ATTENDEE_TYPE_RECEIVER_KEY)) {
                attendeeTypeReceiver = (AttendeeTypeReceiver) retainer.get(ATTENDEE_TYPE_RECEIVER_KEY);
                if (attendeeTypeReceiver != null) {
                    attendeeTypeReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer contains a null attendee types receiver!");
                }
            }
            // Restore an attendee form receiver.
            if (retainer.contains(ATTENDEE_FORM_RECEIVER_KEY)) {
                attendeeFormReceiver = (AttendeeFormReceiver) retainer.get(ATTENDEE_FORM_RECEIVER_KEY);
                // Reset the activity reference.
                attendeeFormReceiver.setActivity(this);
            }
            // Restore an attendee save receiver.
            if (retainer.contains(ATTENDEE_SAVE_RECEIVER_KEY)) {
                attendeeSaveReceiver = (AttendeeSaveReceiver) retainer.get(ATTENDEE_SAVE_RECEIVER_KEY);
                // Reset the activity reference.
                attendeeSaveReceiver.setActivity(this);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: retainer is null!");
        }
    }

    protected void initReportReferences() {

        Intent intent = getIntent();
        // Init the expense report cache reference.
        ReportType reportType = ReportUtil.getReportType(intent);
        if (reportType != null) {
            expRepCache = ReportUtil.getReportCache(getConcurCore(), reportType);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initReportReferences: unable to determine report type.");
        }

        if (expRepCache != null) {
            // Obtain a reference to the expense report.
            String reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
            if (reportKey != null) {
                expRep = expRepCache.getReportDetail(reportKey);
                if (expRep == null) {
                    // Fall-back to a non-detailed report.
                    expRep = expRepCache.getReport(reportKey);
                }
                if (expRep != null) {
                    String expRepEntryKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
                    try {
                        if (ReportUtil.isReportEditable(reportType, expRep)) {
                            expRepEntDet = getConcurCore().getCurrentEntryDetailForm();
                            if (expRepEntDet == null) {
                                expRepEntDet = (ExpenseReportEntryDetail) expRepCache.getReportEntry(expRep,
                                        expRepEntryKey);
                            }
                        } else {
                            expRepEntDet = (ExpenseReportEntryDetail) expRepCache
                                    .getReportEntry(expRep, expRepEntryKey);
                        }
                    } catch (ClassCastException ccExc) {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".initReportReferences: non detail expense entry - " + ccExc.getMessage(),
                                ccExc);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initReportReferences: unable to locate expense report!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initReportReferences: launch intent missing expense report key!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initReportReferences: unable to obtain expense report cache!");
        }

        // Construct the form field view listener.
        frmFldViewListener = new AdvancedAttendeeSearchFormFieldViewListener(this, expRep, expRepEntDet);
    }

    protected void initFavoritesSearchUI() {

        modeFavorites = (Button) findViewById(R.id.attendee_search_favorites);
        if (modeFavorites == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initFavoritesSearchUI: unable to locate 'attendee_search_favorites' view!");
        }

        // Grab all our controls
        searchText = (EditText) findViewById(R.id.listSearchEdit);
        // Set the default action for the pin view to do the submit
        searchText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    AttendeeSearch.this.onClick(searchButton);
                    return true;
                }
                return false;
            }
        });
        // Install a touch-listener that determines whether the right compound
        // drawable has been
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
                    AttendeeSearch.this.onClick(searchButton);
                    return true;
                }
                return false;
            }

        });

        if (searchTextStr != null) {
            searchText.setText(searchTextStr);
        }

        // Get the favorites results list ready
        favoritesResultsList = (ListView) findViewById(R.id.attendee_search_favorites_list);
        if (favoritesSearchAdapter == null) {
            favoritesSearchAdapter = new ListItemAdapter<AttendeeListItem>(this, null);
        }
        favoritesResultsList.setAdapter(favoritesSearchAdapter);
        favoritesResultsList.setOnItemClickListener(new FavoritesSearchResultClickListener());

        TextView txtView = (TextView) findViewById(R.id.footer_text_one);
        if (txtView != null) {
            txtView.setText(R.string.attendee_favorite_selection_prompt);
        }
    }

    /**
     * Determines whether or not any currently displayed advanced attendee search fields have a value.
     * 
     * @return returns whether any currently displayed advanced attendee search fields have a value.
     */
    protected boolean attendeeSearchFieldsHaveValues() {
        boolean retVal = false;

        // Iterate over the list of form fields to determine if any non-hidden
        // fields
        // have values. If so, then enabled the search button, if not, then
        // disable it.
        if (curAttendeeTypeItem != null) {
            List<FormFieldView> frmFldViews = atnTypeFrmFldViewMap.get(curAttendeeTypeItem.id);
            if (frmFldViews != null) {
                for (FormFieldView frmFldVw : frmFldViews) {
                    if (frmFldVw.hasValue()) {
                        retVal = true;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Determines whether a current attendee type is selected.
     * 
     * @return whether a current attendee type has been selected.
     */
    protected boolean curAttendeeTypeSelected() {
        return (curAttendeeTypeItem != null);
    }

    protected void initAdvancedSearchUI() {

        // Obtain mode advanced button.
        modeAdvanced = (Button) findViewById(R.id.attendee_search_advanced);
        if (modeAdvanced == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initAdvancedSearchUI: unable to locate 'attendee_search_advanced' view!");
        }

        // Obtain a reference to the pager.
        advancedFieldsResultsPager = (ViewPager) findViewById(R.id.view_pager);

        List<View> pagerViews = new ArrayList<View>(2);

        // Inflate both the advanced search fields.
        LayoutInflater inflater = LayoutInflater.from(this);
        advancedSearchFieldsView = inflater.inflate(R.layout.attendee_search_advanced_fields, null);
        pagerViews.add(advancedSearchFieldsView);

        // Set the attendee type information.
        View atdTypeView = advancedSearchFieldsView.findViewById(R.id.attendee_type);
        if (atdTypeView != null) {
            // Set the field name.
            TextView txtView = (TextView) atdTypeView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.attendee_type);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initAdvancedSearchUI: unable to locate 'field_name' view!");
            }
            // Set the current value or if none, prompt text.
            txtView = (TextView) atdTypeView.findViewById(R.id.field_value);
            if (txtView != null) {
                if (curAttendeeTypeItem != null) {
                    txtView.setText(curAttendeeTypeItem.name);
                    layoutAttendeeFieldViews();
                    if (lastSavedInstanceState != null) {
                        FormUtil.restoreFormFieldState(frmFldViewListener, lastSavedInstanceState, retainer);
                    }
                } else {
                    txtView.setText(R.string.select_attendee_type);
                }
                searchButton.setEnabled(curAttendeeTypeSelected());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initAdvancedSearchUI: unable to locate 'field_value' view!");
            }
            // Display an attendee type selection dialog upon selection.
            atdTypeView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDialog(DIALOG_SELECT_ATTENDEE_TYPE);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initAdvancedSearchUI: unable to locate 'attendee_type' view!");
        }

        // Inflate the advanced results view.
        View resultsView = inflater.inflate(R.layout.attendee_search_advanced_results, null);
        pagerViews.add(resultsView);

        // Set the reference to the advanced search results message.
        advancedResultsMessage = (TextView) resultsView.findViewById(R.id.attendee_search_advanced_message);
        // Get the advanced results list ready
        advancedResultsList = (ListView) resultsView.findViewById(R.id.attendee_search_advanced_list);
        if (advancedSearchAdapter == null) {
            advancedSearchAdapter = new ListItemAdapter<AttendeeListItem>(this, null);
        } else {
            configureAdvancedSearchResultsView();
        }
        advancedResultsList.setAdapter(advancedSearchAdapter);
        advancedResultsList.setOnItemClickListener(new AdvancedSearchResultClickListener());

        // Construct the pager adapter and set it on the pager.
        advancedFieldsResultsPagerAdapter = new AdvancedFieldsResultsPagerAdapter(pagerViews);
        advancedFieldsResultsPager.setAdapter(advancedFieldsResultsPagerAdapter);
        advancedFieldsResultsPagerListener = new AdvancedFieldsResultsPagerListener();
        advancedFieldsResultsPager.setOnPageChangeListener(advancedFieldsResultsPagerListener);

        if (checkedAttendees == null) {
            // Initialize the hash set that contains whether or not an attendee
            // has been checked.
            checkedAttendees = new HashSet<ExpenseReportAttendee>();
        }
        if (checkChangeListener == null) {
            // Initialize the check change listener.
            checkChangeListener = new OnCheckChange();
        }

        // Initialize the advanced view mode.
        if (advancedViewMode == null) {
            advancedViewMode = AdvancedViewMode.Fields;
        }

    }

    protected void initUI() {

        initReportReferences();

        // Set the footer search text.
        searchButton = (Button) findViewById(R.id.footer_button_one);
        if (searchButton != null) {
            searchButton.setText(R.string.general_search);
            searchButton.setEnabled(false);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: can't locate 'footer_button_one' view!");
        }

        initFavoritesSearchUI();

        initAdvancedSearchUI();

        // Check whether or not attendee types are available for the report
        // policy and expense key.
        // If not, then send a request

        if (attendeeTypeItems == null) {
            initAttendeeTypeSelectionItems();
        }

        // Set the title.
        getSupportActionBar().setTitle(R.string.attendee_search);

        // Set us up the default
        switch (searchMode) {
        case None:
        case Favorites: {
            selectModeButton(modeFavorites);
            break;
        }
        case Advanced: {
            selectModeButton(modeAdvanced);
            if (advancedViewMode != null) {
                switch (advancedViewMode) {
                case Fields: {
                    flipToAdvancedFields();
                    break;
                }
                case Results: {
                    flipToAdvancedResults();
                    break;
                }
                }
            }
            break;
        }
        }

    }

    /**
     * Will update the attendee type selection field value based on the currently selected attendee type, or a prompt if a current
     * selection has not been made.
     */
    protected void updateAttendeeTypeFieldValue() {
        View attTypeView = advancedSearchFieldsView.findViewById(R.id.attendee_type);
        if (attTypeView != null) {
            TextView txtView = (TextView) attTypeView.findViewById(R.id.field_value);
            if (txtView != null) {
                if (curAttendeeTypeItem != null) {
                    txtView.setText(curAttendeeTypeItem.name);
                } else {
                    txtView.setText(R.string.select_attendee_type_prompt);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateAttendeeTypeFieldValue: unable to locate 'field_value' view.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateAttendeeTypeFieldValue: unable to locate 'attendee_type' view.");
        }
    }

    /**
     * Will layout the current set of attendee views.
     */
    protected void layoutAttendeeFieldViews() {

        if (curAttendeeTypeItem != null) {
            ViewGroup fldGroup = (ViewGroup) advancedSearchFieldsView
                    .findViewById(R.id.attendee_search_advanced_field_list);
            if (fldGroup != null) {
                // First, punt all the children within the group.
                if (fldGroup.getChildCount() > 0) {
                    fldGroup.removeAllViews();
                }
                // Determine whether we have a cached list of fields.
                List<FormFieldView> frmFldViews = atnTypeFrmFldViewMap.get(curAttendeeTypeItem.id);
                if (frmFldViews == null) {
                    List<ExpenseReportFormField> frmFlds = cloneFormFields(curAttendeeTypeItem.id);
                    frmFldViews = FormUtil
                            .populateViewWithFormFields(this, fldGroup, frmFlds, null, frmFldViewListener);
                    atnTypeFrmFldViewMap.put(curAttendeeTypeItem.id, frmFldViews);
                } else {
                    FormUtil.populateViewWithFormFieldViews(this, fldGroup, frmFldViews);
                }

                // Set the current list of form field view on the listener.
                frmFldViewListener.setFormFieldViews(frmFldViews);
                // Clear any current form field view.
                frmFldViewListener.clearCurrentFormFieldView();
                // Last, ensure the view group containing the fields is visible.
                View view = advancedSearchFieldsView.findViewById(R.id.attendee_search_advanced_field_group);
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutAttendeeFieldViews: unable to locate attendee field group!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".layoutAttendeeFieldViews: unable to locate attendee field list group!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".layoutAttendeeFieldViews: curAttendeeTypeItem is null!");
        }
    }

    private List<ExpenseReportFormField> cloneFormFields(String attendeeTypeKey) {
        List<ExpenseReportFormField> frmFlds = null;

        List<AttendeeSearchField> attSrchFlds = getConcurCore().getExpenseActiveCache().getAttendeeSearchFields();
        if (attSrchFlds != null) {
            for (AttendeeSearchField attSrchFld : attSrchFlds) {
                if (attSrchFld.atnTypeKey != null && attSrchFld.atnTypeKey.equalsIgnoreCase(attendeeTypeKey)) {
                    if (attSrchFld.searchFields != null) {
                        frmFlds = new ArrayList<ExpenseReportFormField>(attSrchFld.searchFields.size());
                        for (ExpenseReportFormField frmFld : attSrchFld.searchFields) {
                            try {
                                frmFlds.add((ExpenseReportFormField) frmFld.clone());
                            } catch (CloneNotSupportedException cnsExc) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".cloneFormFields: unable to clone form field!", cnsExc);
                            }
                        }
                    }
                    break;
                }
            }
        }
        return frmFlds;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Will filter a list of attendees based on a set of exclusion attendee keys and external IDs.
     * 
     * @param attendees
     *            the list of items to filter.
     * @param excAtnKeys
     *            the list of exclusion attendee keys.
     * @param excExtIds
     *            the list of exclusion external IDS.
     * 
     * @return a list of filtered items or <code>null</code> if no items match.
     */
    private List<ExpenseReportAttendee> filterAttendees(List<ExpenseReportAttendee> attendees, List<String> excAtnKeys,
            List<String> excExtIds) {
        List<ExpenseReportAttendee> retVal = attendees;

        if (excAtnKeys != null) {
            if (attendees != null) {
                retVal = new ArrayList<ExpenseReportAttendee>();
                for (ExpenseReportAttendee attendee : attendees) {
                    boolean foundExc = false;
                    for (String excAtnKey : excAtnKeys) {
                        if (attendee.atnKey != null && excAtnKey != null && attendee.atnKey.equalsIgnoreCase(excAtnKey)) {
                            foundExc = true;
                            break;
                        }
                    }
                    if (!foundExc && excExtIds != null) {
                        for (String extId : excExtIds) {
                            if (attendee.externalId != null && attendee.externalId.length() > 0 && extId != null
                                    && extId.length() > 0 && attendee.externalId.equalsIgnoreCase(extId)) {
                                foundExc = true;
                                break;
                            }
                        }
                    }
                    // If we didn't find a match on exclusion atn key or
                    // external ID, then add to the result list.
                    if (!foundExc) {
                        retVal.add(attendee);
                    }
                }
            }
        }
        return retVal;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - start
    // ///////////////////////////////////////////////////////////////////////////

    public static class AttendeeList implements Serializable {

        private static final long serialVersionUID = -7266899228271613407L;

        protected List<ExpenseReportAttendee> attendees;

        protected AttendeeList(List<ExpenseReportAttendee> attendees) {
            this.attendees = attendees;
        }
    }

    /**
     * An extension of <code>ViewPager.SimpleOnPageChangeListener</code> to handle page switches between advanced fields and
     * results views.
     */
    public class AdvancedFieldsResultsPagerListener extends ViewPager.SimpleOnPageChangeListener {

        public static final int FIELDS_SEARCH_VIEW_POSITION = 0;
        public static final int RESULTS_SEARCH_VIEW_POSITION = 1;

        @Override
        public void onPageSelected(int position) {
            switch (position) {
            case FIELDS_SEARCH_VIEW_POSITION: {
                searchButton.setText(R.string.general_search);
                searchButton.setEnabled(curAttendeeTypeSelected());
                advancedViewMode = AdvancedViewMode.Fields;
                Toast toast = Toast.makeText(AttendeeSearch.this, R.string.attendee_advanced_search_swipe_left,
                        Toast.LENGTH_SHORT);
                toast.show();
                break;
            }
            case RESULTS_SEARCH_VIEW_POSITION: {
                advancedViewMode = AdvancedViewMode.Results;
                configureAdvancedSearchResultsFooterButton();
                Toast toast = Toast.makeText(AttendeeSearch.this, R.string.attendee_advanced_search_swipe_right,
                        Toast.LENGTH_SHORT);
                toast.show();
                break;
            }
            }
        }

    }

    /**
     * An extension of <code>PagerAdapter</code> to handle animated, end-user controlled switching between advanved search fields
     * and results.
     */
    class AdvancedFieldsResultsPagerAdapter extends PagerAdapter {

        private final List<View> views;

        /**
         * Constructs an instance of <code>AdvancedFieldsResultsPagerAdapter</code> to be used to contain both search fields and
         * results.
         * 
         * @param views
         *            the list of views.
         */
        AdvancedFieldsResultsPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public void destroyItem(View view, int arg1, Object object) {
            ((ViewPager) view).removeView((View) object);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public Object instantiateItem(View pager, int position) {
            View view = views.get(position);
            ((ViewPager) pager).addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

    }

    /**
     * An extension of <code>FormFieldViewListener</code> for handling changes to advanced attendee search fields.
     */
    class AdvancedAttendeeSearchFormFieldViewListener extends FormFieldViewListener {

        /**
         * Constructs an instance of <code>AdvancedAttendeeSearchFormFieldViewListener</code> to handle changes to advanced
         * attendee search fields.
         * 
         * @param activity
         *            an activity reference.
         * @param expenseReport
         *            references the expense report being edited.
         * @param expenseReportEntry
         *            references the report entry being edited.
         */
        public AdvancedAttendeeSearchFormFieldViewListener(BaseActivity activity, ExpenseReport expenseReport,
                ExpenseReportEntry expenseReportEntry) {
            super(activity, expenseReport, expenseReportEntry);
        }

    }

    /**
     * Will send a request to retrieve attendee type information.
     */
    private void sendAttendeeTypeRequest() {
        ConcurService concurService = getConcurService();
        registerAttendeeTypeReceiver();
        attendeeTypeRequest = concurService.sendAttendeeTypeSearchRequest(getUserId(),
                Const.ATTENDEE_SEARCH_LIST_FT_CODE);
        if (attendeeTypeRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".onReceive: unable to create request to retrieve attendee type information!");
            unregisterAttendeeTypeReceiver();
        } else {
            // Set the request object on the receiver.
            attendeeTypeReceiver.setServiceRequest(attendeeTypeRequest);
            // Show the attendee form progress dialog.
            showDialog(DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_PROGRESS);
        }
    }

    /**
     * Will register an instance of <code>AttendeeTypeReceiver</code> with the application context and set the
     * <code>attendeeTypeReceiver</code> attribute.
     */
    protected void registerAttendeeTypeReceiver() {
        if (attendeeTypeReceiver == null) {
            attendeeTypeReceiver = new AttendeeTypeReceiver(this);
            if (attendeeTypeFilter == null) {
                attendeeTypeFilter = new IntentFilter(Const.ACTION_EXPENSE_ATTENDEE_TYPES_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(attendeeTypeReceiver, attendeeTypeFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAttendeeTypeReceiver: attendeeTypeReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AttendeeTypeReceiver</code> with the application context and set the
     * <code>attendeeTypeReceiver</code> to <code>null</code>.
     */
    protected void unregisterAttendeeTypeReceiver() {
        if (attendeeTypeReceiver != null) {
            getApplicationContext().unregisterReceiver(attendeeTypeReceiver);
            attendeeTypeReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttendeeTypeReceiver: attendeeTypeReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving the list of
     * attendee types.
     */
    static class AttendeeTypeReceiver extends BaseBroadcastReceiver<AttendeeSearch, SearchListRequest> {

        private static final String CLS_TAG = AttendeeSearch.CLS_TAG + "." + AttendeeTypeReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeTypeReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeTypeReceiver(AttendeeSearch activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AttendeeSearch activity) {
            activity.attendeeTypeRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            // If the attendee search fields have already been loaded, then
            // dismiss the progress
            // dialog.
            if (activity.isAttendeeSearchFieldLoaded()) {
                activity.dismissDialog(DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_PROGRESS);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_PROGRESS);
            activity.showDialog(DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore ConcurCore = activity.getConcurCore();
            IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
            if (expRepCache != null) {
                ExpenseType expType = ExpenseType.findExpenseType(activity.expRep.polKey, activity.expRepEntDet.expKey);
                List<ListItem> atnTypeKeys = expRepCache
                        .getAttendeeTypes(expType, expRepCache.getSearchAttendeeTypes());
                if (atnTypeKeys != null) {
                    if (activity.isAttendeeSearchFieldLoaded()) {
                        activity.initAttendeeTypeSelectionItems();
                    } else {
                        activity.sendAttendeeSearchFieldRequest(false);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: attendee type list is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: active report cache is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(SearchListRequest request) {
            activity.attendeeTypeRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeTypeReceiver();
        }

    }

    private void sendFavoritesAttendeeSearchRequest() {
        // Register a receiver to handle the request.
        registerFavoritesAttendeeSearchReceiver();
        favoritesAttendeeSearchRequest = getConcurCore().getService().sendAttendeesSearchRequest(getUserId(),
                currentSearchText, excAtnKeys);
        if (favoritesAttendeeSearchRequest != null) {
            // Set the request on the receiver.
            favoritesAttendeeSearchReceiver.setServiceRequest(favoritesAttendeeSearchRequest);
            // Show the progress dialog.
            showDialog(DIALOG_SEARCH_ATTENDEES_PROGRESS);
        } else {
            // Unregister the receiver.
            unregisterFavoritesAttendeeSearchReceiver();
            // TODO: Present an error dialog.
        }
    }

    /**
     * Will register the attendee search receiver and set the <code>listSearchReceiver</code> member.
     */
    private void registerFavoritesAttendeeSearchReceiver() {
        if (favoritesAttendeeSearchReceiver == null) {
            favoritesAttendeeSearchReceiver = new FavoritesAttendeeSearchReceiver(this);
            getApplicationContext().registerReceiver(favoritesAttendeeSearchReceiver, favoritesAttendeeSearchFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerFavoritesAttendeeSearchReceiver: favoritesAttendeeSearchReceiver is not null!");
        }
    }

    /**
     * Will unregister an attendee search receiver and clear the <code>listSearchReceiver</code> reference.
     */
    private void unregisterFavoritesAttendeeSearchReceiver() {
        if (favoritesAttendeeSearchReceiver != null) {
            getApplicationContext().unregisterReceiver(favoritesAttendeeSearchReceiver);
            favoritesAttendeeSearchReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".unregisterFavoritesAttendeeSearchReceiver: favoritesAttendeeSearchReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> that handles the result of a favorites attendee search.
     * 
     * @author andy
     */
    static class FavoritesAttendeeSearchReceiver extends
            BaseActivity.BaseBroadcastReceiver<AttendeeSearch, AttendeeSearchRequest> {

        /**
         * Constructs an instance of <code>AttendeeSearchReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        FavoritesAttendeeSearchReceiver(AttendeeSearch activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AttendeeSearch activity) {
            activity.favoritesAttendeeSearchRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.removeDialog(DIALOG_SEARCH_ATTENDEES_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_SEARCH_ATTENDEES_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore ConcurCore = activity.getConcurCore();
            AttendeeSearchReply attendeeSearchReply = ConcurCore.getAttendeeSearchResults();
            List<AttendeeListItem> listItems = new ArrayList<AttendeeListItem>();
            List<ExpenseReportAttendee> itemsToView = null;
            if (attendeeSearchReply != null && attendeeSearchReply.results != null) {
                itemsToView = activity.filterAttendees(attendeeSearchReply.results, activity.excAtnKeys,
                        activity.excAtnExtIds);
                for (ExpenseReportAttendee attendee : itemsToView) {
                    listItems.add(new AttendeeListItem(attendee, activity.checkedAttendees,
                            activity.checkChangeListener, AttendeeListItem.FAVORITE_SEARCH_ATTENDEE_LIST_ITEM));
                }
            }
            activity.favoritesSearchAdapter.setItems(listItems);
            activity.favoritesSearchAdapter.notifyDataSetChanged();
            // Hide the soft keyboard.
            if (activity.searchText != null) {
                IBinder windowToken = activity.searchText.getWindowToken();
                if (windowToken != null) {
                    com.concur.mobile.platform.ui.common.util.ViewUtil.hideSoftKeyboard(activity, windowToken);
                }
            }
            if (itemsToView != null && itemsToView.size() > 0) {
                activity.showFavoriteSelectionPrompt();
            } else {
                activity.hideFavoriteSelectionPrompt();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AttendeeSearchRequest request) {
            activity.favoritesAttendeeSearchRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterFavoritesAttendeeSearchReceiver();
        }

    }

    private void sendAdvancedAttendeeSearchRequest(String atnTypeKey, List<ExpenseReportFormField> formFields,
            List<String> excAtnKeys, String expKey, String rptPolKey, String rptEntKey) {
        // Register a receiver to handle the request.
        registerAdvancedAttendeeSearchReceiver();
        advancedAttendeeSearchRequest = getConcurCore().getService().sendExtendedAttendeesSearchRequest(getUserId(),
                atnTypeKey, formFields, excAtnKeys, expKey, rptPolKey, rptEntKey);
        if (advancedAttendeeSearchRequest != null) {
            // Set the request on the receiver.
            advancedAttendeeSearchReceiver.setServiceRequest(advancedAttendeeSearchRequest);
            // Show the progress dialog.
            showDialog(DIALOG_SEARCH_ATTENDEES_PROGRESS);
        } else {
            // Unregister the receiver.
            unregisterAdvancedAttendeeSearchReceiver();
        }
    }

    /**
     * Will register the attendee search receiver and set the <code>listSearchReceiver</code> member.
     */
    private void registerAdvancedAttendeeSearchReceiver() {
        if (advancedAttendeeSearchReceiver == null) {
            advancedAttendeeSearchReceiver = new AdvancedAttendeeSearchReceiver(this);
            getApplicationContext().registerReceiver(advancedAttendeeSearchReceiver, advancedAttendeeSearchFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerAdvancedAttendeeSearchReceiver: advancedAattendeeSearchReceiver is not null!");
        }
    }

    /**
     * Will unregister an attendee search receiver and clear the <code>listSearchReceiver</code> reference.
     */
    private void unregisterAdvancedAttendeeSearchReceiver() {
        if (advancedAttendeeSearchReceiver != null) {
            getApplicationContext().unregisterReceiver(advancedAttendeeSearchReceiver);
            advancedAttendeeSearchReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAdvancedAttendeeSearchReceiver: attendeeSearchReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> that handles the result of a favorites attendee search.
     * 
     * @author andy
     */
    static class AdvancedAttendeeSearchReceiver extends
            BaseActivity.BaseBroadcastReceiver<AttendeeSearch, ExtendedAttendeeSearchRequest> {

        /**
         * Constructs an instance of <code>AdvancedAttendeeSearchReceiver</code> .
         * 
         * @param activity
         *            the activity.
         */
        AdvancedAttendeeSearchReceiver(AttendeeSearch activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AttendeeSearch activity) {
            activity.favoritesAttendeeSearchRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.removeDialog(DIALOG_SEARCH_ATTENDEES_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            // If the status of the response is *not*
            // 'Const.REPLY_STATUS_FAIL_CONNECTOR_UNAUTHORIZED', then use
            // a generic message about attendee search failing at the moment. If
            // it is
            // 'Const.REPLY_STATUS_FAIL_CONNECTOR_UNAUTHORIZED', then use the
            // error message returned from the server
            // is localized.
            String replyStatus = intent.getStringExtra(Const.REPLY_STATUS);
            String errMsg = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            if (errMsg == null || errMsg.length() == 0) {
                errMsg = activity.getText(R.string.dlg_attendee_search_failed_message).toString();
            }
            if (replyStatus != null && !replyStatus.equalsIgnoreCase(Const.REPLY_STATUS_FAIL_CONNECTOR_UNAUTHORIZED)) {
                activity.actionStatusErrorMessage = errMsg;
            }
            activity.showDialog(DIALOG_SEARCH_ATTENDEES_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore ConcurCore = activity.getConcurCore();
            ExtendedAttendeeSearchReply attendeeSearchReply = ConcurCore.getExtendedAttendeeSearchResults();
            List<AttendeeListItem> listItems = new ArrayList<AttendeeListItem>();
            List<ExpenseReportAttendee> itemsToView = null;
            // Clear out the set of checked attendees.
            activity.checkedAttendees.clear();
            if (attendeeSearchReply != null && attendeeSearchReply.results != null) {
                itemsToView = activity.filterAttendees(attendeeSearchReply.results, activity.excAtnKeys,
                        activity.excAtnExtIds);
                for (ExpenseReportAttendee attendee : itemsToView) {
                    listItems.add(new AttendeeListItem(attendee, activity.checkedAttendees,
                            activity.checkChangeListener, AttendeeListItem.ADVANCED_SEARCH_ATTENDEE_LIST_ITEM));
                }
            }
            activity.advancedSearchAdapter.setItems(listItems);
            activity.configureAdvancedSearchResultsView();
            activity.advancedSearchAdapter.notifyDataSetChanged();
            // Hide the soft keyboard.
            if (activity.searchButton != null) {
                IBinder windowToken = activity.searchButton.getWindowToken();
                if (windowToken != null) {
                    com.concur.mobile.platform.ui.common.util.ViewUtil.hideSoftKeyboard(activity, windowToken);
                }
            }
            if (itemsToView == null || itemsToView.size() == 0) {
                activity.showDialog(DIALOG_NO_ATTENDEE_SEARCH_RESULTS);
            } else {
                // Flip the advanced view to show the results.
                activity.flipToAdvancedResults();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ExtendedAttendeeSearchRequest request) {
            activity.advancedAttendeeSearchRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAdvancedAttendeeSearchReceiver();
        }

    }

    private void sendAttendeeSearchFieldRequest(boolean showProgressDialog) {
        // Register a receiver to handle the request.
        registerAttendeeSearchFieldReceiver();
        attendeeSearchFieldRequest = getConcurCore().getService().sendAttendeeSearchFieldsRequest(getUserId());
        if (attendeeSearchFieldRequest != null) {
            // Set the request on the receiver.
            attendeeSearchFieldReceiver.setServiceRequest(attendeeSearchFieldRequest);
            if (showProgressDialog) {
                // Display a progress dialog.
                showDialog(DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_PROGRESS);
            }
        } else {
            // Unregister the receiver.
            unregisterFavoritesAttendeeSearchReceiver();
            // TODO: Present an error dialog.
        }
    }

    /**
     * Will register the attendee search receiver and set the <code>listSearchReceiver</code> member.
     */
    private void registerAttendeeSearchFieldReceiver() {
        if (attendeeSearchFieldReceiver == null) {
            attendeeSearchFieldReceiver = new AttendeeSearchFieldReceiver(this);
            getApplicationContext().registerReceiver(attendeeSearchFieldReceiver, attendeeSearchFieldFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerAttendeeSearchFieldReceiver: attendeeSearchFieldReceiver is not null!");
        }
    }

    /**
     * Will unregister an attendee search receiver and clear the <code>listSearchReceiver</code> reference.
     */
    private void unregisterAttendeeSearchFieldReceiver() {
        if (attendeeSearchFieldReceiver != null) {
            getApplicationContext().unregisterReceiver(attendeeSearchFieldReceiver);
            attendeeSearchFieldReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".unregisterAttendeeSearchFieldReceiver: attendeeSearchFieldReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> that handles the result of retrieving attendee search fields.
     * 
     * @author andy
     */
    static class AttendeeSearchFieldReceiver extends
            BaseActivity.BaseBroadcastReceiver<AttendeeSearch, AttendeeSearchFieldsRequest> {

        /**
         * Constructs an instance of <code>AttendeeSearchFieldReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeSearchFieldReceiver(AttendeeSearch activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AttendeeSearch activity) {
            activity.attendeeSearchFieldRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            // No-op.
            // TODO - shouldn't both search types have a progress dialog when
            // searching! I think so.
            activity.dismissDialog(DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_RETRIEVE_ATTENDEE_SEARCH_FIELDS_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.initAttendeeTypeSelectionItems();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AttendeeSearchFieldsRequest request) {
            activity.attendeeSearchFieldRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeSearchFieldReceiver();
        }

    }

    /**
     * Will send a request to save attendee information.
     */
    private void sendAttendeeSaveRequest(ExpenseReportAttendee attendee) {
        // Do the full attendee save
        ConcurService concurService = getConcurService();
        // Ensure that any base attributes on 'atnForm' are set from their field
        // values.
        // ((AttendeeFormFieldViewListener)
        // frmFldViewListener).setBaseAttendeeValuesFromFields();
        registerAttendeeSaveReceiver();
        attendeeSaveRequest = concurService.sendAttendeeSaveRequest(getUserId(), attendee);
        if (attendeeSaveRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".sendAttendeeSaveRequest: unable to create request to save attendee information!");
            unregisterAttendeeSaveReceiver();
        } else {
            // Display a progress dialog.
            showDialog(DIALOG_ADD_ATTENDEES_PROGRESS);

            // Set the request object on the receiver.
            attendeeSaveReceiver.setServiceRequest(attendeeSaveRequest);
        }
    }

    /**
     * Will register an instance of <code>AttendeeSaveReceiver</code> with the application context and set the
     * <code>attendeeSaveReceiver</code> attribute.
     */
    protected void registerAttendeeSaveReceiver() {
        if (attendeeSaveReceiver == null) {
            attendeeSaveReceiver = new AttendeeSaveReceiver(this);
            if (attendeeSaveFilter == null) {
                attendeeSaveFilter = new IntentFilter(Const.ACTION_EXPENSE_ATTENDEE_SAVE);
            }
            getApplicationContext().registerReceiver(attendeeSaveReceiver, attendeeSaveFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAttendeeSaveReceiver: attendeeSaveReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AttendeeSaveReceiver</code> with the application context and set the
     * <code>attendeeSaveReceiver</code> to <code>null</code>.
     */
    protected void unregisterAttendeeSaveReceiver() {
        if (attendeeSaveReceiver != null) {
            getApplicationContext().unregisterReceiver(attendeeSaveReceiver);
            attendeeSaveReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttendeeSaveReceiver: attendeeSaveReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of saving an attendee.
     */
    static class AttendeeSaveReceiver extends BaseBroadcastReceiver<AttendeeSearch, AttendeeSaveRequest> {

        private static final String CLS_TAG = AttendeeSearch.CLS_TAG + "." + AttendeeSaveReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeSaveReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeSaveReceiver(AttendeeSearch activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AttendeeSearch activity) {
            activity.attendeeSaveRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            // Intentional no-op.
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
            activity.showDialog(DIALOG_ADD_ATTENDEES_FAILED);
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            activity.dismissDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
            return false;
        }

        @Override
        protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
            activity.dismissDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore app = (ConcurCore) activity.getApplication();
            AttendeeSaveReply reply = app.getAttendeeSaveResults();

            ExpenseReportAttendee savedAttendee = reply.attendee;
            // Handle if duplicates found.
            if (reply.duplicateAttendees != null) {
                // Duplicate Attendee Strategy:
                // Use the first duplicate attendee that matches on 'externalID'
                // with the one just
                // saved, *or*, if no matching external ID, then use the first
                // duplicate.
                // Grab current external ID.
                ExpenseReportAttendee curAtd = activity.advancedSearchSelectedAttendees
                        .get(activity.advancedSearchCurrentAttendeeSaveIndex);
                if (curAtd != null) {
                    boolean setSavedAttendee = false;
                    for (ExpenseReportAttendee expRepAtt : reply.duplicateAttendees) {
                        if (expRepAtt.externalId != null && expRepAtt.externalId.length() > 0
                                && curAtd.externalId != null && curAtd.externalId.length() > 0
                                && curAtd.externalId.equalsIgnoreCase(expRepAtt.externalId)) {
                            savedAttendee = expRepAtt;
                            setSavedAttendee = true;
                            break;
                        }
                    }
                    // If no duplicate matching on external ID, then go with the
                    // first duplicate.
                    if (!setSavedAttendee) {
                        if (reply.duplicateAttendees.size() > 0) {
                            savedAttendee = reply.duplicateAttendees.get(0);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: current save attendee is null!");
                }
            }
            // Ensure that 'atnTypeCode' and 'atnTypeName' from the attendee
            // returned in the advanced search is copied
            // into the attendee returned from the save call.
            ExpenseReportAttendee advSrchAtd = activity.advancedSearchSelectedAttendees
                    .get(activity.advancedSearchCurrentAttendeeSaveIndex);
            if (advSrchAtd != null) {
                // Copy over 'atnTypeCode', 'atnTypeName' and 'atnTypeKey'
                savedAttendee.atnTypeCode = advSrchAtd.atnTypeCode;
                savedAttendee.atnTypeName = advSrchAtd.atnTypeName;
                savedAttendee.atnTypeKey = advSrchAtd.atnTypeKey;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: selected advanced search result attendee is null!");
            }
            // Ensure that if 'currentVersionNumber' isn't set in
            // 'savedAttendee', that it gets set to the value of 'versionNumber'
            // in 'savedAttendee'.
            if (savedAttendee.currentVersionNumber == null || savedAttendee.currentVersionNumber.length() == 0) {
                savedAttendee.currentVersionNumber = savedAttendee.versionNumber;
            }
            // Replace the advanced search selected attendee with the attendee
            // returned in the save response.
            activity.advancedSearchSelectedAttendees
                    .set(activity.advancedSearchCurrentAttendeeSaveIndex, savedAttendee);
            // Find the next index into 'advancedSearchSelectedAttendees' that
            // should be saved.
            int nextAtdSaveIndex = -1;
            for (int atdInd = activity.advancedSearchCurrentAttendeeSaveIndex + 1; atdInd < activity.advancedSearchSelectedAttendees
                    .size(); ++atdInd) {
                ExpenseReportAttendee selAtd = activity.advancedSearchSelectedAttendees.get(atdInd);
                // If the attendee already has a key, then it's already been
                // saved within the system and
                // the client doesn't need to call 'SaveAttendee' to obtain a
                // key.
                if (selAtd.atnKey == null || selAtd.atnKey.length() == 0) {
                    if (selAtd.externalId != null && selAtd.externalId.length() > 0) {
                        nextAtdSaveIndex = atdInd;
                        break;
                    }
                }
            }
            // If we found another attendee to be saved, then set the global
            // index, clone and fill the attendee and call
            // save.
            if (nextAtdSaveIndex != -1) {
                // Set the global save index.
                activity.advancedSearchCurrentAttendeeSaveIndex = nextAtdSaveIndex;
                // Obtain next external attendee.
                ExpenseReportAttendee selAtd = activity.advancedSearchSelectedAttendees.get(nextAtdSaveIndex);
                // Populate attendee fields from column definitions.
                ConcurCore ConcurCore = activity.getConcurCore();
                ExtendedAttendeeSearchReply attendeeSearchReply = ConcurCore.getExtendedAttendeeSearchResults();
                List<ExpenseReportFormField> colDefs = null;
                if (attendeeSearchReply != null) {
                    colDefs = attendeeSearchReply.columnDefinitions;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: attendee search reply is null!");
                }
                activity.fillFieldsFromColumnDefinitionInfo(selAtd, colDefs);
                // Send a save request.
                activity.sendAttendeeSaveRequest(selAtd);
            } else {
                activity.dismissDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
                // No attendees with external ID's, so just return the selected
                // list.
                ConcurCore concurCore = activity.getConcurCore();
                concurCore.setSelectedAttendees(activity.advancedSearchSelectedAttendees);
                Intent data = new Intent();
                data.putExtra(Flurry.PARAM_NAME_VIA, Flurry.PARAM_VALUE_ADVANCED_SEARCH);
                activity.setResult(RESULT_OK, data);
                activity.finish();
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AttendeeSaveRequest request) {
            activity.attendeeSaveRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeSaveReceiver();
        }

    }

    /**
     * Will send a request to retrieve attendee type information.
     */
    private void sendAttendeeFormRequest(String atnTypeKey, String atnKey) {
        ConcurService concurService = getConcurService();
        registerAttendeeFormReceiver();
        attendeeFormRequest = concurService.sendAttendeeFormRequest(getUserId(), atnTypeKey, atnKey);
        if (attendeeFormRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".sendAttendeeFormRequest: unable to create request to retrieve attendee form information!");
            unregisterAttendeeFormReceiver();
        } else {
            // Set the request object on the receiver.
            attendeeFormReceiver.setServiceRequest(attendeeFormRequest);
            // Show the add attendee progress dailog.
            showDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
        }
    }

    /**
     * Will register an instance of <code>AttendeeFormReceiver</code> with the application context and set the
     * <code>attendeeFormReceiver</code> attribute.
     */
    protected void registerAttendeeFormReceiver() {
        if (attendeeFormReceiver == null) {
            attendeeFormReceiver = new AttendeeFormReceiver(this);
            if (attendeeFormFilter == null) {
                attendeeFormFilter = new IntentFilter(Const.ACTION_EXPENSE_ATTENDEE_FORM_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(attendeeFormReceiver, attendeeFormFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAttendeeFormReceiver: attendeeFormReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AttendeeFormReceiver</code> with the application context and set the
     * <code>attendeeFormReceiver</code> to <code>null</code>.
     */
    protected void unregisterAttendeeFormReceiver() {
        if (attendeeFormReceiver != null) {
            getApplicationContext().unregisterReceiver(attendeeFormReceiver);
            attendeeFormReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttendeeFormReceiver: attendeeFormReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving a form for
     * attendee editing.
     */
    static class AttendeeFormReceiver extends BaseBroadcastReceiver<AttendeeSearch, AttendeeFormRequest> {

        private static final String CLS_TAG = AttendeeSearch.CLS_TAG + "." + AttendeeFormReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AttendeeFormReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AttendeeFormReceiver(AttendeeSearch activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AttendeeSearch activity) {
            activity.attendeeFormRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            // Intention no-op.
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
            activity.showDialog(DIALOG_ADD_ATTENDEES_FAILED);
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            activity.dismissDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
            return false;
        }

        @Override
        protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
            activity.dismissDialog(DIALOG_ADD_ATTENDEES_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Grab the attendee form from the application object and place it
            // into our map.
            ConcurCore ConcurCore = activity.getConcurCore();
            ExpenseReportAttendee attendeeForm = ConcurCore.getAttendeeForm();
            if (attendeeForm != null) {
                if (intent.hasExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY)) {
                    String atnTypeKey = intent.getStringExtra(Const.EXTRA_EXPENSE_ATTENDEE_TYPE_KEY);
                    if (atnTypeKey != null) {
                        atnTypeKey = atnTypeKey.trim();
                        if (atnTypeKey.length() > 0) {
                            // Save a reference to the master attendee type
                            // form.
                            activity.attendeeTypeForm = attendeeForm;
                            // Create a clone 'activity.attendeeTypeForm' and
                            // fill in values from 'selectedAttendee'.
                            ExpenseReportAttendee selectedAttendee = activity.advancedSearchSelectedAttendees
                                    .get(activity.advancedSearchCurrentAttendeeSaveIndex);
                            ExpenseReportAttendee saveAttendee = FormUtil.cloneAndFill(activity.attendeeTypeForm,
                                    selectedAttendee);
                            // Ensure that 'saveAttendee' has specific fields
                            // filled out from 'selectedAttendee'.
                            activity.fillFirstLastCompanyExternalIdFields(saveAttendee, selectedAttendee);
                            // Send the save request.
                            activity.sendAttendeeSaveRequest(saveAttendee);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: attendee type key is empty!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent has null attendee type key!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent is missing attendee type key!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: downloaded attendee form is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AttendeeFormRequest request) {
            activity.attendeeFormRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttendeeFormReceiver();
        }

    }

    /**
     * An implementation of <code>CompountButton.onCheckedChangeListener</code> to control adding/removing instances of
     * <code>ExpenseReportAttendee</code> from a set of attendees representing checked attendees.
     */
    class OnCheckChange implements CompoundButton.OnCheckedChangeListener {

        private final String CLS_TAG = AttendeeSearch.CLS_TAG + "." + OnCheckChange.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged (android.widget.CompoundButton, boolean)
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getTag() instanceof ExpenseReportAttendee) {
                ExpenseReportAttendee attendee = (ExpenseReportAttendee) buttonView.getTag();
                if (isChecked) {
                    if (!checkedAttendees.add(attendee)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCheckedChange: attendee already in checked set!");
                    }
                } else {
                    if (!checkedAttendees.remove(attendee)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCheckedChanged: attendee not in checked set!");
                    }
                }
                configureAdvancedSearchResultsFooterButton();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCheckChanged: tag is not an attendee object!");
            }
        }
    }

    /**
     * An implementation of <code>AdapterView.OnItemClickListener</code> for the purpose of handling clicking on an attendee
     * returned in a favorites search.
     */
    class FavoritesSearchResultClickListener implements AdapterView.OnItemClickListener {

        private final String CLS_TAG = AttendeeSearch.CLS_TAG
                + FavoritesSearchResultClickListener.class.getSimpleName();

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0) {
                AttendeeListItem atdLstItem = favoritesSearchAdapter.getItem(position);
                ExpenseReportAttendee selectedAttendee = atdLstItem.getAttendee();
                if (selectedAttendee != null) {
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    List<ExpenseReportAttendee> selectedAttendees = new ArrayList<ExpenseReportAttendee>(1);
                    selectedAttendees.add(selectedAttendee);
                    ConcurCore.setSelectedAttendees(selectedAttendees);
                    Intent data = new Intent();
                    data.putExtra(Flurry.PARAM_NAME_VIA, Flurry.PARAM_VALUE_QUICK_SEARCH);
                    setResult(RESULT_OK, new Intent());
                    finish();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: selectedItem is null!");
                }
            }
        }
    }

    /**
     * An implementation of <code>AdapterView.OnItemClickListener</code> for the purpose of handling clicking on an attendee
     * returned in an advanced search.
     */
    class AdvancedSearchResultClickListener implements AdapterView.OnItemClickListener {

        private final String CLS_TAG = AttendeeSearch.CLS_TAG + AdvancedSearchResultClickListener.class.getSimpleName();

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0) {
                AttendeeListItem atdLstItem = advancedSearchAdapter.getItem(position);
                ExpenseReportAttendee selectedAttendee = atdLstItem.getAttendee();
                if (selectedAttendee != null) {
                    List<ExpenseReportAttendee> selectedAttendees = new ArrayList<ExpenseReportAttendee>(1);
                    selectedAttendees.add(selectedAttendee);
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    ConcurCore.setSelectedAttendees(selectedAttendees);
                    Intent intent = new Intent(AttendeeSearch.this, ExpenseAttendeePreview.class);
                    startActivityForResult(intent, ATTENDEE_ADVANCE_PREVIEW_REQ_CODE); // TODO: determine what int to pass
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: selectedItem is null!");
                }
            }
        }
    }

    class SearchResultsAdapter extends BaseAdapter {

        private final Context context;
        private List<ExpenseReportAttendee> attendeeItems;
        private final List<ExpenseReportAttendee> emptyAttendeeItems = new ArrayList<ExpenseReportAttendee>(1);

        public SearchResultsAdapter(Context context) {
            this.context = context;
            attendeeItems = emptyAttendeeItems;
        }

        public void updateListItems(List<ExpenseReportAttendee> attendeeItems) {
            if (attendeeItems == null) {
                this.attendeeItems = emptyAttendeeItems;
            } else {
                this.attendeeItems = attendeeItems;
            }
            notifyDataSetChanged();
        }

        public void clearListItems() {
            attendeeItems = emptyAttendeeItems;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return attendeeItems.size();
        }

        @Override
        public ExpenseReportAttendee getItem(int position) {
            return attendeeItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                row = inflater.inflate(R.layout.attendee_favorite_search_result_row, null);
            }

            ExpenseReportAttendee attendeeItem = getItem(position);
            TextView tv = (TextView) row.findViewById(R.id.attendee_name);
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(attendeeItem.lastName);
            if (attendeeItem.firstName != null && attendeeItem.firstName.length() > 0) {
                strBldr.append(", ");
                strBldr.append(attendeeItem.firstName);
            }
            tv.setText(strBldr.toString());

            // if ((position % 2) == 0) {
            // tv.setBackgroundColor(colorBlueStripe);
            // } else {
            // tv.setBackgroundColor(colorWhiteStripe);
            // }

            return row;
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - end
    // ///////////////////////////////////////////////////////////////////////////

}

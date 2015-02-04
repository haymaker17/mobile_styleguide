package com.concur.mobile.core.request.activity;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.apptentive.android.sdk.Log;
import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.AbstractConnectFormFieldActivity;
import com.concur.mobile.core.request.RequestPagerAdapter;
import com.concur.mobile.core.request.service.RequestParser;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.FormDTO;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.dto.RequestSegmentDTO;
import com.concur.mobile.platform.request.groupConfiguration.Policy;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.groupConfiguration.SegmentType;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.util.Parse;

import java.util.*;

/**
 * Created by OlivierB on 28/01/2015.
 */
public class RequestEntryActivity extends AbstractConnectFormFieldActivity implements View.OnClickListener {

    private static final String CLS_TAG = RequestEntryActivity.class.getSimpleName();
    public static final String ENTRY_ID = "entryId";

    private static final int ID_LOADING_VIEW = 0;
    private static final int ID_ENTRY_VIEW = 1;

    private static final int ID_NO_VIEWPAGER_VIEW = 0;
    private static final int ID_VIEWPAGER_VIEW = 1;

    private static final String FIELD_SEGMENT_TYPE = "SegmentType";

    private static final String FIELD_FROM_ID = "FromLocationID";
    private static final String FIELD_TO_ID = "ToLocationID";
    private static final String FIELD_START_DATE = "DepartureDate";
    private static final String FIELD_START_TIME = "DepartureTime";
    private static final String FIELD_END_DATE = "ArrivalDate";
    private static final String FIELD_END_TIME = "ArrivalTime";
    private static final String FIELD_COMMENT = "Comment";
    private static final String FIELD_AMOUNT = "TransactionAmount";
    private static final String FIELD_CURRENCY = "CrnKey";
    // --- MISC
    // --- CARRT / TAXIF / LIMOF
    // --- PARKG / HOTEL
    // --- AIRFR / RAILF
    // --- DININ / EVENT : nothing specific.

    public static final int TAB_ONE_WAY = 0;
    public static final int TAB_ROUND_TRIP = 1;
    public static final int TAB_MULTI_LEG = 2;

    protected ConnectForm form;
    protected Locale locale;

    private RequestListCache requestListCache = null;
    private ConnectFormFieldsCache formFieldsCache = null;
    private BaseAsyncResultReceiver asyncReceiverSave;

    private RequestDTO request;
    private RequestEntryDTO entry;
    private List<RequestSegmentDTO> segmentOneWay;
    private List<RequestSegmentDTO> segmentsRoundTrip;
    private List<RequestSegmentDTO> segmentsMultiLeg;

    private static Map<SegmentType.RequestSegmentType, List<String>> layoutVisibilities = new HashMap<SegmentType.RequestSegmentType, List<String>>();

    private ViewFlipper entryVF;
    private ViewFlipper entryTypeVF;
    private TextView requestTitle;
    private ViewPager viewPager;

    private int viewedFragment = -1;
    private int originFragment = -1;
    private int fragmentOnInitialization = -1;
    private boolean createMode = false;
    //private boolean isAir = false;
    private SegmentType.RequestSegmentType viewedType = null;

    static {
        final List<String> hotelLayout = new ArrayList<String>();  // HOTEL
        hotelLayout.add(FIELD_TO_ID);
        hotelLayout.add(FIELD_START_DATE);
        hotelLayout.add(FIELD_START_TIME);
        hotelLayout.add(FIELD_END_DATE);
        hotelLayout.add(FIELD_END_TIME);
        hotelLayout.add(FIELD_CURRENCY);
        hotelLayout.add(FIELD_AMOUNT);
        hotelLayout.add(FIELD_COMMENT);
        layoutVisibilities.put(SegmentType.RequestSegmentType.HOTEL, hotelLayout);
        final List<String> airLayout = new ArrayList<String>();
        airLayout.add(FIELD_FROM_ID);
        airLayout.addAll(hotelLayout);
        layoutVisibilities.put(SegmentType.RequestSegmentType.AIR, airLayout);
        layoutVisibilities.put(SegmentType.RequestSegmentType.CAR, airLayout);
    }

    /**
     * Bundle need to contain:
     * - RequestListActivity.REQUEST_ID
     * And for consultation / update:
     * - RequestEntryActivity.ENTRY_ID
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_entry);

        final ConcurCore concurCore = (ConcurCore) getApplication();

        requestListCache = (RequestListCache) concurCore.getRequestListCache();
        formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();
        //groupConfigurationCache = (RequestGroupConfigurationCache) concurCore.getRequestGroupConfigurationCache();

        final Bundle bundle = getIntent().getExtras();
        final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);
        final String entryId = bundle.getString(RequestEntryActivity.ENTRY_ID);

        /*this.isEditable = bundle.getString(RequestSummaryActivity.REQUEST_IS_EDITABLE).equals(Boolean.TRUE.toString()) ?
                true :
                false;*/

        locale = this.getResources().getConfiguration().locale != null ?
                this.getResources().getConfiguration().locale :
                Locale.US;

        // --- update mode
        if (requestId != null && entryId != null) {
            request = requestListCache.getValue(requestId);
            entry = request.getEntriesMap().get(entryId);
            form = formFieldsCache.getFormFields(entry.getSegmentFormId());
        }
        // --- create mode
        else if (requestId != null) {
            createMode = true;
            request = requestListCache.getValue(requestId);
            form = formFieldsCache.getFormFields(entry.getSegmentFormId());
            entry = new RequestEntryDTO();
            // --- entry initialization
            entry.setListSegment(new ArrayList<RequestSegmentDTO>());
            // --- TODO
        } else {
            // TODO this might be used later if we can create directly from a segment type selection screen
            // --- This is wrong, go back to previous screen and log error
            Log.e(CLS_TAG, "requestId is null !");
            finish();
        }

        configureUI();
    }

    private void configureUI() {
        entryVF = (ViewFlipper) findViewById(R.id.entryVF);
        entryTypeVF = (ViewFlipper) findViewById(R.id.entryTypeVF);
        entryVF.setDisplayedChild(ID_ENTRY_VIEW);
        viewedType = SegmentType.RequestSegmentType.getByCode(entry.getSegmentTypeCode());
        if (viewedType == null) {
            viewedType = SegmentType.RequestSegmentType.CAR;
        }
        // --- Air segments have a specific display
        if (viewedType == SegmentType.RequestSegmentType.AIR) {
            entryTypeVF.setDisplayedChild(ID_VIEWPAGER_VIEW);
        } else {
            entryTypeVF.setDisplayedChild(ID_NO_VIEWPAGER_VIEW);
        }
        final LinearLayout entryFields = (LinearLayout) findViewById(R.id.entryFields);
        requestTitle = (TextView) findViewById(R.id.entryTitle);
        requestTitle.setText(entry.getSegmentType());
        // --- we use fragments if it's air, which will launch display by themselves
        if (viewedType != SegmentType.RequestSegmentType.AIR) {
            // --- only one segment possible
            this.setDisplayFields(entry.getListSegment().iterator().next(), form, entryFields);
            applySaveButtonPolicy(findViewById(R.id.saveButton));
        } else {
            final Map<Integer, String> tabTitles = new HashMap<Integer, String>();
            tabTitles.put(TAB_ONE_WAY, getResources().getString(R.string.air_search_btn_oneway));
            tabTitles.put(TAB_ROUND_TRIP, getResources().getString(R.string.air_search_btn_roundtrip));
            final RequestPagerAdapter<RequestEntryActivity> adapter = new RequestPagerAdapter<RequestEntryActivity>(
                    this, getSupportFragmentManager(), tabTitles);
            viewPager = (ViewPager) findViewById(R.id.viewPagerLayout);
            viewPager.setAdapter(adapter);
            viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

                @Override
                public void onPageSelected(int position) {
                    viewedFragment = position;
                    applyAirLayoutChange(position);
                }
            });
            // --- set the current tab & apply the temporary in use leg list
            if (createMode || entry.getListSegment().size() == 1) {
                viewedFragment = originFragment = TAB_ONE_WAY;
                segmentOneWay = entry.getListSegment();
                viewPager.setCurrentItem(TAB_ONE_WAY);
            } else if (entry.getListSegment().size() == 2) {
                viewedFragment = originFragment = TAB_ROUND_TRIP;
                segmentsRoundTrip = entry.getListSegment();
                viewPager.setCurrentItem(TAB_ROUND_TRIP);
            } else if (entry.getListSegment().size() > 2) {
                viewedFragment = originFragment = TAB_MULTI_LEG;
                segmentsMultiLeg = entry.getListSegment();
                //TODO multi-leg
            }
        }
    }

    private void applyAirLayoutChange(int position) {
        //TODO :: apply values tansformation
        Toast.makeText(RequestEntryActivity.this, "Selected page position: " + position, Toast.LENGTH_SHORT).show();
    }

    //FIXME appliquer une methode genre applyCustomSortOnFormFields() pour gerer leur changement de décision a la con

    /**
     * Called by fragments, manage the rendering depending on the tab selected
     *
     * @param fieldLayout
     * @param idFragment
     */
    @Override
    public void setDisplayFields(final LinearLayout fieldLayout, int idFragment) {
        fragmentOnInitialization = idFragment;
        final ConcurCore core = getConcurCore();
        boolean isOrigin = idFragment == originFragment;
        final List<RequestSegmentDTO> entryListSegment = entry.getListSegment();
        final List<RequestSegmentDTO> inUseListSegment;

        if (idFragment == TAB_ONE_WAY) {
            // --- initialize / set the segment list in use
            if (isOrigin) {
                segmentOneWay = entryListSegment;
            } else {
                if (segmentOneWay != null) {
                    segmentOneWay.clear();
                } else {
                    segmentOneWay = new ArrayList<RequestSegmentDTO>();
                }
            }
            inUseListSegment = segmentOneWay;
            // --- one segment only
            final RequestSegmentDTO oneWaySegment;
            if (createMode) {
                oneWaySegment = new RequestSegmentDTO();
                inUseListSegment.add(oneWaySegment);
                // --- TODO : set default form id
            } else if (!isOrigin) {
                // --- we only keep the first segment
                oneWaySegment = entryListSegment.iterator().next();
                inUseListSegment.add(oneWaySegment);
            } else {
                oneWaySegment = inUseListSegment.iterator().next();
            }
            setDisplayFields(oneWaySegment, core.getRequestFormFieldsCache().getValue(oneWaySegment.getSegmentFormId()),
                    fieldLayout, null, null);
        } else if (idFragment == TAB_ROUND_TRIP) {
            // --- initialize / set the segment list in use
            if (isOrigin) {
                segmentsRoundTrip = entryListSegment;
            } else {
                if (segmentsRoundTrip != null) {
                    segmentsRoundTrip.clear();
                } else {
                    segmentsRoundTrip = new ArrayList<RequestSegmentDTO>();
                }
            }
            inUseListSegment = segmentsRoundTrip;
            if (createMode) {
                final String segmentDefaultFormId = extractSegmentDefaultFormId(SegmentType.RequestSegmentType.AIR);

                final RequestSegmentDTO segmentD = new RequestSegmentDTO();
                segmentD.setSegmentFormId(segmentDefaultFormId);
                inUseListSegment.add(segmentD);

                final RequestSegmentDTO segmentA = new RequestSegmentDTO();
                segmentA.setSegmentFormId(segmentDefaultFormId);
                inUseListSegment.add(segmentA);
            } else if (!isOrigin) {
                if (entryListSegment.size() == 1) {
                    // --- adds the first segment of the current existing entry (one way)
                    final RequestSegmentDTO segmentD = entryListSegment.iterator().next();
                    inUseListSegment.add(segmentD);
                    // --- conversion operation from one way to round trip
                    final String segmentDefaultFormId = extractSegmentDefaultFormId(SegmentType.RequestSegmentType.AIR);

                    final RequestSegmentDTO segmentA = new RequestSegmentDTO();
                    segmentA.setSegmentFormId(segmentDefaultFormId);
                    segmentA.setSegmentType(segmentD.getSegmentType());
                    segmentA.setFromLocationName(segmentD.getToLocationName());
                    segmentA.setToLocationName(segmentD.getFromLocationName());
                    if (segmentD.getArrivalDate() != null) {
                        final Calendar cal = new GregorianCalendar();
                        cal.setTime(segmentD.getArrivalDate());
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                        segmentA.setDepartureDate(cal.getTime());
                    }

                    // --- Adds Arrival segment to round trip
                    inUseListSegment.add(segmentA);
                } else if (entryListSegment.size() > 2) {
                    // --- TODO conversion operation from multi-leg to round trip
                }
            }

            int displayOrder = 0;
            for (RequestSegmentDTO segment : inUseListSegment) {
                segment.setDisplayOrder(displayOrder);
                setDisplayFields(segment, core.getRequestFormFieldsCache().getValue(segment.getSegmentFormId()),
                        fieldLayout, null, null);
                displayOrder++;
            }
            //setDisplayFields(model, form, fieldLayout, null, null);
            // --- TODO : handle specific rendering
        } else if (idFragment == TAB_MULTI_LEG) {
            // --- initialize / set the segment list in use
            if (isOrigin) {
                segmentsMultiLeg = entryListSegment;
            } else {
                if (segmentsMultiLeg != null) {
                    segmentsMultiLeg.clear();
                } else {
                    segmentsMultiLeg = new ArrayList<RequestSegmentDTO>();
                }
            }
            inUseListSegment = segmentsMultiLeg;
            //TODO
        }
    }

    private String extractSegmentDefaultFormId(SegmentType.RequestSegmentType segmentType) {
        final RequestGroupConfiguration rgc = getConcurCore().getRequestGroupConfigurationCache().getValue(getUserId());
        for (Policy p : rgc.getPolicies()) {
            if (p.getIsDefault()) {
                for (SegmentType st : p.getSegmentTypes()) {
                    if (st.getIconCode().equals(segmentType.getCode())) {
                        return st.getSegmentFormID();
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected String getModelValueByFieldName(FormDTO requestSegment, String fieldName) {
        final RequestSegmentDTO segment = (RequestSegmentDTO) requestSegment;
        //TODO
        if (fieldName.equals(FIELD_FROM_ID)) {
            if (segment.getFromLocationName() == null) {
                return "";
            }
            return segment.getFromLocationName();
        } else if (fieldName.equals(FIELD_TO_ID)) {
            if (segment.getToLocationName() == null) {
                return "";
            }
            return segment.getToLocationName();
        } else if (fieldName.equals(FIELD_AMOUNT)) {
            if (segment.getForeignAmount() == null) {
                return "";
            }
            return segment.getForeignAmount().toString();
        } else if (fieldName.equals(FIELD_START_DATE)) {
            if (segment.getDepartureDate() == null) {
                return "";
            }
            return formatDate(segment.getDepartureDate());
        } else if (fieldName.equals(FIELD_START_TIME)) {
            if (segment.getDepartureDate() == null) {
                return "";
            }
            return formatTime(segment.getDepartureDate());
        } else if (fieldName.equals(FIELD_END_DATE)) {
            if (segment.getArrivalDate() == null) {
                return "";
            }
            return formatDate(segment.getArrivalDate());
        } else if (fieldName.equals(FIELD_END_TIME)) {
            if (segment.getArrivalDate() == null) {
                return "";
            }
            return formatTime(segment.getArrivalDate());
        } else if (fieldName.equals(FIELD_CURRENCY)) {
            if (segment.getForeignCurrencyCode() == null) {
                return "";
            }
            //TODO : find a solution to handle v < 19
            return Currency.getInstance(segment.getForeignCurrencyCode()).getDisplayName();
        } else if (fieldName.equals(FIELD_COMMENT)) {
            if (segment.getForeignCurrencyCode() == null) {
                return "";
            }
            return segment.getLastComment();
        }
        return null;
    }

    @Override
    protected void setModelValueByFieldName(FormDTO model, String fieldName, String value) {
        final RequestSegmentDTO segment = (RequestSegmentDTO) model;
        if (fieldName.equals(FIELD_FROM_ID)) {
            // --- TODO : get location name with that ID
            segment.setFromLocationName(value);
        } else if (fieldName.equals(FIELD_TO_ID)) {
            // --- TODO : get location name with that ID
            segment.setToLocationName(value);
        } else if (fieldName.equals(FIELD_AMOUNT)) {
            segment.setForeignAmount(value != null ? Parse.safeParseDouble(value) : 0d);
        } else if (fieldName.equals(FIELD_START_DATE)) {
            segment.setDepartureDate(parseDate(value));
        } else if (fieldName.equals(FIELD_START_TIME)) {
            applyTimeString(segment.getDepartureDate(), value);
        } else if (fieldName.equals(FIELD_END_DATE)) {
            segment.setArrivalDate(parseDate(value));
        } else if (fieldName.equals(FIELD_END_TIME)) {
            applyTimeString(segment.getArrivalDate(), value);
        } else if (fieldName.equals(FIELD_CURRENCY)) {
            //TODO segment.setForeignCurrencyCode(Currency.);
        } else if (fieldName.equals(FIELD_COMMENT)) {
            segment.setLastComment(value);
        }
    }

    @Override
    protected String getLabelFromFieldName(String fieldName) {
        //TODO
        if (fieldName.equals(FIELD_FROM_ID)) {
            return "From";
        } else if (fieldName.equals(FIELD_TO_ID)) {
            return "To";
        } else if (fieldName.equals(FIELD_AMOUNT)) {
            return "Amount";
        } else if (fieldName.equals(FIELD_START_DATE)) {
            return "Departure";
        } else if (fieldName.equals(FIELD_START_TIME)) {
            return "at";
        } else if (fieldName.equals(FIELD_END_DATE)) {
            return "Arrival";
        } else if (fieldName.equals(FIELD_END_TIME)) {
            return "at";
        } else if (fieldName.equals(FIELD_CURRENCY)) {
            return "Currency";
        } else if (fieldName.equals(FIELD_COMMENT)) {
            return "Comment";
        }
        return null;
    }

    @Override
    protected boolean isFieldVisible(FormDTO model, String fieldName) {
        if (viewedType != SegmentType.RequestSegmentType.AIR || fragmentOnInitialization == TAB_ONE_WAY) {
            return layoutVisibilities.get(viewedType).contains(fieldName);
        } else {
            // --- segment number detection
            if (fragmentOnInitialization == TAB_ROUND_TRIP) {
                final RequestSegmentDTO segment = (RequestSegmentDTO) model;
                if (segment.getDisplayOrder() == 0) {
                    // --- outbound
                    if (fieldName.equals(FIELD_FROM_ID) || fieldName.equals(FIELD_TO_ID) || fieldName
                            .equals(FIELD_START_DATE) || fieldName.equals(FIELD_START_TIME) || fieldName
                            .equals(FIELD_END_DATE) || fieldName.equals(FIELD_END_TIME) || fieldName
                            .equals(FIELD_COMMENT)) {
                        return true;
                    }
                } else if (segment.getDisplayOrder() == 1) {
                    // --- return
                    if (fieldName.equals(FIELD_START_DATE) || fieldName.equals(FIELD_START_TIME) || fieldName
                            .equals(FIELD_END_DATE) || fieldName.equals(FIELD_END_TIME) || fieldName
                            .equals(FIELD_CURRENCY) || fieldName.equals(FIELD_AMOUNT) || fieldName
                            .equals(FIELD_COMMENT)) {
                        return true;
                    }
                }
            } else {
                // TAB_MULTI_LEG
            }
        }
        return false;
    }

    @Override
    protected void applySpecificRender(TextView component, LinearLayout.LayoutParams llp, ConnectFormField ff) {
        //TODO
        if (ff.getName().equals(FIELD_SEGMENT_TYPE)) {
            component.setTextAppearance(this, R.style.ListCellHeaderText);
            component.setTextColor(getResources().getColor(R.color.White));
            component.setTypeface(Typeface.DEFAULT_BOLD);
            component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
    }

    @Override
    public void applySaveButtonPolicy(View saveButtonView) {
        if (request.getApprovalStatusCode().equals(RequestDTO.ApprovalStatus.CREATION.getCode()) || request
                .getApprovalStatusCode().equals(RequestDTO.ApprovalStatus.RECALLED.getCode())) {
            saveButtonView.setVisibility(View.GONE);
        } else {
            saveButtonView.setVisibility(View.VISIBLE);
        }
        saveButtonView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // --- Applies temporary segment list to entry object
                if (originFragment != viewedFragment) {
                    if (viewedFragment == TAB_ONE_WAY) {
                        entry.setListSegment(segmentOneWay);
                    } else if (viewedFragment == TAB_ROUND_TRIP) {
                        entry.setListSegment(segmentsRoundTrip);
                    } else if (viewedFragment == TAB_MULTI_LEG) {
                        entry.setListSegment(segmentsMultiLeg);
                    }
                    originFragment = viewedFragment;
                }
                // CALL SAVE METHOD
                for (RequestSegmentDTO segment : entry.getListSegment()) {
                    save(form, segment);
                }
            }
        });
    }

    @Override
    protected void save(ConnectForm form, FormDTO model) {
        super.save(form, model);
        if (ConcurCore.isConnected()) {
            // --- creates the listener
            asyncReceiverSave.setListener(new SaveListener());
            entryVF.setDisplayedChild(ID_LOADING_VIEW);
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            //TODO new RequestSaveTask(RequestEntryActivity.this, 1, asyncReceiverSave, tr).execute();
        } else {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    public class SaveListener implements BaseAsyncRequestTask.AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            final boolean isCreation = entry.getId() == null;
            ConnectHelper.displayMessage(getApplicationContext(), "ENTRY SAVED");

            // metrics
            final Map<String, String> params = new HashMap<String, String>();

            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_TRAVEL_REQUEST_ENTRY);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST,
                    (isCreation ? Flurry.EVENT_NAME_CREATE : Flurry.EVENT_NAME_SAVED), params);

            // --- TODO Refresh values

            if (resultData != null) {

                final String entryId = RequestParser
                        .parseActionResponse(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                //TODO
                // we go to the digest screen
                /*if (isCreation) {

                    final Intent i = new Intent(RequestEntryActivity.this, RequestSummaryActivity.class);
                    i.putExtra(RequestListActivity.REQUEST_ID, requestId);

                    // --- Flurry tracking
                    i.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_ENTRY);
                    params.clear();
                    params.put(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_TRAVEL_REQUEST_ENTRY);
                    params.put(Flurry.PARAM_NAME_TO, Flurry.PARAM_VALUE_TRAVEL_REQUEST_SUMMARY);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_LAUNCH, params);

                    startActivity(i);
                    finish();
                } else {
                    final Intent resIntent = new Intent();
                    resIntent.putExtra(DO_WS_REFRESH, true);
                    setResult(Activity.RESULT_OK, resIntent);

                    finish();
                }*/

            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(R.string.tr_error_save));

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestFail");
            Log.d(Const.LOG_TAG, " onRequestFail in SaveListener...");
            entryVF.setDisplayedChild(ID_ENTRY_VIEW);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in SaveListener...");
            entryVF.setDisplayedChild(ID_ENTRY_VIEW);
        }

        @Override
        public void cleanup() {
            asyncReceiverSave.setListener(null);
        }

    }

    @Override
    protected Locale getLocale() {
        return locale;
    }

    @Override
    protected DateUtil.DatePattern getPattern() {
        return DateUtil.DatePattern.DB_INPUT;
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < getDateViews().size(); i++) {
            if ((Integer) getDateViews().get(i) == view.getId()) {
                ((DatePickerDialog) getDatePickerDialogs().get(i)).show();
            }
        }
    }

    private void cleanupReceivers() {
        // NTD
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SAVE
        // activity creation
        if (asyncReceiverSave == null) {
            asyncReceiverSave = new BaseAsyncResultReceiver(new Handler());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanupReceivers();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cleanupReceivers();
    }
}

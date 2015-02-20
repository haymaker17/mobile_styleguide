package com.concur.mobile.core.request.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.*;
import com.apptentive.android.sdk.Log;
import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.AbstractConnectFormFieldActivity;
import com.concur.mobile.core.expense.charge.activity.CurrencySpinnerAdapter;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.request.RequestPagerAdapter;
import com.concur.mobile.core.request.task.RequestEntrySaveTask;
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
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.ui.common.view.MoneyFormField;

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
    private static final String FIELD_AMOUNT = "ForeignAmount";
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

    private Intent locationIntent;

    private RequestDTO request;
    private RequestEntryDTO entry;
    private List<RequestSegmentDTO> segmentOneWay;
    private List<RequestSegmentDTO> segmentsRoundTrip;
    private List<RequestSegmentDTO> segmentsMultiLeg;

    private static Map<SegmentType.RequestSegmentType, List<String>> layoutVisibilities = new HashMap<SegmentType.RequestSegmentType, List<String>>();

    private LinearLayout currentFieldsLayout;
    private ViewFlipper entryVF;
    private ViewFlipper entryTypeVF;
    private TextView requestTitle;
    private ViewPager viewPager;
    private CurrencySpinnerAdapter curTypeAdapter;
    private TextView locationTappedView = null;

    private Map<Integer, LinearLayout> layoutPerTab;
    private int viewedFragment = -1;
    private int originFragment = -1;
    private int fragmentOnInitialization = -1;
    private boolean createMode = false;
    private boolean hasCustomLayouts = false;
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
        layoutVisibilities.put(SegmentType.RequestSegmentType.RAIL, airLayout);
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
            if (request == null) {
                ConnectHelper.displayMessage(this, "Error : request object is null !!!");
                finish();
            } else {
                entry = request.getEntriesMap().get(entryId);
                form = formFieldsCache.getFormFields(entry.getSegmentFormId());
                setCanSave(request.isActionPermitted(RequestParser.PermittedAction.SAVE) && (
                        request.getApprovalStatusCode().equals(RequestDTO.ApprovalStatus.CREATION.getCode()) || request
                                .getApprovalStatusCode().equals(RequestDTO.ApprovalStatus.RECALLED.getCode())));
                viewedType = SegmentType.RequestSegmentType.getByCode(entry.getSegmentTypeCode());
            }
        }
        // --- create mode
        else if (requestId != null) {
            createMode = true;
            request = requestListCache.getValue(requestId);
            entry = new RequestEntryDTO();
            // --- entry initialization
            entry.setListSegment(new ArrayList<RequestSegmentDTO>());
            // --- TODO
            //entry.setSegmentFormId();
            //entry.setSegmentType();
            //entry.setSegmentTypeCode();
            form = formFieldsCache.getFormFields(entry.getSegmentFormId());
            viewedType = SegmentType.RequestSegmentType.getByCode(entry.getSegmentTypeCode());
        } else {
            // TODO this might be used later if we can create directly from a segment type selection screen
            // --- This is wrong, go back to previous screen and log error
            Log.e(CLS_TAG, "requestId is null !");
            finish();
        }
        if (viewedType == null) {
            viewedType = SegmentType.RequestSegmentType.CAR;
        }
        if (form == null) {
            finish();
            Log.e(CLS_TAG, "form is null. finish() activity");
            Toast.makeText(this, getResources().getString(R.string.general_error), Toast.LENGTH_LONG);
        }

        configureUI();
    }

    private void configureUI() {
        entryVF = (ViewFlipper) findViewById(R.id.entryVF);
        entryTypeVF = (ViewFlipper) findViewById(R.id.entryTypeVF);
        entryVF.setDisplayedChild(ID_ENTRY_VIEW);
        // --- Air & Rail segments have a specific display with specific processing
        hasCustomLayouts = (viewedType == SegmentType.RequestSegmentType.AIR
                || viewedType == SegmentType.RequestSegmentType.RAIL);

        if (hasCustomLayouts) {
            // --- initializes the mapping between a tab id and the corresponding layout id
            entryTypeVF.setDisplayedChild(ID_VIEWPAGER_VIEW);

            layoutPerTab = new HashMap<Integer, LinearLayout>();
        } else {
            entryTypeVF.setDisplayedChild(ID_NO_VIEWPAGER_VIEW);
        }
        currentFieldsLayout = (LinearLayout) findViewById(R.id.formFieldsLayout);
        if (entry != null) {
            requestTitle = (TextView) findViewById(R.id.entryTitle);
            requestTitle.setText(entry.getSegmentType());
        }
        // --- we use fragments if it's air, which will launch display by themselves
        if (!hasCustomLayouts) {
            // --- only one segment possible
            this.setDisplayFields(entry.getListSegment().iterator().next(), form, currentFieldsLayout);
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
                }
            });
            // --- set the current tab & apply the temporary in use leg list
            if (createMode || entry.getTripType() == RequestEntryDTO.TripType.ONE_WAY) {
                viewedFragment = originFragment = TAB_ONE_WAY;
                segmentOneWay = entry.getListSegment();
                viewPager.setCurrentItem(TAB_ONE_WAY);
            } else if (entry.getTripType() == RequestEntryDTO.TripType.ROUND_TRIP) {
                viewedFragment = originFragment = TAB_ROUND_TRIP;
                segmentsRoundTrip = entry.getListSegment();
                viewPager.setCurrentItem(TAB_ROUND_TRIP);
            } else if (entry.getTripType() == RequestEntryDTO.TripType.MULTI_SEGMENT) {
                viewedFragment = originFragment = TAB_MULTI_LEG;
                segmentsMultiLeg = entry.getListSegment();
                //TODO multi-leg
            }
        }
    }

    /**
     * Called once by each fragment on rendering, manage display depending on the tab selected
     *
     * @param fieldLayout
     * @param idFragment
     */
    @Override
    public void initializeFragmentDisplay(final LinearLayout fieldLayout, int idFragment) {
        // --- Map layout
        layoutPerTab.put(idFragment, fieldLayout);

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
            if (createMode && inUseListSegment.size() < 1) {
                oneWaySegment = new RequestSegmentDTO();
                inUseListSegment.add(oneWaySegment);
                //oneWaySegment.setSegmentFormId(extractSegmentDefaultFormId(viewedType));
            } else if (!isOrigin) {
                // --- we only keep the first segment
                oneWaySegment = entryListSegment.iterator().next();
                inUseListSegment.add(oneWaySegment);
            } else {
                oneWaySegment = inUseListSegment.iterator().next();
            }
            oneWaySegment.setDisplayOrder(0);
            setDisplayFields(oneWaySegment, core.getRequestFormFieldsCache().getValue(entry.getSegmentFormId()),
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
            if (createMode && inUseListSegment.size() < 2) {
                final String segmentDefaultFormId = extractSegmentDefaultFormId(viewedType);

                final RequestSegmentDTO segmentD = new RequestSegmentDTO();
                //segmentD.setSegmentFormId(segmentDefaultFormId);
                inUseListSegment.add(segmentD);

                final RequestSegmentDTO segmentA = new RequestSegmentDTO();
                //segmentA.setSegmentFormId(segmentDefaultFormId);
                inUseListSegment.add(segmentA);
            } else if (!isOrigin) {
                if (entryListSegment.size() == 1) {
                    // --- adds the first segment of the current existing entry (one way)
                    final RequestSegmentDTO segmentD = entryListSegment.iterator().next();
                    inUseListSegment.add(segmentD);
                    // --- conversion operation from one way to round trip
                    final String segmentDefaultFormId = extractSegmentDefaultFormId(SegmentType.RequestSegmentType.AIR);

                    final RequestSegmentDTO segmentA = new RequestSegmentDTO();
                    //segmentA.setSegmentFormId(segmentDefaultFormId);
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
                setDisplayFields(segment, core.getRequestFormFieldsCache().getValue(entry.getSegmentFormId()),
                        fieldLayout, null, null);
                displayOrder++;
            }
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

            int displayOrder = 0;
            for (RequestSegmentDTO segment : inUseListSegment) {
                segment.setDisplayOrder(displayOrder);
                displayOrder++;
            }
        }
    }

    private boolean hasChange(List<RequestSegmentDTO> segmentList) {
        boolean hasChange = false;

        final List<ConnectFormField> formFields = form.getFormFields();
        Collections.sort(formFields);

        for (RequestSegmentDTO segment : segmentList) {
            for (ConnectFormField ff : formFields) {
                final TextView compView = getComponent(segment, ff.getName());
                final String fieldName = ff.getName();
                if (compView != null) {
                    final String displayedValue = compView.getText().toString();
                    if (fieldName.equals(FIELD_FROM_ID)) {
                        if (segment.getFromLocationName() == null) {
                            hasChange |= displayedValue.length() > 0;
                        } else {
                            hasChange |= !segment.getFromLocationName().equals(displayedValue);
                        }
                    } else if (fieldName.equals(FIELD_TO_ID)) {
                        hasChange |= (segment.getToLocationName() == null && displayedValue != null
                                && displayedValue.length() > 0) || !segment.getToLocationName().equals(displayedValue);
                    } else if (fieldName.equals(FIELD_AMOUNT)) {
                        if (compView instanceof MoneyFormField) {
                            // --- compView is a TextView if readonly, meaning it can't be modified so we don't care
                            //     about it's value in this case
                            if (entry.getForeignAmount() == null) {
                                hasChange |= displayedValue.length() > 0;
                            } else {
                                hasChange |= !entry.getForeignAmount()
                                        .equals(((MoneyFormField) compView).getAmountValue());
                            }
                        }
                    } else if (fieldName.equals(FIELD_START_DATE)) {
                        if (segment.getDepartureDate() == null) {
                            hasChange |= displayedValue.length() > 0;
                        } else {
                            hasChange |= !formatDate(segment.getDepartureDate()).equals(displayedValue);
                        }
                    } else if (fieldName.equals(FIELD_START_TIME)) {
                        // --- value is stored on a 24h format
                        String comparedValue = displayedValue;
                        if (!android.text.format.DateFormat.is24HourFormat(this)) {
                            comparedValue = convertTimeFormat(displayedValue, false, true);
                        }
                        hasChange |= !formatTime(segment.getDepartureDate(), true).equals(comparedValue);
                    } else if (fieldName.equals(FIELD_END_DATE)) {
                        if (segment.getArrivalDate() == null) {
                            hasChange |= displayedValue.length() > 0;
                        } else {
                            hasChange |= !formatDate(segment.getArrivalDate()).equals(displayedValue);
                        }
                    } else if (fieldName.equals(FIELD_END_TIME)) {
                        // --- value is stored on a 24h format
                        String comparedValue = displayedValue;
                        if (!android.text.format.DateFormat.is24HourFormat(this)) {
                            comparedValue = convertTimeFormat(displayedValue, false, true);
                        }
                        hasChange |= !formatTime(segment.getArrivalDate(), true).equals(comparedValue);
                    } else if (fieldName.equals(FIELD_CURRENCY)) {
                        // --- if hint is null, used never selected anything in the popup
                        hasChange |= compView.getHint() != null && !entry.getForeignCurrencyCode()
                                .equals(compView.getHint());
                    } else if (fieldName.equals(FIELD_COMMENT)) {
                        if (segment.getLastComment() == null) {
                            hasChange |= displayedValue.length() > 0;
                        } else {
                            hasChange |= !segment.getLastComment().equals(displayedValue);
                        }
                    }
                }
                if (hasChange) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Note : Move this to RequestGroupConfiguration in a static method if it's needed anywhere else
     *
     * @param segmentType
     * @return
     */

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
    protected String getModelDisplayedValueByFieldName(FormDTO requestSegment, String fieldName) {
        final RequestSegmentDTO segment = (RequestSegmentDTO) requestSegment;
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
            if (entry.getForeignAmount() == null) {
                return "";
            }
            return entry.getForeignAmount().toString();
        } else if (fieldName.equals(FIELD_START_DATE)) {
            if (segment.getDepartureDate() == null) {
                return "";
            }
            return formatDate(segment.getDepartureDate());
        } else if (fieldName.equals(FIELD_START_TIME)) {
            if (segment.getDepartureDate() == null) {
                return "";
            }
            return formatTime(segment.getDepartureDate(), android.text.format.DateFormat.is24HourFormat(this));
        } else if (fieldName.equals(FIELD_END_DATE)) {
            if (segment.getArrivalDate() == null) {
                return "";
            }
            return formatDate(segment.getArrivalDate());
        } else if (fieldName.equals(FIELD_END_TIME)) {
            if (segment.getArrivalDate() == null) {
                return "";
            }
            return formatTime(segment.getArrivalDate(), android.text.format.DateFormat.is24HourFormat(this));
        } else if (fieldName.equals(FIELD_CURRENCY)) {
            if (entry.getForeignCurrencyCode() == null) {
                return "";
            }
            if (curTypeAdapter == null) {
                curTypeAdapter = new CurrencySpinnerAdapter(this);
            }
            final int pos = curTypeAdapter.getPositionForCurrency(entry.getForeignCurrencyCode());
            if (pos >= 0) {
                return ((ListItem) curTypeAdapter.getItem(pos)).text;
            }
            return "";
        } else if (fieldName.equals(FIELD_COMMENT)) {
            if (segment.getLastComment() == null) {
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
            final TextView view = getComponent(model, fieldName);
            if (view != null && view.getHint() != null) {
                segment.setFromLocationId(view.getHint().toString());
                segment.setFromLocationName(value);
            }
        } else if (fieldName.equals(FIELD_TO_ID)) {
            final TextView view = getComponent(model, fieldName);
            if (view != null && view.getHint() != null) {
                segment.setToLocationId(view.getHint().toString());
                segment.setToLocationName(value);
            }
        } else if (fieldName.equals(FIELD_AMOUNT)) {
            final TextView field = RequestEntryActivity.this.getComponent(model, FIELD_AMOUNT);
            // --- field can be null if hidden
            if (field != null && field instanceof MoneyFormField) {
                entry.setForeignAmount(((MoneyFormField) field).getAmountValue());
                entry.setForeignAmount(entry.getForeignAmount());
            }
        } else if (fieldName.equals(FIELD_START_DATE)) {
            segment.setDepartureDate(parseDate(value));
        } else if (fieldName.equals(FIELD_START_TIME)) {
            if (segment.getDepartureDate() != null) {
                applyTimeString(segment.getDepartureDate(), value);
            }
        } else if (fieldName.equals(FIELD_END_DATE)) {
            segment.setArrivalDate(parseDate(value));
        } else if (fieldName.equals(FIELD_END_TIME)) {
            if (segment.getArrivalDate() != null) {
                applyTimeString(segment.getArrivalDate(), value);
            }
        } else if (fieldName.equals(FIELD_CURRENCY)) {
            // --- component is null if value is never changed
            if (getComponent(model, fieldName) != null) {
                final CharSequence currencyCodeSequence = getComponent(model, fieldName).getHint();
                final String code = currencyCodeSequence != null ? currencyCodeSequence.toString() : null;
                // --- trick to get the code back
                entry.setForeignCurrencyCode(code);
            }
        } else if (fieldName.equals(FIELD_COMMENT)) {
            segment.setLastComment(value);
        }
    }

    @Override
    protected String getLabelFromFieldName(String fieldName) {
        //TODO : labels depending on the segment type ?
        if (fieldName.equals(FIELD_FROM_ID)) {
            return getResources().getString(R.string.general_separator_from);
        } else if (fieldName.equals(FIELD_TO_ID)) {
            return getResources().getString(R.string.general_separator_to);
        } else if (fieldName.equals(FIELD_AMOUNT)) {
            return getResources().getString(R.string.amount);
        } else if (fieldName.equals(FIELD_START_DATE)) {
            return getResources().getString(R.string.general_departure);
        } else if (fieldName.equals(FIELD_START_TIME)) {
            return getResources().getString(R.string.tr_at);
        } else if (fieldName.equals(FIELD_END_DATE)) {
            return getResources().getString(R.string.tr_arrival);
        } else if (fieldName.equals(FIELD_END_TIME)) {
            return getResources().getString(R.string.tr_at);
        } else if (fieldName.equals(FIELD_CURRENCY)) {
            return getResources().getString(R.string.currency);
        } else if (fieldName.equals(FIELD_COMMENT)) {
            return getResources().getString(R.string.comment);
        }
        return null;
    }

    @Override
    protected DisplayType getDisplayType(ConnectFormField ff) {
        if (ff.getName().equals(FIELD_FROM_ID) || ff.getName().equals(FIELD_TO_ID)) {
            return DisplayType.PICKLIST;
        }
        return super.getDisplayType(ff);
    }

    @Override
    protected boolean isFieldVisible(FormDTO model, String fieldName) {
        if (!hasCustomLayouts || fragmentOnInitialization == TAB_ONE_WAY) {
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
    protected void applySpecificRender(final FormDTO model, final TextView component,
            final LinearLayout.LayoutParams llp, final ConnectFormField ff) {
        if (ff.getName().equals(FIELD_SEGMENT_TYPE)) {
            component.setTextAppearance(this, R.style.ListCellHeaderText);
            component.setTextColor(getResources().getColor(R.color.White));
            component.setTypeface(Typeface.DEFAULT_BOLD);
            component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        } else if (ff.getName().equals(FIELD_CURRENCY)) {
            component.addTextChangedListener(new TextWatcher() {

                @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    // --- ntd
                }

                @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    // --- ntd
                }

                @Override public void afterTextChanged(Editable editable) {
                    final TextView field = RequestEntryActivity.this.getComponent(model, FIELD_AMOUNT);
                    // --- field can be null if hidden
                    if (field != null && field instanceof MoneyFormField) {
                        ((MoneyFormField) field).setCurrencyCode(entry.getForeignCurrencyCode());
                    }
                }
            });
        } else if (ff.getName().equals(FIELD_AMOUNT)) {
            // --- currency initialization
            if (component instanceof MoneyFormField) {
                ((MoneyFormField) component).setCurrencyCode(entry.getForeignCurrencyCode());
            }
        }
    }

    @Override
    public void applySaveButtonPolicy(View saveButtonView) {
        if (request.isActionPermitted(RequestParser.PermittedAction.SAVE)) {
            saveButtonView.setVisibility(View.VISIBLE);
        } else {
            saveButtonView.setVisibility(View.GONE);
        }
        saveButtonView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                saveAction();
            }
        });
    }

    private void saveAction() {
        if (hasCustomLayouts) {
            // --- AIR & RAIL segment/entry types

            // --- Cleans & applies temporary segment list to entry object
            if (viewedFragment == TAB_ONE_WAY) {
                entry.setTripType(RequestEntryDTO.TripType.ONE_WAY);
                entry.setListSegment(segmentOneWay);
                if (entry.getListSegment().size() > 1) {
                    // --- security lock : we can't have more than 1 on a round way => remove anything above
                    segmentOneWay = new ArrayList<RequestSegmentDTO>();
                    segmentOneWay.add(entry.getListSegment().iterator().next());
                    entry.setListSegment(segmentOneWay);
                }
            } else if (viewedFragment == TAB_ROUND_TRIP) {
                entry.setTripType(RequestEntryDTO.TripType.ROUND_TRIP);
                entry.setListSegment(segmentsRoundTrip);
                if (entry.getListSegment().size() > 1) {
                    // --- security lock : we can't have more than 1 on a round way => remove anything above
                    segmentsRoundTrip = new ArrayList<RequestSegmentDTO>();
                    for (int i = 0; i < 2; i++) {
                        segmentsRoundTrip.add(entry.getListSegment().get(i));
                    }
                    entry.setListSegment(segmentsRoundTrip);
                }
            } else if (viewedFragment == TAB_MULTI_LEG) {
                entry.setTripType(RequestEntryDTO.TripType.MULTI_SEGMENT);
                entry.setListSegment(segmentsMultiLeg);
            }
            final int length = entry.getListSegment().size();
            for (int i = 0; i < length; i++) {
                final RequestSegmentDTO segment = entry.getListSegment().get(i);
                // --- Applies a bulletproof display order
                segment.setDisplayOrder(i);
                save(form, segment);
            }
            originFragment = viewedFragment;
        } else {
            // --- !AIR & !RAIL segment/entry types
            save(form, entry.getListSegment().iterator().next());
        }
        if (ConcurCore.isConnected()) {
            // --- creates the listener
            asyncReceiverSave.setListener(new SaveListener());
            entryVF.setDisplayedChild(ID_LOADING_VIEW);
            // --- onRequestResult calls cleanup() on execution, so listener will be destroyed by processing
            new RequestEntrySaveTask(this, 1, asyncReceiverSave, entry).execute();
        } else {
            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    @Override
    protected Locale getLocale() {
        return locale;
    }

    @Override
    protected DateUtil.DatePattern getDatePattern() {
        return DateUtil.DatePattern.DB_INPUT;
    }

    @Override
    public void onClick(final View view) {
        final CustomDatePickerDialog datePicker = getDateField((String) view.getTag());
        if (datePicker != null) {
            datePicker.setClickedView(view);
            datePicker.show();
            return;
        }
        final CustomTimePickerDialog timePicker = getTimeField((String) view.getTag());
        if (timePicker != null) {
            timePicker.setClickedView(view);
            timePicker.show();
            return;
        }
        if (view.getTag().equals(FIELD_CURRENCY)) {
            showCurrencyDialog(view);
        } else if (view.getTag().equals(FIELD_FROM_ID) || view.getTag().equals(FIELD_TO_ID)) {
            if (locationIntent == null) {
                locationIntent = new Intent(RequestEntryActivity.this, LocationSearchActivity.class);
                locationIntent.putExtra(LocationSearchActivity.EXTRA_PARAM_IS_AIRPORT, true);
            }
            locationTappedView = (TextView) view;
            startActivityForResult(locationIntent, Const.REQUEST_CODE_LOCATION);
        }
    }

    /*
     * Currency management
     * ********************************
     */

    private AlertDialog showCurrencyDialog(final View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.currency_prompt);
        if (curTypeAdapter == null) {
            curTypeAdapter = new CurrencySpinnerAdapter(this);
        }
        builder.setSingleChoiceItems(curTypeAdapter, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    Object selCurObj = curTypeAdapter.getItem(which);
                    if (selCurObj instanceof ListItem) {
                        final ListItem li = (ListItem) selCurObj;
                        if (li != null) {
                            final TextView tv = (TextView) view;
                            tv.setText(li.text);
                            tv.setHint(li.code);
                        }
                    }
                }
                dialog.dismiss();
            }
        });
        final AlertDialog alertDlg = builder.create();
        final ListView listView = alertDlg.getListView();
        listView.setTextFilterEnabled(true);
        alertDlg.show();

        return alertDlg;
    }

    /* ******************************* */

    @Override
    protected void save(ConnectForm form, final FormDTO model) {
        super.save(form, model);
    }

    @Override protected LinearLayout getCurrentFieldsLayout() {
        return hasCustomLayouts ? layoutPerTab.get(viewedFragment) : currentFieldsLayout;
    }

    public class SaveListener implements BaseAsyncRequestTask.AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            final boolean isCreation = entry.getId() == null;
            ConnectHelper.displayMessage(getApplicationContext(), "ENTRY SAVED");
            requestListCache.setDirty(true);

            // metrics
            final Map<String, String> params = new HashMap<String, String>();

            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_TRAVEL_REQUEST_ENTRY);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST,
                    (isCreation ? Flurry.EVENT_NAME_CREATE : Flurry.EVENT_NAME_SAVED), params);

            final Intent resIntent = new Intent();
            resIntent.putExtra(RequestHeaderActivity.DO_WS_REFRESH, true);
            setResult(Activity.RESULT_OK, resIntent);
            finish();
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

    private void cleanupReceivers() {
        // NTD
    }

    /*
     * (non-Javadoc)
     *
     * @see com.concur.mobile.activity.expense.ConcurView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final String selectedListItemKey = data.getStringExtra(LocationSearchActivity.EXTRA_PARAM_LOCATION_ID);
            final String selectedListItemText = data.getStringExtra(LocationSearchActivity.EXTRA_PARAM_LOCATION_NAME);
            if (locationTappedView != null) {
                if (selectedListItemKey != null) {
                    locationTappedView.setText(selectedListItemText);
                    locationTappedView.setHint(selectedListItemKey);
                } else {
                    locationTappedView.setText("");
                    locationTappedView.setHint("");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SAVE
        // activity creation
        if (asyncReceiverSave == null) {
            asyncReceiverSave = new BaseAsyncResultReceiver(new Handler());
        }
        /// --- hide keyboard
        final IBinder windowToken = entryVF.getWindowToken();
        if (windowToken != null) {
            com.concur.mobile.platform.ui.common.util.ViewUtil.hideSoftKeyboard(this, windowToken);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanupReceivers();
    }

    @Override
    public void onBackPressed() {
        List<RequestSegmentDTO> segmentList = entry.getListSegment();

        if (canSave() && hasCustomLayouts) {
            segmentList = viewedFragment == TAB_ONE_WAY ?
                    segmentOneWay :
                    (viewedFragment == TAB_ROUND_TRIP ? segmentsRoundTrip : segmentsMultiLeg);
        }
        if (canSave() && hasChange(segmentList)) {
            final AlertDialogFragment.OnClickListener yesListener = new AlertDialogFragment.OnClickListener() {

                @Override public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                    // --- save action + redirect
                    saveAction();
                }

                @Override public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                    // --- can't happen
                }
            };
            final AlertDialogFragment.OnClickListener noListener = new AlertDialogFragment.OnClickListener() {

                @Override public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // --- redirect without saving
                    finish();
                }

                @Override public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                    // --- can't happen
                }
            };
            DialogFragmentFactory.getAlertDialog(getResources().getString(R.string.confirm),
                    getResources().getString(R.string.tr_message_save_changes), R.string.general_yes, -1,
                    R.string.general_no, yesListener, null, noListener, noListener)
                    .show(getSupportFragmentManager(), CLS_TAG);
        } else {
            super.onBackPressed();
            cleanupReceivers();
        }
    }
}

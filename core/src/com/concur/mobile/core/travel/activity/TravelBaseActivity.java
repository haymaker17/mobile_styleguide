/**
 * 
 */
package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.data.CreditCard;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.TravelCustomFieldsView.TravelCustomFieldHint;
import com.concur.mobile.core.travel.data.AffinityProgram;
import com.concur.mobile.core.travel.data.CustomTravelText;
import com.concur.mobile.core.travel.data.HotelBenchmark;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.PreSellOption;
import com.concur.mobile.core.travel.data.ReasonCode;
import com.concur.mobile.core.travel.data.RefundableInfo;
import com.concur.mobile.core.travel.data.RuleEnforcementLevel;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.TravelCustomFieldsConfig;
import com.concur.mobile.core.travel.data.TravelPointsConfig;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.data.Violation.StandardViolationType;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.travel.service.GetPreSellOptions;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.travel.service.ItinerarySummaryListRequest;
import com.concur.mobile.core.travel.service.ReasonCodeReply;
import com.concur.mobile.core.travel.service.ReasonCodeRequest;
import com.concur.mobile.core.travel.service.TravelCustomFieldsRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.LayoutUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.view.SpinnerItem;

/**
 * An extension of <code>BaseActivity</code> for managing common travel-related activities, i.e., booking fields, violation
 * messages, reasons, justification, card selection, etc.
 */
public class TravelBaseActivity extends BaseActivity {

    protected static final String CLS_TAG = TravelBaseActivity.class.getSimpleName();

    /**
     * Contains the base dialog ID upon which sub-classes of <code>TravelBaseActivity</code> should base their own dialog IDs.
     */
    protected static final int DIALOG_ID_BASE = 10020;

    protected static final String TRAVEL_CUSTOM_FIELDS_RECEIVER_KEY = "travel.custom.fields.receiver";
    protected static final String REASON_CODE_RECEIVER_KEY = "reason.code.receiver";
    protected static final String INVALID_CUSTOM_FIELDS_LIST_KEY = "invalid.custom.fields.list";

    private static final String EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_LEVEL_KEY = "air.choice.enforcement.level";
    private static final String EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_TEXT = "air.choice.enforcement.text";

    private static final String LAST_CHANGED_TEXT_BUNDLE_KEY = "last.changed.text";
    protected static final String VIOLATION_JUSTIFICATION_KEY = "violation.justification";
    protected static final String VIOLATION_REASON_KEY = "violation.reason";
    private static final String NEED_VIOLATION_REASON_KEY = "need.violation.reason";
    private static final String NEED_VIOLATION_JUSTIFICATION_KEY = "need.violation.justification";
    private static final String DELAYED_VIOLATION_JUSTIFICATION_UI = "delayed.violation.justification.ui";

    private static final String BOOKING_RECORD_LOCATOR_KEY = "booking.record.locator";
    private static final String ITINERARY_LIST_RECEIVER_KEY = "itinerary.list.receiver";
    private static final String ITINERARY_RECEIVER_KEY = "itinerary.receiver";
    private static final String BOOKING_ITINERARY_LOCATOR_KEY = "booking.record.locator";

    private static final String CREDIT_CARD_KEY = "credit.card";
    private static final String NEED_CARD_CHOICE_KEY = "need.card.choice";

    private static final String AFFINITY_PROGRAM_KEY = "affinity.program";

    // pre-sell options
    protected static final String RETRIEVE_PRE_SELL_OPTIONS_RECEIVER_KEY = "retrieve.presell.options.receiver";
    protected static final String EXTRA_PRE_SELL_OPTION_KEY = "presell.option";

    protected static final int CUSTOM_FIELDS_PROGRESS_DIALOG = 10000;
    protected static final int CUSTOM_FIELDS_FAILURE_DIALOG = 10001;
    protected static final int CUSTOM_FIELDS_DYNAMIC_DIALOG = 10002;
    protected static final int CUSTOM_FIELDS_INVALID_VALUES = 10003;
    protected static final int REASON_CODE_PROGRESS_DIALOG = 10004;
    protected static final int REASON_CODE_FAILURE_DIALOG = 10005;
    protected static final int NO_CARDS_DIALOG = 10006;
    protected static final int DIALOG_SELECT_CARD = 10007;
    protected static final int REQUIRED_FIELDS_DIALOG = 10008;
    protected static final int AFFINITY_CHOICE_DIALOG = 10009;
    protected static final int AFFINITY_CHOICE_REMINDER_DIALOG = 10010;
    protected static final int NON_REFUNDABLE_REMINDER_DIALOG = 10011;
    protected static final int CONFIRM_BOOKING_DIALOG = 10012;
    protected static final int BOOKING_PROGRESS_DIALOG = 10013;
    protected static final int BOOKING_SUCCEEDED_DIALOG = 10014;
    protected static final int BOOKING_FAILED_DIALOG = 10015;
    public static final int CUSTOM_FIELDS_UPDATE_PROGRESS_DIALOG = 10016;
    public static final int CUSTOM_FIELDS_UPDATE_FAILURE_DIALOG = 10017;
    public static final int RETRIEVE_PRE_SELL_OPTIONS_DIALOG = 10018;
    public static final int RETRIEVE_PRE_SELL_OPTIONS_FAILED_DIALOG = 10019;

    protected static final String TRAVEL_CUSTOM_VIEW_FRAGMENT_TAG = "travel.custom.view";
    protected static final String TRAVEL_SELL_OPTION_VIEW_FRAGMENT_TAG = "travel.sell.option.view";
    protected static final String CANCELLATION_POLICY_DIALOG_TAG = "CancellationPolicyDialog";

    // Contains the travel custom fields receiver.
    protected TravelCustomFieldsReceiver travelCustomFieldsReceiver;
    // Contains the filter used to register the travel custom fields receiver.
    protected IntentFilter travelCustomFieldsFilter;
    // Contains a reference to an outstanding request to retrieve travel custom fields information.
    protected TravelCustomFieldsRequest travelCustomFieldsRequest;
    // Contains a reference to a fragment used to display travel custom fields.
    protected TravelCustomFieldsView travelCustomFieldsView;

    // Contains a reference to a fragment used to display flight options.
    protected SellOptionFieldsView sellOptionFieldsView;

    // Contains the last list of invalid fields.
    protected List<TravelCustomFieldHint> invalidFields;

    // Contains the reason code receiver.
    protected ReasonCodeReceiver reasonCodeReceiver;
    // Contains the filter used to register the reason code receiver.
    protected IntentFilter reasonCodeFilter;
    // Contians a reference to an outstanding request to retrieve a set of reason codes.
    protected ReasonCodeRequest reasonCodeRequest;

    // References the currently selected reason code.
    protected SpinnerItem reasonCode;
    // References the id of the currently selected reason code.
    protected String reasonCodeId;
    // References the available reason codes.
    protected SpinnerItem[] reasonCodeChoices;
    // References the justification text.
    // Contains whether or not the UI layout for violation/justification should be
    // delayed due to retrieving custom reason codes.
    protected boolean delayedViolationReasonJustificationUI;

    // Contains the currently entered justification text.
    protected String justificationText;

    // References the EditText used in the violation justification dialog.
    protected EditText textEdit;
    // Contains the last changed text as reported via the text change listener.
    // This is used to track the last changed text so that in the event of an orientation
    // change it can be re-populated into the field.
    protected String lastChangedText;

    // Contains the currently selected violation enforcement level.
    protected Integer selectedViolationEnforcementLevel;
    // Contains the currently selection violation enforcement message.
    protected String selectedViolationEnforcementText;

    // Contains whether or not (if required), the end-user has selected a violation reason.
    protected boolean needsViolationReason;
    // Contains whether or not (if required), the end-user has provided a violation justification.
    protected boolean needsViolationJustification;
    // Contains whether or not the end-user has selected a card.
    protected boolean needsCardSelection;

    // Contains the currently selected card.
    protected SpinnerItem curCardChoice;
    // Contains the list of cards.
    protected SpinnerItem[] cardChoices;

    // References the currently selected affinity program choice.
    protected SpinnerItem curAffinityChoice;
    // References the current list of possible affinity program choices.
    protected SpinnerItem[] affinityChoices;

    // Contains the Cliqbook Trip id if this hotel is being booked in the context
    // of a trip.
    protected String cliqbookTripId;

    // Contains whether a search was launched with a cliqbook trip id.
    protected boolean launchedWithCliqbookTripId;

    // Contains the receiver used to handle the results of an itinerary summary list request.
    protected ItineraryListReceiver itinerarySummaryListReceiver;

    // Contains the filter used to register the itinerary summary list receiver.
    protected IntentFilter itinerarySummaryListFilter;

    // Contains a reference to the currently outstanding itinerary summary list request.
    protected ItinerarySummaryListRequest itinerarySummaryListRequest;

    // Contains the receiver used to handle the results of an itinerary request.
    protected ItineraryReceiver itineraryReceiver;

    // Contains the filter used to register the itinerary receiver.
    protected IntentFilter itineraryFilter;

    // Contains a reference to the currently outstanding itinerary request.
    protected ItineraryRequest itineraryRequest;

    // Contains the booking record locator.
    protected String bookingRecordLocator;

    // Contains the itinerary locator.
    protected String itinLocator;

    // show dialog
    protected boolean isShown = false;

    protected BaseAsyncResultReceiver preSellOptionsReceiver;
    protected PreSellOption preSellOption;

    protected int cvvNumber;
    // Contains whether or not the end-user has provided CVV Number.
    protected boolean needsCVVNumber;
    private static final String NEED_CARD_CVV_NUMBER_KEY = "need.card.cvv.number";

    GetPreSellOptions getPresellOptions;

    protected boolean useTravelPoints;
    protected int travelPoints;
    protected String travelPointsInBank;
    protected boolean showingManageViolations;

    public static final String EXTRA_TITLE_RESOURCE_ID_KEY = "title.resource.id";
    public static final String EXTRA_CONTENT_RESOURCE_ID_KEY = "content.resource.id";
    public static final String EXTRA_SHOW_PRICE_TO_BEAT_KEY = "show.price.to.beat.list";
    protected static final String EXTRA_USE_TRAVEL_POINTS_SELECTED_KEY = "use.travel.points.selected";
    protected static final String EXTRA_REASON_CODE_SELECTED_KEY = "reason.code";
    protected static final String EXTRA_JUSTIFICATION_TEXT_KEY = "justification.text";
    protected static final String EXTRA_VIOLATION_TYPE_KEY = "violation.type";
    protected static final String EXTRA_TRAVEL_POINTS_TO_USE_KEY = "travel.points.to.use";
    protected static final String EXTRA_TRAVEL_POINTS_IN_BANK_KEY = "travel.points.in.bank";
    protected static final String EXTRA_FORMATTED_PRICE_TO_BEAT_KEY = "formatted.price.to.beat";
    public static final String EXTRA_FORMATTED_MIN_PRICE_TO_BEAT_KEY = "formatted.min.price.to.beat";
    public static final String EXTRA_FORMATTED_MAX_PRICE_TO_BEAT_KEY = "formatted.max.price.to.beat";

    protected static final String USE_TRAVEL_POINTS_RETAINER_KEY = "use.travel.points";
    protected static final String SHOWING_MANAGE_VIOLATIONS_RETAINER_KEY = "showing.manage.violations";

    protected boolean isShowRatingPrompt = false;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (retainer != null) {
            // Recover any stored invalid field list.
            if (retainer.contains(INVALID_CUSTOM_FIELDS_LIST_KEY)) {
                Object retObj = retainer.get(INVALID_CUSTOM_FIELDS_LIST_KEY);
                if (retObj != null) {
                    invalidFields = (List<TravelCustomFieldHint>) retObj;
                }
            }

            // Recover any reference to the travel custom field view fragment.
            restoreTravelCustomFieldsView();

            // Recover any reference to the sell option field view fragment.
            restoreSellOptionFieldsView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // View manageViolationsView = null;

        switch (requestCode) {
        case SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE:
            // MOB-14331
            // Check whether there a form field view should handle the onActivityResult.
            if (travelCustomFieldsView != null && travelCustomFieldsView.getFormFieldViewListener() != null
                    && travelCustomFieldsView.getFormFieldViewListener().isCurrentFormFieldViewSet()) {
                travelCustomFieldsView.getFormFieldViewListener().getCurrentFormFieldView()
                        .onActivityResult(requestCode, resultCode, data);
            }
            break;

        case Const.REQUEST_CODE_USE_TRAVEL_POINTS:

            switch (resultCode) {
            case RESULT_CANCELED:
                // the started activity was cancelled
                break;
            case RESULT_OK:
                if (data == null) {
                    useTravelPoints = true;
                } else {
                    useTravelPoints = data.getBooleanExtra(EXTRA_USE_TRAVEL_POINTS_SELECTED_KEY, false);
                    if (data.hasExtra(EXTRA_REASON_CODE_SELECTED_KEY)) {
                        reasonCode = (SpinnerItem) data.getSerializableExtra(EXTRA_REASON_CODE_SELECTED_KEY);
                    }
                    if (data.hasExtra(EXTRA_JUSTIFICATION_TEXT_KEY)) {
                        justificationText = data.getStringExtra(EXTRA_JUSTIFICATION_TEXT_KEY);
                    }
                }

                // initialize the violations view
                initViolationsViewForTravelPoints();
            }

        }
    }

    /**
     * Initialize values.
     * 
     * @param inState
     *            a bundle containing saved state.
     */
    protected void initValues(Bundle inState) {

        // The 'delayedViolationJustificatonUI' boolean state needs to be restored prior to the
        // call to 'initReasonChoices' below.
        if (inState != null) {
            delayedViolationReasonJustificationUI = inState.getBoolean(DELAYED_VIOLATION_JUSTIFICATION_UI);
        }
        // Init reason code choices.
        initReasonChoices();
        // Init the card choices.
        initCardChoices();
        // Init the affinity program choices.
        initAffinityChoices();

        initCancellationPolicyView();

        if (inState != null) {
            // Restore any currently selected air choice rule.
            if (inState.containsKey(EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_LEVEL_KEY)) {
                selectedViolationEnforcementLevel = inState.getInt(EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_LEVEL_KEY);
            }
            if (inState.containsKey(EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_TEXT)) {
                selectedViolationEnforcementText = inState.getString(EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_TEXT);
            }
            // Restore 'lastChangedText'.
            if (inState.containsKey(LAST_CHANGED_TEXT_BUNDLE_KEY)) {
                lastChangedText = inState.getString(LAST_CHANGED_TEXT_BUNDLE_KEY);
            }
            // Restore 'justificationText'.
            if (inState.containsKey(VIOLATION_JUSTIFICATION_KEY)) {
                justificationText = inState.getString(VIOLATION_JUSTIFICATION_KEY);
            }
            // Restore 'curReasonCode'.
            if (inState.containsKey(VIOLATION_REASON_KEY)) {
                reasonCodeId = inState.getString(VIOLATION_REASON_KEY);
                if (reasonCodeId != null && reasonCodeChoices != null) {
                    for (SpinnerItem resCode : reasonCodeChoices) {
                        if (resCode.id.equalsIgnoreCase(reasonCodeId)) {
                            reasonCode = resCode;
                            break;
                        }
                    }
                }
            }
            // Restore 'curAffinityChoice'.
            if (inState.containsKey(AFFINITY_PROGRAM_KEY)) {
                String affinityId = inState.getString(AFFINITY_PROGRAM_KEY);
                if (affinityId != null && affinityChoices != null) {
                    for (SpinnerItem ffFlyer : affinityChoices) {
                        if (ffFlyer.id.equalsIgnoreCase(affinityId)) {
                            curAffinityChoice = ffFlyer;
                            break;
                        }
                    }
                }
            }
            // Restore 'curCardChoice'.
            if (inState.containsKey(CREDIT_CARD_KEY)) {
                String ccId = inState.getString(CREDIT_CARD_KEY);
                if (ccId != null && cardChoices != null) {
                    for (SpinnerItem ccCard : cardChoices) {
                        if (ccCard.id.equalsIgnoreCase(ccId)) {
                            curCardChoice = ccCard;
                            break;
                        }
                    }
                }
            }
            // Restore various booleans indicating whether certain fields are required.
            if (inState.containsKey(NEED_VIOLATION_REASON_KEY)) {
                needsViolationReason = inState.getBoolean(NEED_VIOLATION_REASON_KEY);
            }
            if (inState.containsKey(NEED_VIOLATION_JUSTIFICATION_KEY)) {
                needsViolationJustification = inState.getBoolean(NEED_VIOLATION_JUSTIFICATION_KEY);
            }
            if (inState.containsKey(NEED_CARD_CHOICE_KEY)) {
                needsCardSelection = inState.getBoolean(NEED_CARD_CHOICE_KEY);
            }
            if (inState.containsKey(NEED_CARD_CVV_NUMBER_KEY)) {
                needsCVVNumber = inState.getBoolean(NEED_CARD_CVV_NUMBER_KEY);
            }
            cliqbookTripId = inState.getString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            launchedWithCliqbookTripId = inState.getBoolean(Const.EXTRA_TRAVEL_LAUNCHED_WITH_CLIQBOOK_TRIP_ID);
            // Restore the booking record locator.
            if (inState.containsKey(BOOKING_RECORD_LOCATOR_KEY)) {
                bookingRecordLocator = inState.getString(BOOKING_RECORD_LOCATOR_KEY);
            }

            // for Travel Points
            if (inState.containsKey(USE_TRAVEL_POINTS_RETAINER_KEY)) {
                useTravelPoints = inState.getBoolean(USE_TRAVEL_POINTS_RETAINER_KEY, false);
            }
            if (inState.containsKey(EXTRA_TRAVEL_POINTS_IN_BANK_KEY)) {
                travelPointsInBank = inState.getString(EXTRA_TRAVEL_POINTS_IN_BANK_KEY);
            }
            if (inState.containsKey("TravelPoints")) {
                travelPoints = inState.getInt("TravelPoints");
            }

            showingManageViolations = inState.getBoolean(SHOWING_MANAGE_VIOLATIONS_RETAINER_KEY, false);

            // end of Travel Points

        } else {
            Intent intent = getIntent();
            cliqbookTripId = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            String cliqbookTripId = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            launchedWithCliqbookTripId = (cliqbookTripId != null && cliqbookTripId.length() > 0);
        }
    }

    /**
     * Initializes the card choice view.
     */
    protected void initCardChoiceView() {
        View cardView = findViewById(R.id.card_selection);
        View layout = findViewById(R.id.layout_credit_card_selection);
        if (layout != null && isSendCreditCard() != null && !isSendCreditCard()) {
            layout.setVisibility(View.GONE);
            return;
        }
        if (cardView != null) {
            // Set the field title.
            TextView txtView = (TextView) cardView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.general_credit_card);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate 'field_name' text view!");
            }
            // Init the view.
            updateCardChoiceView();
            // Set the click handler.
            cardView.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    if (cardChoices != null && cardChoices.length > 0) {
                        showDialog(DIALOG_SELECT_CARD);
                    } else {
                        showDialog(NO_CARDS_DIALOG);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate card selection group!");
        }
    }

    // Initialize the frequent flyer information.
    protected void initAffinityChoiceView() {
        View ffSelView = findViewById(R.id.affinity_selection);
        if (ffSelView != null) {
            // Set the field title.
            TextView txtView = (TextView) ffSelView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(getAffinityFieldLabel());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initFrequentFlyer: unable to locate 'field_name' text view!");
            }
            // Set the frequent flyer information.
            txtView = (TextView) ffSelView.findViewById(R.id.field_value);
            if (txtView != null) {
                if (affinityChoices != null && affinityChoices.length > 0) {
                    // Initialize the view.
                    updateAffinityChoiceView();
                    // Set the click handler.
                    ffSelView.setOnClickListener(new OnClickListener() {

                        public void onClick(View v) {
                            showDialog(AFFINITY_CHOICE_DIALOG);
                        }
                    });
                } else {
                    // Hide the entire frequent flyer section.
                    View ffSecView = findViewById(R.id.affinity_group);
                    if (ffSecView != null) {
                        ffSecView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    /**
     * Initializes some of the UI views common across travel related activities.
     */
    protected void initUI() {
        // Set the cards information.
        initCardChoiceView();

        if (!delayedViolationReasonJustificationUI) {
            // Set the violation reason/justification reason.
            initViolationReasonJustificationView();
        }

        // Set frequent flyer number selection.
        initAffinityChoiceView();

        // Set the title on the footer button.
        Button button = (Button) findViewById(R.id.footer_button_one);
        if (button != null) {
            button.setText(R.string.general_reserve);
            List<Violation> violations = getViolations();
            if (violations != null) {
                // Disable the reserve button if booking is disabled for this flight.
                int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(violations);
                RuleEnforcementLevel ruleEnfLevel = ViewUtil.getRuleEnforcementLevel(enforcementLevel);
                if (ruleEnfLevel == RuleEnforcementLevel.INACTIVE) {
                    button.setEnabled(false);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate 'footer_button_one' button!");
        }

        initCardCVVNumberView();

        initCancellationPolicyView();

    }

    /**
     * Will update the card view based on the value of <code>curCardChoice</code>.
     */
    protected void updateCardChoiceView() {
        View cardSelectionView = findViewById(R.id.card_selection);
        if (cardSelectionView != null) {
            TextView txtView = (TextView) cardSelectionView.findViewById(R.id.field_value);
            if (curCardChoice != null) {
                txtView.setText(curCardChoice.name);
            } else {
                txtView.setText(R.string.general_select_card);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateCardChoiceView: unable to locate 'card_selection' view!");
        }
    }

    /**
     * Will update the affinity program number view based on the value of <code>curAffinityChoice</code>.
     */
    protected void updateAffinityChoiceView() {
        // Enable the frequent flyer section.
        View ffSecView = findViewById(R.id.affinity_group);
        if (ffSecView != null) {
            ffSecView.setVisibility(View.VISIBLE);
        }

        View frequentFlyerSelectionView = findViewById(R.id.affinity_selection);
        if (frequentFlyerSelectionView != null) {
            TextView txtView = (TextView) frequentFlyerSelectionView.findViewById(R.id.field_value);
            if (curAffinityChoice != null) {
                txtView.setText(curAffinityChoice.name);
            } else {
                txtView.setText(R.string.general_specify_program);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateAffinityChoiceView: unable to locate 'affinity_selection' view!");
        }
    }

    /**
     * Will update the violation justification view based on the value of <code>justificationText</code>.
     */
    protected void updateViolationJustificationView() {
        View justificationView = findViewById(R.id.violation_justification);
        if (justificationView != null) {
            TextView txtView = (TextView) justificationView.findViewById(R.id.field_value);
            if (justificationText != null) {
                txtView.setText(justificationText);
                needsViolationJustification = false;
            } else {
                txtView.setText(R.string.general_specify_justification);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateFrequentFlyerChoiceView: unable to locate 'card_selection' view!");
        }
    }

    /**
     * Determines whether a booking is refundable.
     * 
     * @return returns whether a booking is refundable.
     */
    protected boolean isBookingRefundable() {
        return true;
    }

    /**
     * Will send the request to perform the booking.
     */
    protected void sendBookingRequest() {
        // No-op.
    }

    /**
     * Will cancel the booking request.
     */
    protected void cancelBookingRequest() {
        // No-op.
    }

    /**
     * Gets the booking progress message text.
     * 
     * @return returns the booking progress message text.
     */
    protected CharSequence getBookingProgressDialogMessage() {
        return "";
    }

    /**
     * Gets the booking confirmation title.
     * 
     * @return returns the booking confirmation title.
     */
    protected CharSequence getBookingConfirmDialogTitle() {
        return "";
    }

    /**
     * Gets the booking confirmation message.
     * 
     * @return returns the booking confirmation message.
     */
    protected CharSequence getBookingConfirmDialogMessage() {
        return "";
    }

    /**
     * Gets the booking succeeded dialog title.
     * 
     * @return returns the booking succeeded dialog title.
     */
    protected CharSequence getBookingSucceededDialogTitle() {
        return getText(R.string.general_reservation_confirmed);
    }

    /**
     * Gets the booking succeeded dialog message.
     * 
     * @return returns the booking succeeded dialog message.
     */
    protected CharSequence getBookingSucceededDialogMessage() {
        return "";
    }

    /**
     * Gets the booking failed dialog title.
     * 
     * @return returns the booking failed dialog title.
     */
    protected CharSequence getBookingFailedDialogTitle() {
        return getText(R.string.general_reservation_failed);
    }

    /**
     * Gets the affinity choice dialog title.
     * 
     * @return returns the affinity choice dialog title.
     */
    protected CharSequence getAffinityChoiceDialogTitle() {
        return "";
    }

    /**
     * Gets the affinity choice reminder dialog title.
     * 
     * @return returns the affinity choice reminder dialog title.
     */
    protected CharSequence getAffinityChoiceReminderDialogTitle() {
        return "";
    }

    /**
     * Gets the affinity choice reminder dialog message.
     * 
     * @return returns the affinity choice reminder dialog message.
     */
    protected CharSequence getAffinityChoiceReminderDialogMessage() {
        return "";
    }

    /**
     * Gets the booking missing fields dialog title.
     * 
     * @return returns the booking missing fields dialog title.
     */
    protected CharSequence getBookingMissingFieldsDialogTitle() {
        return getText(R.string.dlg_book_missing_fields_title);
    }

    /**
     * Gets the booking type, i.e., Air, Car, Hotel, etc..
     * 
     * @return returns the booking type, i.e., Air, Car, Hotel, etc.
     */
    protected CharSequence getBookingType() {
        return "";
    }

    /**
     * Gets the non-refundable warning dialog title.
     * 
     * @return returns the non-refundable warning dialog title.
     */
    protected CharSequence getNonRefundableWarningDialogTitle() {
        return "";
    }

    /**
     * Gets the affinity program field label.
     * 
     * @return returns the affinity program field label.
     */
    protected CharSequence getAffinityFieldLabel() {
        return "";
    }

    /**
     * Gets the non-refundable warning dialog message.
     * 
     * @return returns the non-refundable warning dialog message.
     */
    protected CharSequence getNonRefundableWarningDialogMessage() {
        return "";
    }

    /**
     * Provides a notification that the booking has succeeded and the end-usre has selected the 'Okay' button in the succeeded
     * dialog.
     */
    protected void onBookingSucceeded() {
        // No-op.
    }

    /**
     * Gets the value of the extra <code>Const.EXTRA_PROMPT_FOR_ADD</code> that should be passed into the itinerary view if the
     * end-user is taken there after a successful booking.
     * 
     * @return returns the value of the extra <code>Const.EXTRA_PROMPT_FOR_ADD</code> that should be passed into the itinerary
     *         view if the end-user is taken there after a successful booking. {default: <code>false</code> .
     */
    protected boolean getItineraryViewPromptForAdd() {
        return false;
    }

    /**
     * Determines whether a booking is an instant purchase fare.
     * 
     * @return returns whether a booking is an instant purchase fare.
     */
    protected boolean isBookingInstantPurchaseFare() {
        return false;
    }

    /**
     * Gets the instant purchase warning dialog message.
     * 
     * @return returns the instant purchase warning dialog message.
     */
    protected CharSequence getInstantPurchaseWarningDialogMessage() {
        return getText(R.string.general_instant_purchase_warning_message);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dlg = null;
        // Check whether there a form field view should handle the dialog creation.
        if (travelCustomFieldsView != null && travelCustomFieldsView.getFormFieldViewListener() != null
                && travelCustomFieldsView.getFormFieldViewListener().isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            dlg = travelCustomFieldsView.getFormFieldViewListener().getCurrentFormFieldView().onCreateDialog(id);
        } else if (sellOptionFieldsView != null && sellOptionFieldsView.getFormFieldViewListener() != null
                && sellOptionFieldsView.getFormFieldViewListener().isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            dlg = sellOptionFieldsView.getFormFieldViewListener().getCurrentFormFieldView().onCreateDialog(id);
        } else {
            switch (id) {
            case BOOKING_PROGRESS_DIALOG: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(getBookingProgressDialogMessage());
                progDlg.setIndeterminate(true);
                progDlg.setCancelable(false);
                progDlg.setCanceledOnTouchOutside(false);
                dlg = progDlg;
                isShown = true;
                dlg.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        cancelBookingRequest();
                    }
                });

                break;
            }
            case CONFIRM_BOOKING_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getBookingConfirmDialogTitle());
                builder.setMessage(getBookingConfirmDialogMessage());
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(CONFIRM_BOOKING_DIALOG);
                        // Commit the travel custom fields.
                        commitTravelCustomFields();

                        sendBookingRequest();
                    }
                });
                builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                dlg = builder.create();
                break;
            }
            case BOOKING_SUCCEEDED_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getBookingSucceededDialogTitle());
                builder.setMessage(getBookingSucceededDialogMessage());
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        onBookingSucceeded();
                    }
                });
                dlg = builder.create();
                break;
            }
            case BOOKING_FAILED_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getBookingFailedDialogTitle());
                builder.setMessage(actionStatusErrorMessage);
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dlg = builder.create();
                break;
            }
            case CUSTOM_FIELDS_DYNAMIC_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.dlg_travel_dynamic_custom_fields_title);
                dlgBldr.setMessage(R.string.dlg_travel_dynamic_custom_fields_message);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case CUSTOM_FIELDS_FAILURE_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.dlg_travel_booking_info_title);
                dlgBldr.setMessage(R.string.travel_booking_info_unavailable_message);
                dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case CUSTOM_FIELDS_UPDATE_FAILURE_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.dlg_travel_booking_info_title);
                dlgBldr.setMessage(R.string.travel_booking_info_update_unavailable_message);
                dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case CUSTOM_FIELDS_INVALID_VALUES: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(getText(R.string.dlg_travel_booking_info_title).toString());
                dlgBldr.setCancelable(true);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        invalidFields = null;
                    }
                });
                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.travel_invalid_custom_field, null);
                dlgBldr.setView(view);
                dlg = dlgBldr.create();
                dlg.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        invalidFields = null;
                    }
                });
                break;
            }
            case CUSTOM_FIELDS_PROGRESS_DIALOG: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(this.getText(R.string.dlg_travel_retrieve_custom_fields_progress_message));
                progDlg.setIndeterminate(true);
                progDlg.setCancelable(true);
                dlg = progDlg;
                dlg.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        if (travelCustomFieldsRequest != null) {
                            travelCustomFieldsRequest.cancel();
                        }
                    }
                });
                break;
            }
            case CUSTOM_FIELDS_UPDATE_PROGRESS_DIALOG: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(this.getText(R.string.dlg_travel_retrieve_custom_fields_update_progress_message));
                progDlg.setIndeterminate(true);
                progDlg.setCancelable(true);
                dlg = progDlg;
                dlg.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        if (travelCustomFieldsView != null) {
                            travelCustomFieldsView.cancelCustomFieldsUpdate();
                        }
                    }
                });
                break;
            }
            case REASON_CODE_PROGRESS_DIALOG: {
                ProgressDialog progDlg = new ProgressDialog(this);
                progDlg.setMessage(this.getText(R.string.dlg_travel_retrieve_reason_code_progress_message));
                progDlg.setIndeterminate(true);
                progDlg.setCancelable(false);
                dlg = progDlg;
                break;
            }
            case REASON_CODE_FAILURE_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.dlg_travel_retrieve_reason_code_failure_title);
                dlgBldr.setMessage(R.string.dlg_travel_retrieve_reason_code_failure_message);
                dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case REQUIRED_FIELDS_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getBookingMissingFieldsDialogTitle());
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.book_required_field, null);
                builder.setView(view);
                dlg = builder.create();
                break;
            }
            case Const.DIALOG_TRAVEL_VIOLATION_VIEW_MESSAGE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dlg_violation_title);
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        selectedViolationEnforcementLevel = null;
                        selectedViolationEnforcementText = null;
                    }
                });
                builder.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        selectedViolationEnforcementLevel = null;
                        selectedViolationEnforcementText = null;
                    }
                });
                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.violation_message, null);
                builder.setView(view);
                dlg = builder.create();
                break;
            }
            case Const.DIALOG_TRAVEL_VIOLATION_REASON: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.general_select_reason);
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, reasonCodeChoices) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                listAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

                int selectedItem = -1;
                if (reasonCode != null) {
                    for (int i = 0; i < reasonCodeChoices.length; i++) {
                        if (reasonCode.id.equals(reasonCodeChoices[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }
                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        reasonCode = reasonCodeChoices[which];
                        needsViolationReason = false;
                        LayoutUtil.updateViolationReasonChoiceView(TravelBaseActivity.this, reasonCode);
                        removeDialog(Const.DIALOG_TRAVEL_VIOLATION_REASON);
                    }
                });
                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        removeDialog(Const.DIALOG_TRAVEL_VIOLATION_REASON);
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case AFFINITY_CHOICE_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(getAffinityChoiceDialogTitle());
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, affinityChoices) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                int selectedItem = -1;
                if (curAffinityChoice != null) {
                    for (int i = 0; i < affinityChoices.length; i++) {
                        if (curAffinityChoice.id.equals(affinityChoices[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }
                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        curAffinityChoice = affinityChoices[which];
                        updateAffinityChoiceView();
                        removeDialog(AFFINITY_CHOICE_DIALOG);
                    }
                });
                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        removeDialog(AFFINITY_CHOICE_DIALOG);
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case AFFINITY_CHOICE_REMINDER_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getAffinityChoiceReminderDialogTitle());
                builder.setMessage(getAffinityChoiceReminderDialogMessage());
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.proceed), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss this dialog, and proceed to confirm reserve.
                        removeDialog(AFFINITY_CHOICE_REMINDER_DIALOG);
                        if (!isBookingRefundable() && !isBookingInstantPurchaseFare()) {
                            showDialog(NON_REFUNDABLE_REMINDER_DIALOG);
                        } else if (validateTravelCustomFields()) {
                            // The 'validateTravelCustomFields' call will display a dialog if invalid
                            // fields are found.
                            showDialog(CONFIRM_BOOKING_DIALOG);
                        }
                    }
                });
                builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(AFFINITY_CHOICE_REMINDER_DIALOG);
                    }
                });
                dlg = builder.create();
                break;
            }
            case DIALOG_SELECT_CARD: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.general_select_card);
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, cardChoices) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                int selectedItem = -1;
                if (curCardChoice != null) {
                    for (int i = 0; i < cardChoices.length; i++) {
                        if (curCardChoice.id.equals(cardChoices[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }
                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        curCardChoice = cardChoices[which];
                        updateCardChoiceView();
                        removeDialog(DIALOG_SELECT_CARD);
                    }
                });
                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        removeDialog(DIALOG_SELECT_CARD);
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case NO_CARDS_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(Format.localizeText(this, R.string.general_missing_card_for_use_title,
                        getBookingType()));
                dlgBldr.setMessage(Format.localizeText(this, R.string.general_missing_card_for_use_message,
                        getBookingType()));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case Const.DIALOG_TRAVEL_VIOLATION_NO_REASONS: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(Format.localizeText(this, R.string.general_missing_violation_reason_title,
                        getBookingType()));
                dlgBldr.setMessage(Format.localizeText(this, R.string.general_missing_violation_reason_message,
                        getBookingType()));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case Const.DIALOG_TRAVEL_VIOLATION_JUSTIFICATION: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);

                // modified for MOB-15666
                final String customText = getTravelViolationJustificationCustomText();
                if (customText == null) {
                    // if no custom travel text then show the general title
                    dlgBldr.setTitle(R.string.general_provide_violation_justification);
                } else {
                    dlgBldr.setTitle(customText);
                }

                dlgBldr.setCancelable(true);
                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        removeDialog(Const.DIALOG_TRAVEL_VIOLATION_JUSTIFICATION);
                        lastChangedText = null;
                    }
                });
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(Const.DIALOG_TRAVEL_VIOLATION_JUSTIFICATION);
                        if (textEdit.getText() != null) {
                            justificationText = textEdit.getText().toString().trim();
                            LayoutUtil.updateViolationJustificationView(TravelBaseActivity.this, justificationText,
                                    customText);
                            needsViolationJustification = false;
                        }
                        lastChangedText = null;
                    }
                });
                dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(Const.DIALOG_TRAVEL_VIOLATION_JUSTIFICATION);
                        lastChangedText = null;
                    }
                });

                textEdit = new EditText(this);
                textEdit.setMinLines(3);
                textEdit.setMaxLines(3);
                textEdit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                textEdit.addTextChangedListener(new TextWatcher() {

                    public void afterTextChanged(Editable s) {
                        lastChangedText = s.toString();
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // No-op.
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // No-op.
                    }
                });
                dlgBldr.setView(textEdit);
                dlg = dlgBldr.create();
                break;
            }
            case NON_REFUNDABLE_REMINDER_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getNonRefundableWarningDialogTitle());
                builder.setMessage(getNonRefundableWarningDialogMessage());
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.proceed), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss this dialog, and proceed to confirm reserve.
                        removeDialog(NON_REFUNDABLE_REMINDER_DIALOG);
                        if (validateTravelCustomFields()) {
                            // The 'validateTravelCustomFields' call will display a dialog if invalid
                            // fields are found.
                            showDialog(CONFIRM_BOOKING_DIALOG);
                        }
                    }
                });
                builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        removeDialog(NON_REFUNDABLE_REMINDER_DIALOG);
                    }
                });
                dlg = builder.create();
                break;
            }
            case RETRIEVE_PRE_SELL_OPTIONS_DIALOG: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(this.getText(R.string.general_loading));
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        // MOB-15382 - cancel the async task
                        if (getPresellOptions != null) {
                            getPresellOptions.cancel(true);
                            removeDialog(RETRIEVE_PRE_SELL_OPTIONS_DIALOG);

                        }

                    }
                });
                dlg = dialog;
                break;
            }
            case RETRIEVE_PRE_SELL_OPTIONS_FAILED_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(this.getText(R.string.retrieve_pre_sell_options_failed_message));
                builder.setCancelable(true);
                builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dlg = builder.create();
                break;
            }
            default: {
                dlg = super.onCreateDialog(id);
                break;
            }
            }
        }
        return dlg;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case CUSTOM_FIELDS_INVALID_VALUES: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            TableLayout tblLayout = (TableLayout) alertDlg.findViewById(R.id.field_list_table);
            if (tblLayout != null) {
                // First, clear out all rows in 'tblLayout' except for the header row.
                for (int rowInd = tblLayout.getChildCount() - 1; rowInd >= 0; --rowInd) {
                    View tblRowView = tblLayout.getChildAt(rowInd);
                    if (tblRowView.getId() != R.id.field_list_table_header) {
                        tblLayout.removeViewAt(rowInd);
                    }
                }
                // Iterate over the list of invalid fields and inflate one instance of
                // 'travel_invalid_custom_field_row' view per field.
                if (invalidFields != null && invalidFields.size() > 0) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (TravelCustomFieldHint tcfh : invalidFields) {

                        TableRow tblRow = (TableRow) inflater.inflate(R.layout.travel_invalid_custom_field_row, null);
                        // Set the field name.
                        TextView txtView = (TextView) tblRow.findViewById(R.id.field_row_name);
                        if (txtView != null) {
                            txtView.setText(tcfh.fieldName);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing 'field_row_name' text view!");
                        }
                        // Set the field hint.
                        txtView = (TextView) tblRow.findViewById(R.id.field_row_hint);
                        if (txtView != null) {
                            txtView.setText(tcfh.hintText);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing 'field_row_hint' text view!");
                        }
                        // Add it to the table layout.
                        tblLayout.addView(tblRow);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: empty list of invalid fields!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing table layout view!");
            }
            break;
        }
        case REQUIRED_FIELDS_DIALOG: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            // Update the table contents.
            TableLayout tblLayout = (TableLayout) alertDlg.findViewById(R.id.field_list_table);
            if (tblLayout != null) {
                // First, clear out all rows in 'tblLayout'.
                tblLayout.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);
                // Check for violation reason needing selecting.
                if (needsViolationReason) {
                    TableRow tblRow = (TableRow) inflater.inflate(R.layout.book_required_field_row, null);
                    // Set the message.
                    TextView txtView = (TextView) tblRow.findViewById(R.id.field_row_current_value);
                    if (txtView != null) {
                        txtView.setText(R.string.dlg_book_missing_field_violation_reason);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing field row field name text view!");
                    }
                    // Add it to the table layout.
                    tblLayout.addView(tblRow);
                }
                // Check for violation justification needing selecting.
                if (needsViolationJustification) {
                    TableRow tblRow = (TableRow) inflater.inflate(R.layout.book_required_field_row, null);
                    // Set the message.
                    TextView txtView = (TextView) tblRow.findViewById(R.id.field_row_current_value);
                    if (txtView != null) {
                        txtView.setText(R.string.dlg_book_missing_field_violation_justification);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing field row field name text view!");
                    }
                    // Add it to the table layout.
                    tblLayout.addView(tblRow);
                }
                // Check for credit card selection.
                if (needsCardSelection) {
                    TableRow tblRow = (TableRow) inflater.inflate(R.layout.book_required_field_row, null);
                    // Set the message.
                    TextView txtView = (TextView) tblRow.findViewById(R.id.field_row_current_value);
                    if (txtView != null) {
                        txtView.setText(R.string.dlg_book_missing_field_card_selection);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing field row field name text view!");
                    }
                    // Add it to the table layout.
                    tblLayout.addView(tblRow);
                }
                // Check for credit card cvv number.
                if (needsCVVNumber) {
                    TableRow tblRow = (TableRow) inflater.inflate(R.layout.book_required_field_row, null);
                    // Set the message.
                    TextView txtView = (TextView) tblRow.findViewById(R.id.field_row_current_value);
                    if (txtView != null) {
                        txtView.setText(R.string.dlg_book_missing_field_cvv_number);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing field row field name text view!");
                    }
                    // Add it to the table layout.
                    tblLayout.addView(tblRow);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing table layout view!");
            }
            break;
        }
        case Const.DIALOG_TRAVEL_VIOLATION_JUSTIFICATION: {
            if (textEdit != null) {
                String txtVal = (justificationText != null) ? justificationText : "";
                if (lastChangedText != null) {
                    txtVal = lastChangedText;
                }
                textEdit.setText(txtVal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: textEdit is null!");
            }
            break;
        }
        case Const.DIALOG_TRAVEL_VIOLATION_VIEW_MESSAGE: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            TextView txtView = (TextView) alertDlg.findViewById(R.id.rule_message);
            if (txtView != null) {
                // Set the icon.
                if (selectedViolationEnforcementLevel != null) {
                    switch (ViewUtil.getRuleEnforcementLevel(selectedViolationEnforcementLevel)) {
                    case NONE: {
                        alertDlg.setIcon(R.drawable.icon_informational);
                        break;
                    }
                    case WARNING: {
                        alertDlg.setIcon(R.drawable.icon_yellowex);
                        break;
                    }
                    case ERROR: {
                        alertDlg.setIcon(R.drawable.icon_redex);
                        break;
                    }
                    case HIDE: {
                        // No-op.
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onPrepareDialog: airchoice has rule enforcement level of 'hidden'!");
                        break;
                    }
                    default:
                        break;
                    }
                } else {
                    alertDlg.setIcon(0);
                }
                // Set the text.
                if (selectedViolationEnforcementText != null) {
                    txtView.setText(selectedViolationEnforcementText);
                } else {
                    txtView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate 'rule_message' text view!");
            }
            break;
        }
        case BOOKING_FAILED_DIALOG: {
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            // Store travel custom fields receiver.
            if (travelCustomFieldsReceiver != null) {
                retainer.put(TRAVEL_CUSTOM_FIELDS_RECEIVER_KEY, travelCustomFieldsReceiver);
                travelCustomFieldsReceiver.setActivity(null);
            }
            // Store reason code receiver.
            if (reasonCodeReceiver != null) {
                retainer.put(REASON_CODE_RECEIVER_KEY, reasonCodeReceiver);
                reasonCodeReceiver.setActivity(null);
            }
            // Store current list of invalid travel custom fields.
            if (invalidFields != null) {
                retainer.put(INVALID_CUSTOM_FIELDS_LIST_KEY, invalidFields);
            }
            // Save the itinerary list receiver
            if (itinerarySummaryListReceiver != null) {
                // Clear the activity reference, it will be set in the 'onCreate' method.
                itinerarySummaryListReceiver.setActivity(null);
                // Store it in the retainer
                retainer.put(ITINERARY_LIST_RECEIVER_KEY, itinerarySummaryListReceiver);
            }
            // Save the itinerary receiver.
            if (itineraryReceiver != null) {
                // Clear the activity reference, it will be set in the 'onCreate' method.
                itineraryReceiver.setActivity(null);
                // Store it in the retainer
                retainer.put(ITINERARY_RECEIVER_KEY, itineraryReceiver);
            }
            // Save the booking record locator.
            if (bookingRecordLocator != null) {
                retainer.put(BOOKING_RECORD_LOCATOR_KEY, bookingRecordLocator);
            }
            // Save the itinerary locator.
            if (itinLocator != null) {
                retainer.put(BOOKING_ITINERARY_LOCATOR_KEY, itinLocator);
            }

            // pre-sell options
            if (preSellOptionsReceiver != null) {
                preSellOptionsReceiver.setListener(null);
                retainer.put(RETRIEVE_PRE_SELL_OPTIONS_RECEIVER_KEY, preSellOptionsReceiver);
            }
            if (preSellOption != null) {
                retainer.put(EXTRA_PRE_SELL_OPTION_KEY, preSellOption);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        outState.putBoolean(Const.EXTRA_TRAVEL_LAUNCHED_WITH_CLIQBOOK_TRIP_ID, launchedWithCliqbookTripId);
        outState.putBoolean(DELAYED_VIOLATION_JUSTIFICATION_UI, delayedViolationReasonJustificationUI);
        outState.putBoolean(NEED_VIOLATION_REASON_KEY, needsViolationReason);
        outState.putBoolean(NEED_VIOLATION_JUSTIFICATION_KEY, needsViolationJustification);
        if (selectedViolationEnforcementLevel != null) {
            outState.putInt(EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_LEVEL_KEY, selectedViolationEnforcementLevel);
        }
        if (selectedViolationEnforcementText != null) {
            outState.putString(EXTRA_AIR_CHOICE_RULE_ENFORCEMENT_TEXT, selectedViolationEnforcementText);
        }
        if (lastChangedText != null) {
            outState.putString(LAST_CHANGED_TEXT_BUNDLE_KEY, lastChangedText);
        }
        if (justificationText != null) {
            outState.putString(VIOLATION_JUSTIFICATION_KEY, justificationText);
        }
        if (reasonCode != null) {
            outState.putString(VIOLATION_REASON_KEY, reasonCode.id);
        }
        if (curCardChoice != null) {
            outState.putString(CREDIT_CARD_KEY, curCardChoice.id);
        }
        if (curAffinityChoice != null) {
            outState.putString(AFFINITY_PROGRAM_KEY, curAffinityChoice.id);
        }
        outState.putBoolean(NEED_CARD_CHOICE_KEY, needsCardSelection);
        outState.putBoolean(NEED_CARD_CVV_NUMBER_KEY, needsCVVNumber);
        // Save the booking record locator.
        if (bookingRecordLocator != null) {
            outState.putString(BOOKING_RECORD_LOCATOR_KEY, bookingRecordLocator);
        }
        // Save the itinerary locator.
        if (itinLocator != null) {
            outState.putString(BOOKING_ITINERARY_LOCATOR_KEY, itinLocator);
        }

        // for Travel Points
        outState.putBoolean(USE_TRAVEL_POINTS_RETAINER_KEY, useTravelPoints);

        if (travelPointsInBank != null) {
            outState.putString(EXTRA_TRAVEL_POINTS_IN_BANK_KEY, travelPointsInBank);
        }
        if (travelPoints != 0) {
            outState.putInt("TravelPoints", travelPoints);
        }

        outState.putBoolean(SHOWING_MANAGE_VIOLATIONS_RETAINER_KEY, showingManageViolations);
        // end of Travel Points
    }

    /**
     * Will restore any outstanding receivers waiting on messages.
     */
    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore travel custom fields receiver.
            if (retainer.contains(TRAVEL_CUSTOM_FIELDS_RECEIVER_KEY)) {
                travelCustomFieldsReceiver = (TravelCustomFieldsReceiver) retainer
                        .get(TRAVEL_CUSTOM_FIELDS_RECEIVER_KEY);
                if (travelCustomFieldsReceiver != null) {
                    travelCustomFieldsReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer contains a null travel custom fields receiver!");
                }
            }
            // Restore reason code receiver.
            if (retainer.contains(REASON_CODE_RECEIVER_KEY)) {
                reasonCodeReceiver = (ReasonCodeReceiver) retainer.get(REASON_CODE_RECEIVER_KEY);
                if (reasonCodeReceiver != null) {
                    reasonCodeReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer contains a null reason code receiver!");
                }
            }
            // Restore the itinerary summary list receiver.
            if (retainer.contains(ITINERARY_LIST_RECEIVER_KEY)) {
                itinerarySummaryListReceiver = (ItineraryListReceiver) retainer.get(ITINERARY_LIST_RECEIVER_KEY);
                if (itinerarySummaryListReceiver != null) {
                    itinerarySummaryListReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer contains a null itinerary list receiver!");
                }
            }
            // Restore the itinerary receiver.
            if (retainer.contains(ITINERARY_RECEIVER_KEY)) {
                itineraryReceiver = (ItineraryReceiver) retainer.get(ITINERARY_RECEIVER_KEY);
                if (itineraryReceiver != null) {
                    itineraryReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer contains a null itinerary receiver!");
                }
            }

            // pre-sell options
            if (retainer.contains(RETRIEVE_PRE_SELL_OPTIONS_RECEIVER_KEY)) {
                preSellOptionsReceiver = (BaseAsyncResultReceiver) retainer.get(RETRIEVE_PRE_SELL_OPTIONS_RECEIVER_KEY);
                preSellOptionsReceiver.setListener(new TravelPreSellOptionsListener());
            }
            if (retainer.contains(EXTRA_PRE_SELL_OPTION_KEY)) {
                preSellOption = (PreSellOption) retainer.get(EXTRA_PRE_SELL_OPTION_KEY);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: retainer is null!");
        }
    }

    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.footer_button_one) {
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(getViolations());
                RuleEnforcementLevel ruleEnfLevel = ViewUtil.getRuleEnforcementLevel(enforcementLevel);
                if ((ruleEnfLevel == RuleEnforcementLevel.ERROR || ruleEnfLevel == RuleEnforcementLevel.WARNING)
                        && (reasonCodeChoices == null || reasonCodeChoices.length == 0)) {
                    // Reason selection required but no reasons!
                    showDialog(Const.DIALOG_TRAVEL_VIOLATION_NO_REASONS);
                } else if (cardChoices == null) {
                    // Card selection required but no cards!
                    showDialog(NO_CARDS_DIALOG);
                } else if (showRequiredFieldsDialog()) {
                    showDialog(REQUIRED_FIELDS_DIALOG);
                } else if (showAffinityReminderDialog()) {
                    showDialog(AFFINITY_CHOICE_REMINDER_DIALOG);
                } else if (!isBookingRefundable() && ViewUtil.isShowNonRefundableMessageEnabled(this)
                        && hasNonRefundableMessage() && !isBookingInstantPurchaseFare()) {
                    // MOB-15102 - if booking is instant purchase then we also show the message about non-refund
                    showDialog(NON_REFUNDABLE_REMINDER_DIALOG);
                } else if (validateTravelCustomFields()) {
                    // The 'validateTravelCustomFields' call will display a dialog if invalid
                    // fields are found.
                    showDialog(CONFIRM_BOOKING_DIALOG);
                }
            }
        }
    }

    /**
     * Gets whether or not there is a non-refundable warning message defined in SystemConfig.
     * 
     * @return returns whether or not there is a non-refundable warning message defined in SystemConfig.
     */
    protected boolean hasNonRefundableMessage() {
        boolean retVal = false;
        ConcurCore concurCore = getConcurCore();
        SystemConfig sysConfig = concurCore.getSystemConfig();
        if (sysConfig != null) {
            RefundableInfo refInfo = sysConfig.getRefundableInfo();
            if (refInfo != null) {
                retVal = (refInfo.message != null && refInfo.message.length() > 0);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasNonRefundableMessage: refInfo is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".hasNonRefundableMessage: sysConfig is null!");
        }
        return retVal;
    }

    /**
     * Determines whether any fields are missing values required for air booking and will set the various 'needsXXX' boolean
     * values accordingly.
     * 
     * @return returns <code>true</code> if the required fiels dialog should be displayed.
     */
    protected boolean showRequiredFieldsDialog() {
        boolean retVal = false;

        // Init the fields.
        needsViolationReason = false;
        needsViolationJustification = false;
        needsCardSelection = false;
        needsCVVNumber = false;

        // validate violation required fields
        retVal = isViolationRequiredFieldsNeeded();

        // Check for whether card has been selected.
        if (isSendCreditCard()) {
            if (curCardChoice == null) {
                needsCardSelection = true;
                retVal = true;
            }
        }

        if (preSellOption != null && preSellOption.isCvvNumberRequired() && cvvNumber == 0) {
            needsCVVNumber = true;
            retVal = true;
        }

        return retVal;
    }

    // Validate violation required fields
    protected boolean isViolationRequiredFieldsNeeded() {
        boolean retVal = false;

        // First check for needing violation reason selection.
        int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(getViolations());
        RuleEnforcementLevel ruleEnfLevel = ViewUtil.getRuleEnforcementLevel(enforcementLevel);
        if (ruleEnfLevel == RuleEnforcementLevel.ERROR || ruleEnfLevel == RuleEnforcementLevel.WARNING) {
            // if Use Travel Points selected?
            if (canRedeemTravelPointsAgainstViolations() && useTravelPoints) {
                retVal = false;
            } else {

                // Violation reason selected?
                if (reasonCode == null) {
                    needsViolationReason = true;
                    retVal = true;
                }
                // Violation justification provided?
                ConcurCore concurCore = getConcurCore();
                SystemConfig sysConfig = concurCore.getSystemConfig();
                if (sysConfig == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".showRequiredFieldsDialog: sysConfig is null!");
                }
                if (justificationText == null || justificationText.length() == 0) {
                    // Check whether SystemConfig stipulates that 'violation justification' is required.
                    if (sysConfig != null && sysConfig.getRuleViolationExplanationRequired() != null
                            && sysConfig.getRuleViolationExplanationRequired()) {
                        needsViolationJustification = true;
                        retVal = true;
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Will determine whether the affinity reminder dialog should be displayed.
     * 
     * @return whether the affinity reminder dialog should be displayed.
     */
    protected boolean showAffinityReminderDialog() {
        boolean retVal = false;
        retVal = (affinityChoices != null && affinityChoices.length > 0 && curAffinityChoice == null);
        return retVal;
    }

    /**
     * Gets the appropriate affinity program list for the booking type.
     * 
     * @return
     */
    protected List<AffinityProgram> getAffinities() {
        List<AffinityProgram> affinities = null;
        return affinities;
    }

    /**
     * Initializes the list of spinner objects representing frequent flyer choices.
     */
    protected void initAffinityChoices() {
        // modified for MOB-14317
        List<AffinityProgram> affinities = (preSellOption == null ? null : preSellOption.getAffinityPrograms());

        if (affinities != null && affinities.size() > 0) {
            affinityChoices = new SpinnerItem[affinities.size()];
            for (int affInd = 0; affInd < affinities.size(); ++affInd) {
                AffinityProgram aff = affinities.get(affInd);
                affinityChoices[affInd] = new SpinnerItem(aff.programId, aff.description);
                // default program
                if (aff.defaultProgram) {
                    curAffinityChoice = affinityChoices[affInd];
                }
            }
        }
    }

    /**
     * Gets the appropriate list of <code>CreditCard</code> objects based on the booking type.
     * 
     * @return returns a list of <code>CreditCard</code> objects based on the booking type.
     */
    protected List<CreditCard> getCards() {
        List<CreditCard> cards = null;
        return cards;
    }

    /**
     * Gets the appropriate <code>CreditCard</code> object based on the booking type.
     * 
     * @return returns the appropriate <code>CreditCard</object> based on the appropriate booking type.
     */
    protected CreditCard getDefaultCard() {
        CreditCard card = null;
        return card;
    }

    /**
     * Initializes the list of spinner objects representing card choices and will initialize 'curCardChoice' to the spinner item
     * representing the default card, if any.
     */
    protected void initCardChoices() {
        // modified for MOB-14317
        List<CreditCard> cards = (preSellOption == null ? null : preSellOption.getCreditCards());

        if (cards != null) {
            // modified for MOB-14317
            CreditCard defaultCard = (preSellOption == null ? null : preSellOption.getDefualtCreditCard());

            if (defaultCard == null) {
                // MOB-13825 in case of corporate ghost card configured, the number of cards will be cards+1, hence try to set
                // default card here
                defaultCard = (cards.size() == 1) ? cards.get(0) : null;
            }
            // Construct some spinner objects which will be used to populate a dialog.
            cardChoices = new SpinnerItem[cards.size()];
            for (int cardChInd = 0; cardChInd < cards.size(); ++cardChInd) {
                CreditCard card = cards.get(cardChInd);
                String numberSubstring = card.ccLastFour;
                String displayName = String.format("%s %s", card.name == null ? "" : card.name,
                        numberSubstring == null ? "" : numberSubstring);
                cardChoices[cardChInd] = new SpinnerItem(Integer.toString(card.ccId), displayName);
                if (curCardChoice == null && defaultCard != null && defaultCard.ccId == card.ccId) {
                    curCardChoice = cardChoices[cardChInd];
                }
            }
        }
    }

    /**
     * Initializes the list of spinner objects representing violation reason choices.
     */
    protected void initReasonChoices() {

        List<Violation> violations = getViolations();

        if (!delayedViolationReasonJustificationUI && violations != null) {

            // If airChoice contains custom violation types but the set of reason codes has been
            // downloaded, or airChoice does not have custom violation types and SystemConfig is available
            // then create the list of reason choices based on violation types in 'airChoice'. Otherwise,
            // send a request to the server to retrieve the custom codes.
            boolean hasCustomViolationType = hasCustomViolationType(violations);
            if ((hasCustomViolationType && getConcurCore().getReasonCodes() != null)
                    || (!hasCustomViolationType && getConcurCore().getSystemConfig() != null)) {

                // Iterate through the violations and pass the reason code list in to get
                // populated.
                List<ReasonCode> reasonCodes = null;
                for (Violation viol : violations) {
                    reasonCodes = getReasonCodesForType(viol.violationType, reasonCodes);
                }

                // Set up the violation justification reasons.
                if (reasonCodes != null && reasonCodes.size() > 0) {
                    // Construct some spinner objects which will be used to populate a dialog.
                    reasonCodeChoices = new SpinnerItem[reasonCodes.size()];
                    for (int resCodeInd = 0; resCodeInd < reasonCodes.size(); ++resCodeInd) {
                        ReasonCode resCode = reasonCodes.get(resCodeInd);
                        reasonCodeChoices[resCodeInd] = new SpinnerItem(resCode.id, resCode.description);
                    }
                    // set reasoncodechoices in the app object that needed for Travel Points Manager Approval activity
                    ConcurCore core = (ConcurCore) ConcurCore.getContext();
                    // core.setReasonCodeChoices(reasonCodeChoices);
                }
                delayedViolationReasonJustificationUI = false;
            } else {
                delayedViolationReasonJustificationUI = true;
                sendReasonCodeRequest();
            }
        }
    }

    /**
     * Handles notification that the list of reason codes have been received.
     */
    protected void onReasonCodesReceived() {
        delayedViolationReasonJustificationUI = false;
        initReasonChoices();
        if (reasonCodeId != null && reasonCodeChoices != null) {
            for (SpinnerItem resCode : reasonCodeChoices) {
                if (resCode.id.equalsIgnoreCase(reasonCodeId)) {
                    reasonCode = resCode;
                    break;
                }
            }
        }
        initViolationReasonJustificationView();
    }

    /**
     * Initialize the violation justification/reason UI.
     */
    protected void initViolationReasonJustificationView() {

        View reasonsCodeView = findViewById(R.id.violation_reason);
        View justificationView = findViewById(R.id.violation_justification);
        View messagesView = findViewById(R.id.violation);

        if (canRedeemTravelPointsAgainstViolations()) {

            // show the Violations label
            findViewById(R.id.violations_with_travel_points_header).setVisibility(View.VISIBLE);

            // override the violation reason UI element with the static message
            TextView txtView = (TextView) reasonsCodeView.findViewById(R.id.field_name);

            // if activity restarted then re-initialize the UI
            if (showingManageViolations) {
                initViolationsViewForTravelPoints();
            } else {
                if (txtView != null) {
                    txtView.setText(R.string.general_violation_reason);
                }
                txtView = (TextView) reasonsCodeView.findViewById(R.id.field_value);
                if (txtView != null) {
                    txtView.setText(R.string.travel_points_address_violations_label);
                }

                reasonsCodeView.setOnClickListener(new ManageViolationsOnClickListener());

                // hide the violation justification UI element
                justificationView.setVisibility(View.GONE);

                // hide the violation messgaes UI element
                messagesView.setVisibility(View.GONE);

                // default to use travel points option
                useTravelPoints = true;
            }

        } else {
            if (getViolations() != null && getViolations().size() > 0) {
                // show the Violations label
                View violationsWithTP = findViewById(R.id.violations_with_travel_points_header);
                if (violationsWithTP != null) {
                    violationsWithTP.setVisibility(View.VISIBLE);
                }
            }

            // hide the violation justification UI element
            justificationView.setVisibility(View.VISIBLE);

            // hide the violation messgaes UI element
            messagesView.setVisibility(View.VISIBLE);

            // Construct the violation message click listener.
            OnClickListener violationClickListener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (v.getTag() instanceof Violation) {
                        Violation violation = (Violation) v.getTag();
                        selectedViolationEnforcementLevel = violation.enforcementLevel;
                        selectedViolationEnforcementText = violation.message;
                        showDialog(Const.DIALOG_TRAVEL_VIOLATION_VIEW_MESSAGE);
                    }
                }
            };

            LayoutUtil.layoutViolations(this, getViolations(), reasonCodeChoices, reasonCode, violationClickListener,
                    justificationText, getTravelViolationJustificationCustomText());
        }
    }

    /**
     * Gets the list of <code>Violation</code> objects associated with a fare.
     * 
     * @return returns a list of violations; otherwise, returns <code>null</code>.
     */
    protected List<Violation> getViolations() {
        return null;
    }

    /**
     * Check whether we need to send credit card detail. Required check for car reservation;
     * 
     * @retuns true: if need to send credit card detail
     */
    protected Boolean isSendCreditCard() {
        return true;
    }

    /**
     * Determines whether there exists at least one custom violation type in <code>violations</code>. <br>
     * <br>
     * <b>NOTE:</b>For the "standard types" of "I", "X" or "G", this method will return <code>true</code>.
     * 
     * @param violations
     *            the list of violations.
     * @return returns whether there exists at least one custom violation type in <code>violations</code>.
     */
    protected boolean hasCustomViolationType(List<Violation> violations) {
        boolean retVal = false;
        if (violations != null) {
            for (Violation violation : violations) {
                try {
                    Violation.StandardViolationType stdViolType = Violation.StandardViolationType
                            .fromString(violation.violationType);
                    // Check for a "standard" type of "I", "X" or "G".
                    if (stdViolType == Violation.StandardViolationType.ITINERARY
                            || stdViolType == Violation.StandardViolationType.AIR_EXCHANGE
                            || stdViolType == Violation.StandardViolationType.GENERAL) {
                        retVal = true;
                        break;
                    }
                } catch (IllegalArgumentException ilaExc) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Will fill into <code>currentCodes</code> the appropriate set of <code>ReasonCode</code> objects based on
     * <code>violationType</code> ensuring duplicates are not added to <code>currentCodes</code>.
     * 
     * @param violationType
     *            the violation type.
     * @param currentCodes
     *            the current populated list of codes.
     * @return returns a new list of <code>ReasonCode</code> objects if <code>currentCodes</code> is <code>null</code>; otherwise
     *         <code>currentCodes</code> is populated.
     */
    protected List<ReasonCode> getReasonCodesForType(String violationType, List<ReasonCode> currentCodes) {
        List<ReasonCode> codes = currentCodes;
        // First check if 'violationType' is a "standard type", and one of Air, Hotel or Car. If so, then use the
        // appropriate set from SystemConfig. If SystemConfig is not available for some reason, then form the set from the
        // set of custom reason codes (which actually contain all types).
        if (violationType != null && violationType.length() > 0) {
            StandardViolationType stdViolType = null;
            try {
                stdViolType = Violation.StandardViolationType.fromString(violationType);
            } catch (IllegalArgumentException ilaExc) {
                // No-op.
            }
            List<ReasonCode> candidateList = null;
            if (stdViolType != null) {
                SystemConfig sysConfig = getConcurCore().getSystemConfig();
                if (sysConfig != null) {
                    switch (stdViolType) {
                    case AIR: {
                        candidateList = sysConfig.getAirReasons();
                        break;
                    }
                    case HOTEL: {
                        candidateList = sysConfig.getHotelReasons();
                        break;
                    }
                    case CAR: {
                        candidateList = sysConfig.getCarReasons();
                        break;
                    }
                    case ITINERARY:
                    case GENERAL:
                    case AIR_EXCHANGE: {
                        // The above types will be fetched out of the "custom" list since it includes all reason codes, i.e., for
                        // both
                        // the standard and custom violation types.
                        break;
                    }
                    }
                }
            }
            // Form the candidate list by looking up 'violationType'.
            if (candidateList == null) {
                // If 'candidateList' is 'null', then either 'violationType' represents a "custom type", or a "standard type" that
                // has
                // no representation in SystemConfig, i.e., 'ITINERARY', 'GENERAL' or 'AIR_EXCHANGE' or SysConfig is not
                // available.
                // So, in the code below, if there's a match on 'violationType', then the reason code will be added to
                // 'candidateList'.
                // Else, if the 'violationType' is of type 'GENERAL' and 'stdViolType' is non-null, then the 'GENERAL' type will
                // be added
                // to 'candidateList'. This permits all 'GENERAL' types to be included in 'candidateList' when 'violationType'
                // represents
                // a "standard" type. However, for non-"standard" types, 'GENERAL' will not be included since 'stdViolType' will
                // be 'null'.
                ReasonCodeReply rcReply = getConcurCore().getReasonCodes();
                if (rcReply != null) {
                    if (rcReply.reasonCodes != null) {
                        for (ReasonCode rc : rcReply.reasonCodes) {
                            if ((rc.violationType != null && rc.violationType.equalsIgnoreCase(violationType))
                                    || (rc.violationType != null
                                            && rc.violationType
                                                    .equalsIgnoreCase(Violation.StandardViolationType.GENERAL.name()) && stdViolType != null)) {
                                if (candidateList == null) {
                                    candidateList = new ArrayList<ReasonCode>();
                                }
                                candidateList.add(rc);
                            }
                        }
                    } else {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".getReasonCodesForType: custom reason code set is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getReasonCodesForType: custom reason codes not downloaded!");
                }
            }
            // Examine the candidate list and add to 'codes' blocking dups from being added.
            if (candidateList != null) {
                if (codes == null) {
                    codes = new ArrayList<ReasonCode>();
                }
                // Iterate through the candidate list and add those codes to 'codes' if there is no reason code in the list
                // matching on 'id'.
                for (ReasonCode rc : candidateList) {
                    boolean foundCode = false;
                    for (ReasonCode retCode : codes) {
                        if (retCode.id != null && rc.id != null && retCode.id.equalsIgnoreCase(rc.id)) {
                            foundCode = true;
                            break;
                        }
                    }
                    if (!foundCode) {
                        codes.add(rc);
                    }
                }
            }
        }
        return codes;
    }

    /**
     * Will determine if there are travel custom fields and whether all required fields have values.
     * 
     * @return returns whether all required travel custom fields have been filled out.
     */
    protected boolean isMissingRequiredTravelCustomFields() {
        boolean retVal = true;
        if (travelCustomFieldsView != null) {
            retVal = travelCustomFieldsView.isMissingRequiredFields();
        }
        return retVal;
    }

    /**
     * Will validate the set of travel custom fields. If the fields are found to be invalid, then this method will display a
     * dialog.
     * 
     * @return returns whether the custom fields had invalid values.
     */
    protected boolean validateTravelCustomFields() {
        boolean retVal = true;
        if (travelCustomFieldsView != null) {
            List<TravelCustomFieldHint> invalidFields = travelCustomFieldsView.findInvalidFieldValues();
            if (invalidFields != null && invalidFields.size() > 0) {
                // Set the field reference.
                this.invalidFields = invalidFields;
                showDialog(CUSTOM_FIELDS_INVALID_VALUES);
                retVal = false;
            }
        }
        return retVal;
    }

    /**
     * Will return whether or not there any travel custom fields to be displayed.
     * 
     * @return returns whether there are any custom fields to be displayed.
     */
    protected boolean hasTravelCustomFields() {
        boolean retVal = false;
        ConcurCore concurCore = getConcurCore();
        if (concurCore != null) {
            TravelCustomFieldsConfig config = concurCore.getTravelCustomFieldsConfig();
            if (config != null) {
                if (config.hasDependencies != null && config.hasDependencies != Boolean.TRUE
                        && config.formFields != null && config.formFields.size() > 0) {
                    retVal = true;
                }
            }
        }
        return retVal;
    }

    /**
     * Gets whether the travel custom fields view should display fields designed for the start of the booking process. <br>
     * <b>NOTE:</b>&nbsp;Sub-classes of <code>TravelBaseActivity</code> should override this method to define a value. [defaults:
     * true].
     * 
     * @return returns <code>true</code> if travel custom fields for the start of the booking process should be displayed. If
     *         <code>false</code> if fields at the end of the booking process should be displayed.
     */
    protected boolean getDisplayAtStart() {
        return true;
    }

    /**
     * Will determine whether or not a set of travel custom fields should be requested from the server.
     * 
     * This method determines this by whether or not there are any travel custom fields have been downloaded that do not represent
     * an update of values.
     * 
     * @return returns whether or not a set of travel custom fields should be retrieved from the server.
     */
    protected boolean shouldRequestTravelCustomFields() {
        boolean retVal = true;
        ConcurCore concurCore = getConcurCore();
        if (concurCore != null) {
            TravelCustomFieldsConfig config = concurCore.getTravelCustomFieldsConfig();
            retVal = (config == null);
        }
        return retVal;
    }

    /**
     * Will commit any travel custom field values to the underlying object model and persistence.
     */
    protected void commitTravelCustomFields() {
        if (travelCustomFieldsView != null) {
            travelCustomFieldsView.saveFieldValues();
        }
    }

    /**
     * Will add the travel custom fields view to the existing activity view.
     * 
     * @param readOnly
     *            contains whether the fields should be read-only.
     * @param displayAtStart
     *            if <code>true</code> will result in the fields designated to be displayed at the start of the booking process
     *            will be displayed; otherwise, fields at the end of the booking process will be displayed.
     */
    protected void addTravelCustomFieldsView(boolean readOnly, boolean displayAtStart) {
        // Company has static custom fields, so display them via our nifty fragment!
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        travelCustomFieldsView = new TravelCustomFieldsView();
        Bundle args = new Bundle();
        args.putBoolean(TravelCustomFieldsView.READ_ONLY, readOnly);
        args.putBoolean(TravelCustomFieldsView.DISPLAY_AT_START, displayAtStart);
        travelCustomFieldsView.setArguments(args);
        fragmentTransaction.add(R.id.booking_custom_fields, travelCustomFieldsView, TRAVEL_CUSTOM_VIEW_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    /**
     * Will restore the reference to an travel custom views fragment.
     */
    protected void restoreTravelCustomFieldsView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment frag = fragmentManager.findFragmentByTag(TRAVEL_CUSTOM_VIEW_FRAGMENT_TAG);
        if (frag instanceof TravelCustomFieldsView) {
            travelCustomFieldsView = (TravelCustomFieldsView) frag;
        }
    }

    /**
     * Gets whether or not a travel custom fields view already exists for this activity.
     * 
     * @return returns whether a travel custom fields view already exists for this activity.
     */
    protected boolean hasTravelCustomFieldsView() {
        return (travelCustomFieldsView != null);
    }

    /**
     * Will initialize the travel custom fields view.
     */
    public void initTravelCustomFieldsView() {
        // Check for whether 'custom_fields' view group exists!
        if (findViewById(R.id.booking_custom_fields) != null) {
            ConcurCore concurCore = getConcurCore();
            TravelCustomFieldsConfig customFieldsConfig = concurCore.getTravelCustomFieldsConfig();
            if (customFieldsConfig.formFields != null && customFieldsConfig.formFields.size() > 0) {
                addTravelCustomFieldsView(false, getDisplayAtStart());
            }
        }
    }

    /**
     * Will retrieve the list of <code>TravelCustomField</code> objects containing edited values.
     * 
     * @return returns the list of <TravelCustomField> objects containing edited values.
     */
    public List<TravelCustomField> getTravelCustomFields() {
        List<TravelCustomField> retVal = null;
        if (travelCustomFieldsView != null) {
            retVal = travelCustomFieldsView.getTravelCustomFields();
        }
        return retVal;
    }

    /**
     * Will send a request to retrieve travel custom booking information.
     */
    protected void sendTravelCustomFieldsRequest() {
        ConcurService concurService = getConcurService();
        registerTravelCustomFieldsReceiver();
        travelCustomFieldsRequest = concurService.sendTravelCustomFieldsRequest();
        if (travelCustomFieldsRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".onReceive: unable to create request to retrieve travel custom field information!");
            unregisterTravelCustomFieldsReceiver();
        } else {
            // Set the request object on the receiver.
            travelCustomFieldsReceiver.setServiceRequest(travelCustomFieldsRequest);
            // Show the travel custom fields progress dialog.
            showDialog(CUSTOM_FIELDS_PROGRESS_DIALOG);
        }
    }

    /**
     * Will register an instance of <code>TravelCustomFieldsReceiver</code> with the application context and set the
     * <code>travelCustomFieldsReceiver</code> attribute.
     */
    protected void registerTravelCustomFieldsReceiver() {
        if (travelCustomFieldsReceiver == null) {
            travelCustomFieldsReceiver = new TravelCustomFieldsReceiver(this);
            if (travelCustomFieldsFilter == null) {
                travelCustomFieldsFilter = new IntentFilter(Const.ACTION_TRAVEL_CUSTOM_FIELDS);
            }
            getApplicationContext().registerReceiver(travelCustomFieldsReceiver, travelCustomFieldsFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerTravelCustomFieldsReceiver: travelCustomFieldsReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>TravelCustomFieldsReceiver</code> with the application context and set the
     * <code>travelCustomFieldsReceiver</code> to <code>null</code>.
     */
    protected void unregisterTravelCustomFieldsReceiver() {
        if (travelCustomFieldsReceiver != null) {
            getApplicationContext().unregisterReceiver(travelCustomFieldsReceiver);
            travelCustomFieldsReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterTravelCustomFieldsReceiver: travelCustomFieldsReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving the list of travel
     * custom fields.
     */
    static class TravelCustomFieldsReceiver extends
            BaseBroadcastReceiver<TravelBaseActivity, TravelCustomFieldsRequest> {

        /**
         * Constructs an instance of <code>TravelCustomFieldsReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        TravelCustomFieldsReceiver(TravelBaseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(TravelBaseActivity activity) {
            activity.travelCustomFieldsRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(CUSTOM_FIELDS_PROGRESS_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(CUSTOM_FIELDS_FAILURE_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.initTravelCustomFieldsView();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(TravelCustomFieldsRequest request) {
            activity.travelCustomFieldsRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterTravelCustomFieldsReceiver();
        }

    }

    /**
     * Will send a request to retrieve reason code information.
     */
    protected void sendReasonCodeRequest() {
        ConcurService concurService = getConcurService();
        registerReasonCodeReceiver();
        reasonCodeRequest = concurService.sendReasonCodeRequest(getUserId(), null);
        if (reasonCodeRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create request to retrieve reason code information!");
            unregisterReasonCodeReceiver();
        } else {
            // Set the request object on the receiver.
            reasonCodeReceiver.setServiceRequest(reasonCodeRequest);
            // Show the travel custom fields progress dialog.
            showDialog(REASON_CODE_PROGRESS_DIALOG);
        }
    }

    /**
     * Will register an instance of <code>ReasonCodeReceiver</code> with the application context and set the
     * <code>reasonCodeReceiver</code> attribute.
     */
    protected void registerReasonCodeReceiver() {
        if (reasonCodeReceiver == null) {
            reasonCodeReceiver = new ReasonCodeReceiver(this);
            if (reasonCodeFilter == null) {
                reasonCodeFilter = new IntentFilter(Const.ACTION_TRAVEL_REASON_CODES_UPDATED);
            }
            getApplicationContext().registerReceiver(reasonCodeReceiver, reasonCodeFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerReasonCodeReceiver: reasonCodeReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>ReasonCodeReceiver</code> with the application context and set the
     * <code>reasonCodeReceiver</code> to <code>null</code>.
     */
    protected void unregisterReasonCodeReceiver() {
        if (reasonCodeReceiver != null) {
            getApplicationContext().unregisterReceiver(reasonCodeReceiver);
            reasonCodeReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReasonCodeReceiver: reasonCodeReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving a list of reason
     * codes.
     */
    static class ReasonCodeReceiver extends BaseBroadcastReceiver<TravelBaseActivity, ReasonCodeRequest> {

        /**
         * Constructs an instance of <code>ReasonCodeReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReasonCodeReceiver(TravelBaseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(TravelBaseActivity activity) {
            activity.reasonCodeRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(REASON_CODE_PROGRESS_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(REASON_CODE_FAILURE_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.onReasonCodesReceived();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ReasonCodeRequest request) {
            activity.reasonCodeRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterReasonCodeReceiver();
        }

    }

    /**
     * Will send a request to obtain an itinerary.
     */
    protected void sendItineraryRequest(String itinLocator) {
        ConcurService concurService = getConcurService();
        registerItineraryReceiver();
        itineraryRequest = concurService.sendItineraryRequest(itinLocator);
        if (itineraryRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendItineraryRequest: unable to create itinerary request.");
            unregisterItineraryReceiver();
        } else {
            // Show the dialog.
            showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
            // Set the request object on the receiver.
            itineraryReceiver.setServiceRequest(itineraryRequest);
        }
    }

    /**
     * Will register an itinerary receiver.
     */
    protected void registerItineraryReceiver() {
        if (itineraryReceiver == null) {
            itineraryReceiver = new ItineraryReceiver(this);
            if (itineraryFilter == null) {
                itineraryFilter = new IntentFilter(Const.ACTION_TRIP_UPDATED);
            }
            getApplicationContext().registerReceiver(itineraryReceiver, itineraryFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerItineraryReceiver: itineraryReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an itinerary receiver.
     */
    protected void unregisterItineraryReceiver() {
        if (itineraryReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(itineraryReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItinerarySReceiver: illegal argument", ilaExc);
            }
            itineraryReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItineraryReceiver: itineraryReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of an itinerary list request.
     */
    protected class ItineraryReceiver extends BaseBroadcastReceiver<TravelBaseActivity, ItineraryRequest> {

        /**
         * Constructs an instance of <code>ItineraryReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryReceiver(TravelBaseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(TravelBaseActivity activity) {
            activity.itineraryRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (intent.hasExtra(Const.EXTRA_ITIN_LOCATOR)) {
                String itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
                if (itinLocator != null) {
                    Intent i = getSegmentIntent();
                    i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                    i.putExtra(Const.EXTRA_PROMPT_FOR_ADD, activity.getItineraryViewPromptForAdd());
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, isShowRatingPrompt);
                    activity.startActivity(i);
                    activity.finish();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator has invalid value!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator missing!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ItineraryRequest request) {
            activity.itineraryRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterItineraryReceiver();
        }

    }

    /**
     * get segmentList intent.
     * */
    protected Intent getSegmentIntent() {
        return new Intent(TravelBaseActivity.this, SegmentList.class);
    }

    /**
     * Will send a request to obtain an itinerary list.
     */
    protected void sendItinerarySummaryListRequest() {
        ConcurService concurService = getConcurService();
        registerItinerarySummaryListReceiver();
        itinerarySummaryListRequest = concurService.sendItinerarySummaryListRequest(false);
        if (itinerarySummaryListRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".sendItinerarySummaryListRequest: unable to create summary itinerary list request.");
            unregisterItinerarySummaryListReceiver();
        } else {
            // Set the request object on the receiver.
            itinerarySummaryListReceiver.setServiceRequest(itinerarySummaryListRequest);
            // Show the retrieving itinerary dialog even though the summary list needs to
            // be retrieved first.
            showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }
    }

    /**
     * Will register an itinerary summary list receiver.
     */
    protected void registerItinerarySummaryListReceiver() {
        if (itinerarySummaryListReceiver == null) {
            itinerarySummaryListReceiver = new ItineraryListReceiver(this);
            if (itinerarySummaryListFilter == null) {
                itinerarySummaryListFilter = new IntentFilter(Const.ACTION_SUMMARY_TRIPS_UPDATED);
            }
            getApplicationContext().registerReceiver(itinerarySummaryListReceiver, itinerarySummaryListFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerItinerarySummaryListReceiver: itinerarySummaryListReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an itinerary summary list receiver.
     */
    protected void unregisterItinerarySummaryListReceiver() {
        if (itinerarySummaryListReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(itinerarySummaryListReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItinerarySummaryListReceiver: illegal argument", ilaExc);
            }
            itinerarySummaryListReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".unregisterItinerarySummaryListReceiver: itinerarySummaryListReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of an itinerary list request.
     */
    protected class ItineraryListReceiver extends
            BaseBroadcastReceiver<TravelBaseActivity, ItinerarySummaryListRequest> {

        /**
         * Constructs an instance of <code>ItineraryListReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryListReceiver(TravelBaseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(TravelBaseActivity activity) {
            activity.itinerarySummaryListRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            onDismissListReceiverDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            return super.handleHttpError(context, intent, httpStatus);
        }

        @Override
        protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
            super.handleRequestFailure(context, intent, requestStatus);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            Trip trip = onHandleSuccessGetTrip(activity);
            onHandleSuccessItineraryList(trip, activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ItinerarySummaryListRequest request) {
            activity.itinerarySummaryListRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterItinerarySummaryListReceiver();
        }

    }

    /**
     * Successfully handle the response of trip-list and retrieve trip to handle other operations.
     * 
     * @return
     * */
    protected Trip onHandleSuccessGetTrip(TravelBaseActivity activity) {
        // Set the re-fetch flag to true as it needs to include this new reservation
        IItineraryCache itinCache = activity.getConcurCore().getItinCache();
        itinCache.setShouldRefetchSummaryList(true);
        // Look up the trip summary object based on the "itinLocator" if it is present else use "record locator"
        Trip trip = null;
        if (activity.bookingRecordLocator == null) {
            trip = itinCache.getItinerarySummaryByClientLocator(itinLocator);
        } else {
            // for safe fail over case, should not reach here
            trip = itinCache.getItinerarySummaryByBookingRecordLocator(activity.bookingRecordLocator);
        }
        return trip;
    }

    /**
     * Successfully handle itineraryList after getting trip
     * */
    protected void onHandleSuccessItineraryList(Trip trip, TravelBaseActivity activity) {
        if (trip != null) {
            activity.sendItineraryRequest(trip.itinLocator);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".handleSuccess: unable to locate trip summary object by booking record locator.");
        }
    }

    /**
     * handle dismiss dialog task. Some project like Gov. required to dismiss trip summary list dialog, to continue with other
     * service call. while some doesn't required.
     * */
    protected void onDismissListReceiverDialog(int dialogTravelRetrieveItinerary) {
        // activity.dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
    }

    protected void getPreSellOptions(String choiceId) {
        if (ConcurCore.isConnected()) {
            preSellOptionsReceiver = new BaseAsyncResultReceiver(new Handler());
            preSellOptionsReceiver.setListener(new TravelPreSellOptionsListener());
            getPresellOptions = new GetPreSellOptions(getApplicationContext(), 1, preSellOptionsReceiver, choiceId);
            getPresellOptions.execute();
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    // override in the concrete classes
    protected void updatePreSellOptions() {
    }

    /**
     * Will restore the reference to sell option views fragment.
     */
    protected void restoreSellOptionFieldsView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment frag = fragmentManager.findFragmentByTag(TRAVEL_SELL_OPTION_VIEW_FRAGMENT_TAG);
        if (frag instanceof SellOptionFieldsView) {
            sellOptionFieldsView = (SellOptionFieldsView) frag;
        }
    }

    /**
     * Gets whether or not a sell option fields view already exists for this activity.
     * 
     * @return returns whether a sell option fields view already exists for this activity.
     */
    protected boolean hasSellOptionFieldsView() {
        return (sellOptionFieldsView != null);
    }

    /**
     * Initializes the card cvv number view.
     */
    protected void initCardCVVNumberView() {
        if (preSellOption == null)
            return;

        View cvvNumberView = findViewById(R.id.card_cvv_number);
        if (!preSellOption.isCvvNumberRequired()) {
            if (cvvNumberView != null) {
                cvvNumberView.setVisibility(View.GONE);
                // View layout = cvvNumberView.findViewById(R.id.card_cvv_number_layout);
                View layout = findViewById(R.id.card_cvv_number_layout);
                if (layout != null) {
                    layout.setVisibility(View.GONE);
                }
            }
        } else {
            if (cvvNumberView != null) {
                cvvNumberView.setVisibility(View.VISIBLE);
                // View layout = cvvNumberView.findViewById(R.id.card_cvv_number_layout);
                View layout = findViewById(R.id.card_cvv_number_layout);
                if (layout != null) {
                    layout.setVisibility(View.VISIBLE);
                }
                // Set the field title.
                TextView txtView = (TextView) cvvNumberView.findViewById(R.id.card_cvv_number_field_name);
                if (txtView != null) {
                    txtView.setText(R.string.general_credit_card_cvv_number);
                    // Init the view.
                    updateCardCVVNumber();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate 'field_name' text view!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate card cvv number group!");
            }
        }
    }

    protected void updateCardCVVNumber() {
        final View cvvNumberView = findViewById(R.id.card_cvv_number);
        if (preSellOption != null && preSellOption.isCvvNumberRequired() && cvvNumberView != null) {
            // set the field value
            TextView fieldValueTxtView = (TextView) cvvNumberView.findViewById(R.id.card_cvv_number_field_value);
            fieldValueTxtView.setEnabled(true);
            if (cvvNumber > 0) {
                fieldValueTxtView.setText(String.valueOf(cvvNumber));
            } else {
                fieldValueTxtView.setText("");
            }

            fieldValueTxtView.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    TextView view = (TextView) cvvNumberView.findViewById(R.id.card_cvv_number_field_note);
                    if (s != null && view != null) {
                        String curValue = s.toString().trim();
                        if (curValue.length() == 0) {
                            cvvNumber = 0;
                        } else {
                            Pattern pat = Pattern.compile("[0-9]+");
                            Matcher match = pat.matcher(curValue);
                            if (match.matches()) {
                                cvvNumber = Integer.parseInt(curValue);
                                // Hide notification.
                                ViewUtil.setVisibility(view, R.id.card_cvv_number_field_note, View.GONE);
                            } else {
                                // Show notification.
                                String txt = Format.localizeText(getApplicationContext(),
                                        R.string.general_field_value_invalid, curValue);
                                view.setText(txt);
                                view.setTextAppearance(getApplicationContext(), R.style.FormFieldNoteRedText);
                                ViewUtil.setVisibility(view, R.id.card_cvv_number_field_note, View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

            });

        }
    }

    /**
     * Cancellation Policy....need to be override by the concrete classes
     */
    protected void initCancellationPolicyView() {
    }

    /**
     * Travel Points header text
     * 
     * @param priceToBeat
     * @param priceToBeatResId
     * @param travelPointsInBank
     * @param travelPointsInBankResId
     */
    // Travel Points header text for Air
    // invoked by Air search summary, filter and detail derived classes
    protected void initAirTravelPointsHeader(String formattedPriceToBeat, int priceToBeatResId,
            final String travelPointsInBank, int travelPointsInBankResId) {

        TextView travelPointsHdrView = LayoutUtil.getTravelPointsHeader(this, formattedPriceToBeat, priceToBeatResId,
                travelPointsInBank, travelPointsInBankResId);

        if (travelPointsHdrView != null) {
            travelPointsHdrView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Intent travelPointsExpActivity = new Intent(TravelBaseActivity.this, TravelPointsExplanation.class);
                    travelPointsExpActivity.putExtra(EXTRA_TITLE_RESOURCE_ID_KEY,
                            R.string.segment_travel_points_price_to_beat);
                    TravelPointsConfig travelPointsConfig = ((ConcurCore) getApplication()).getUserConfig().travelPointsConfig;
                    if (travelPointsConfig != null && travelPointsConfig.isAirTravelPointsEnabled()) {
                        travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                R.string.travel_points_p2b_explanation_desc_with_tp);
                    } else {
                        travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                R.string.travel_points_p2b_explanation_desc);
                    }

                    startActivity(travelPointsExpActivity);
                    // GA & Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_TYPE, Flurry.EVENT_NAME_AIR);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                            Flurry.EVENT_NAME_VIEWED_PRICE_TO_BEAT_RANGE, params);
                }
            });
        }
    }

    /**
     * Travel Points header text
     * 
     * @param priceToBeat
     * @param priceToBeatResId
     * @param travelPointsInBank
     * @param travelPointsInBankResId
     */
    // Travel Points header text in the Select Hotel screen for Hotel
    // invoked by Hotel search summary derived classes
    protected void initHotelTravelPointsHeader() {

        HotelSearchReply results = ((ConcurCore) getApplication()).getHotelSearchResults();
        ArrayList<HotelBenchmark> hotelBenchmarks = results.hotelBenchmarks;
        if (hotelBenchmarks != null) {
            String formattedMinBenchmarkPrice = null;
            String formattedMaxBenchmarkPrice = null;

            HotelBenchmark hotelBenchmarkWithMinPrice = null;
            HotelBenchmark hotelBenchmarkWithMaxPrice = null;

            for (HotelBenchmark hotelBenchmark : hotelBenchmarks) {
                if (hotelBenchmark.getPrice() != null && hotelBenchmark.getPrice() > 0) {
                    if (hotelBenchmarkWithMinPrice == null
                            || hotelBenchmarkWithMinPrice.getPrice() > hotelBenchmark.getPrice()) {
                        hotelBenchmarkWithMinPrice = hotelBenchmark;
                    }
                    if (hotelBenchmarkWithMaxPrice == null
                            || hotelBenchmarkWithMaxPrice.getPrice() < hotelBenchmark.getPrice()) {
                        hotelBenchmarkWithMaxPrice = hotelBenchmark;
                    }
                }
            }

            Locale locale = this.getResources().getConfiguration().locale;

            if (hotelBenchmarkWithMinPrice != null && hotelBenchmarkWithMinPrice.getPrice() != null) {
                formattedMinBenchmarkPrice = FormatUtil.formatAmount(hotelBenchmarkWithMinPrice.getPrice(), locale,
                        hotelBenchmarkWithMinPrice.getCrnCode(), true, true);
            }
            if (hotelBenchmarkWithMaxPrice != null && hotelBenchmarkWithMaxPrice.getPrice() != null) {
                formattedMaxBenchmarkPrice = FormatUtil.formatAmount(hotelBenchmarkWithMaxPrice.getPrice(), locale,
                        hotelBenchmarkWithMaxPrice.getCrnCode(), true, true);
            }

            final boolean showPriceToBeatList = (formattedMinBenchmarkPrice != null && formattedMaxBenchmarkPrice != null);

            // MOB-17696
            final boolean noPriceRange = (formattedMinBenchmarkPrice != null && formattedMinBenchmarkPrice
                    .equals(formattedMaxBenchmarkPrice));
            if (noPriceRange) {
                // setting one of the prices to null will show a different text
                formattedMaxBenchmarkPrice = null;
            }

            final String formattedMinBenchmarkPriceToSend = formattedMinBenchmarkPrice;
            final String formattedMaxBenchmarkPricetoSend = formattedMaxBenchmarkPrice;

            String travelPointsInBank = null;

            if (results.travelPointsBank != null && results.travelPointsBank.getPointsAvailableToSpend() != null) {
                travelPointsInBank = Integer.toString(results.travelPointsBank.getPointsAvailableToSpend());
            }

            TextView travelPointsHdrView = LayoutUtil.getTravelPointsHeader(this, formattedMinBenchmarkPrice,
                    formattedMaxBenchmarkPrice, travelPointsInBank);

            if (travelPointsHdrView != null) {
                travelPointsHdrView.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        Intent travelPointsExpActivity = new Intent(TravelBaseActivity.this,
                                TravelPointsExplanation.class);
                        travelPointsExpActivity.putExtra(EXTRA_TITLE_RESOURCE_ID_KEY,
                                R.string.segment_travel_points_price_to_beat);

                        TravelPointsConfig travelPointsConfig = ((ConcurCore) getApplication()).getUserConfig().travelPointsConfig;
                        if (travelPointsConfig != null && travelPointsConfig.isHotelTravelPointsEnabled()) {
                            if (noPriceRange) {
                                travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                        R.string.travel_points_p2b_explanation_desc_in_summary_with_tp_no_price_range);
                            } else {
                                travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                        R.string.travel_points_p2b_explanation_desc_in_summary_with_tp);
                            }
                        } else {
                            if (noPriceRange) {
                                travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                        R.string.travel_points_p2b_explanation_desc_in_summary_no_price_range);
                            } else {
                                travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                        R.string.travel_points_p2b_explanation_desc_in_summary);
                            }
                        }

                        if (showPriceToBeatList) {
                            travelPointsExpActivity.putExtra(EXTRA_SHOW_PRICE_TO_BEAT_KEY, true);
                            travelPointsExpActivity.putExtra(EXTRA_FORMATTED_MIN_PRICE_TO_BEAT_KEY,
                                    formattedMinBenchmarkPriceToSend);
                            travelPointsExpActivity.putExtra(EXTRA_FORMATTED_MAX_PRICE_TO_BEAT_KEY,
                                    formattedMaxBenchmarkPricetoSend);
                        }
                        startActivity(travelPointsExpActivity);

                        // set the on header click event in the app object for retrieving in the intent
                        ConcurCore core = (ConcurCore) ConcurCore.getContext();
                        core.setViewedPriceToBeatList(true);
                        core.setViewedPriceToBeatListLastRetrieved(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

                        Map<String, String> travelPointsParams = new HashMap<String, String>();
                        travelPointsParams.put(Flurry.PARAM_NAME_TYPE, Flurry.EVENT_NAME_HOTEL);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                                Flurry.EVENT_NAME_VIEWED_PRICE_TO_BEAT_RANGE, travelPointsParams);

                    }
                });
            }
        }
    }

    // Travel Points header text in the Select Room screen for Hotel
    // invoked by derived class - HotelSearchRooms
    protected void initHotelSelectRoomTravelPointsHeader(String formattedBenchmarkPrice, String travelPointsInBank) {
        final TravelPointsConfig travelPointsConfig = ((ConcurCore) getApplication()).getUserConfig().travelPointsConfig;

        TextView travelPointsHdrView = LayoutUtil.getTravelPointsHeader(this, formattedBenchmarkPrice,
                R.string.travel_points_air_booking_workflow_p2b_header, travelPointsInBank,
                R.string.travel_points_air_booking_workflow_points_header);

        if (travelPointsHdrView != null) {
            travelPointsHdrView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Intent travelPointsExpActivity = new Intent(TravelBaseActivity.this, TravelPointsExplanation.class);
                    travelPointsExpActivity.putExtra(EXTRA_TITLE_RESOURCE_ID_KEY,
                            R.string.segment_travel_points_price_to_beat);

                    if (travelPointsConfig != null && travelPointsConfig.isHotelTravelPointsEnabled()) {
                        travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                R.string.travel_points_p2b_explanation_desc_with_tp);
                    } else {
                        travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                R.string.travel_points_p2b_explanation_desc);
                    }

                    travelPointsExpActivity.putExtra(EXTRA_SHOW_PRICE_TO_BEAT_KEY, false);

                    startActivity(travelPointsExpActivity);
                }
            });
        }
    }

    // Violations view with Travel Points functionality
    protected void initViolationsViewForTravelPoints() {
        if (useTravelPoints) {

            // nullify the previous values if any
            reasonCode = null;
            justificationText = null;

            // hide violations views
            hideViolationsViews();

            // get the Travel Points to use for this booking
            LayoutUtil.updateViolationReasonChoiceView(TravelBaseActivity.this, getTravelPointsToUse());

            // disable the on click event
            View reasonsCodeView = TravelBaseActivity.this.findViewById(R.id.violation_reason);
            reasonsCodeView.setOnClickListener(null);
        } else {

            // if the violations views not visible, show them
            View messagesView = TravelBaseActivity.this.findViewById(R.id.violation);
            boolean violationsViewGroupVisible = messagesView.isShown();

            // show violations views
            showViolationsViews();

            if (violationsViewGroupVisible) {
                // just update the reason code selected, justification text and the violation messages
                LayoutUtil.updateViolationReasonChoiceView(TravelBaseActivity.this, reasonCode);
                LayoutUtil.updateViolationJustificationView(TravelBaseActivity.this, justificationText,
                        getTravelViolationJustificationCustomText());
            } else {
                // Construct the violation message click listener.
                OnClickListener violationClickListener = new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (v.getTag() instanceof Violation) {
                            Violation violation = (Violation) v.getTag();
                            selectedViolationEnforcementLevel = violation.enforcementLevel;
                            selectedViolationEnforcementText = violation.message;
                            showDialog(Const.DIALOG_TRAVEL_VIOLATION_VIEW_MESSAGE);
                        }
                    }
                };

                // add the views
                LayoutUtil.layoutViolations(this, getViolations(), reasonCodeChoices, reasonCode,
                        violationClickListener, justificationText, getTravelViolationJustificationCustomText());
            }
        }

        // show the Manage Violations" UI element and set the on click listener
        showManageViolationsView();

        initTravelPointsInPrice();

    }

    private void showViolationsViews() {
        View justificationView = findViewById(R.id.violation_justification);
        justificationView.setVisibility(View.VISIBLE);

        View messagesView = findViewById(R.id.violation);
        messagesView.setVisibility(View.VISIBLE);

        View manageViolationsViewSeparator = findViewById(R.id.manage_violations_group_sep);
        if (manageViolationsViewSeparator != null) {
            manageViolationsViewSeparator.setVisibility(View.VISIBLE);
        }
    }

    private void hideViolationsViews() {
        View justificationView = findViewById(R.id.violation_justification);
        justificationView.setVisibility(View.GONE);

        View messagesView = findViewById(R.id.violation);
        messagesView.setVisibility(View.GONE);

        View manageViolationsViewSeparator = findViewById(R.id.manage_violations_group_sep);
        if (manageViolationsViewSeparator != null) {
            manageViolationsViewSeparator.setVisibility(View.GONE);
        }
    }

    private void showManageViolationsView() {
        // flag to check for the display of travel points in the Price view
        showingManageViolations = true;
        View manageViolationsView = LayoutUtil.showManageViolationsView(TravelBaseActivity.this);
        manageViolationsView.setOnClickListener(new ManageViolationsOnClickListener());
    }

    protected void initTravelPointsInPrice() {
        boolean showTravelPoints = false;

        if (canRedeemTravelPointsAgainstViolations()) {
            // if not showing manage violations means that the first time display of the reserve screen
            // if manage violations are showing that means the user had already been to the Use Travel Points Choice screen
            if (!showingManageViolations || (showingManageViolations && useTravelPoints)) {
                showTravelPoints = true;
            }
        }

        if (showTravelPoints) {
            showTravelPointsInPrice();
        } else {
            hideTravelPointsInPrice();
        }
    }

    // derived classes has to provide implementation - AirFlightDetail and HotelReserveRoom
    protected void showTravelPointsInPrice() {
    }

    // derived classes has to provide implementation - AirFlightDetail and HotelReserveRoom
    protected void hideTravelPointsInPrice() {
    }

    /**
     * Determines if Travel Points can be used against the Violations approval
     * 
     * @return
     */
    protected boolean canRedeemTravelPointsAgainstViolations() {
        return false;
    }

    /**
     * Derived classes will provide the value.
     * 
     * @return
     */
    protected int getTravelPointsToUse() {
        return travelPoints;
    }

    /**
     * Derived classes will provide the value.
     * 
     * @return
     */
    protected String getTravelPointsInBank() {
        return travelPointsInBank;
    }

    protected void logEvents(int travelPoints, String type) {

        Map<String, String> travelPointsParams = new HashMap<String, String>();

        String companyName = ViewUtil.getUserCompanyName(this);
        String tpts = Integer.toString(travelPoints);

        travelPointsParams.put(Flurry.PARAM_NAME_TRAVEL_POINTS_IN_BANK, getTravelPointsInBank());
        travelPointsParams.put(Flurry.PARAM_NAME_TRAVELLER_COMPANY, companyName);

        if (tpts.contains("-")) {
            travelPointsParams.put(Flurry.PARAM_NAME_SELECTED_OPTION, Flurry.PARAM_VALUE_NO);
            if (useTravelPoints) {
                travelPointsParams.put(Flurry.PARAM_NAME_USE_TRAVEL_POINTS, Flurry.PARAM_VALUE_YES);
                travelPointsParams.put(Flurry.PARAM_NAME_POINTS_USED, tpts);
            } else {
                travelPointsParams.put(Flurry.PARAM_NAME_USE_TRAVEL_POINTS, Flurry.PARAM_VALUE_NO);
            }

        } else if (travelPoints > 0) {
            travelPointsParams.put(Flurry.PARAM_NAME_SELECTED_OPTION, Flurry.PARAM_VALUE_YES);
            travelPointsParams.put(Flurry.PARAM_NAME_POINTS_EARNED, tpts);
        } else {
            travelPointsParams.put(Flurry.PARAM_NAME_SELECTED_OPTION, Flurry.PARAM_VALUE_NA);
        }
        String eventName = type == Flurry.PARAM_VALUE_AIR ? Flurry.EVENT_NAME_AIR_RESERVE
                : Flurry.EVENT_NAME_HOTEL_RESERVE;

        EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT, eventName, travelPointsParams);

    }

    public class TravelPreSellOptionsListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestSuccess in RailPreSellOptionsListener...");
            if (resultData.containsKey(GetPreSellOptions.PRE_SELL_OPTIONS)) {
                dismissDialog(RETRIEVE_PRE_SELL_OPTIONS_DIALOG);
                preSellOption = (PreSellOption) resultData.getSerializable(GetPreSellOptions.PRE_SELL_OPTIONS);
                updatePreSellOptions();
            } else {
                Log.e(Const.LOG_TAG, " onRequestSuccess in RailPreSellOptionsListener... do not contain preSellOptions");
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestFail in RailPreSellOptionsListener...");
            dismissDialog(RETRIEVE_PRE_SELL_OPTIONS_DIALOG);
            showDialog(RETRIEVE_PRE_SELL_OPTIONS_FAILED_DIALOG);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestCancel in RailPreSellOptionsListener...");
            // dialog RETRIEVE_PRE_SELL_OPTIONS_DIALOG is already removed in the onCancel event of this dialog
        }

        @Override
        public void cleanup() {
            preSellOptionsReceiver = null;
        }
    }

    // On click listener for Manage Violations UI element
    class ManageViolationsOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String violationType = null;
            if (getBookingType().toString().equalsIgnoreCase("Air")) {
                violationType = "A";
            } else if (getBookingType().toString().equalsIgnoreCase("Hotel")) {
                violationType = "H";
            }

            Intent travelViolationsActivity = new Intent(TravelBaseActivity.this, TravelViolationsApprovalChoice.class);

            travelViolationsActivity.putExtra(EXTRA_VIOLATION_TYPE_KEY, violationType);
            travelViolationsActivity.putExtra(EXTRA_TRAVEL_POINTS_TO_USE_KEY, getTravelPointsToUse());
            travelViolationsActivity.putExtra(EXTRA_TRAVEL_POINTS_IN_BANK_KEY, getTravelPointsInBank());

            travelViolationsActivity.putExtra(EXTRA_USE_TRAVEL_POINTS_SELECTED_KEY, useTravelPoints);

            // pass on the reason code and justification text if available to the activity
            if (reasonCode != null) {
                travelViolationsActivity.putExtra(EXTRA_REASON_CODE_SELECTED_KEY, reasonCode);
            }
            if (justificationText != null) {
                travelViolationsActivity.putExtra(EXTRA_JUSTIFICATION_TEXT_KEY, justificationText);
            }

            // set the rule violations in the app object for retrieving in the intent
            ConcurCore core = (ConcurCore) ConcurCore.getContext();
            core.setTravelPolicyViolations(getViolations());
            core.setTravelPolicyViolationsLastRetrieved(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            TravelBaseActivity.this.startActivityForResult(travelViolationsActivity,
                    Const.REQUEST_CODE_USE_TRAVEL_POINTS);
        }
    }

    // get the custom text for the travel violation justification
    public String getTravelViolationJustificationCustomText() {
        String text = null;

        CustomTravelText customTravelText = ((ConcurCore) getApplication()).getUserConfig().customTravelText;
        if (customTravelText != null) {
            if (getBookingType().toString().equalsIgnoreCase("Air")) {
                text = customTravelText.airRulesViolationText;
            } else if (getBookingType().toString().equalsIgnoreCase("Hotel")) {
                text = customTravelText.hotelRulesViolationText;
            } else if (getBookingType().toString().equalsIgnoreCase("Car")) {
                text = customTravelText.carRulesViolationText;
            }
        }

        return text;
    }

}

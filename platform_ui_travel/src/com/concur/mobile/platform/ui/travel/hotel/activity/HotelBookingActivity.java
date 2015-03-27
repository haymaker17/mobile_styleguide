package com.concur.mobile.platform.ui.travel.hotel.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.*;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.common.formfield.FormField;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.travel.booking.CreditCard;
import com.concur.mobile.platform.travel.search.hotel.*;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactoryV1;
import com.concur.mobile.platform.ui.common.fragment.RetainerFragmentV1;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.activity.TravelBaseActivity;
import com.concur.mobile.platform.ui.travel.fragment.TravelCustomFieldsFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.CustomDialogFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.CustomDialogFragment.CustomDialogFragmentCallbackListener;
import com.concur.mobile.platform.ui.travel.hotel.fragment.SpinnerDialogFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.SpinnerDialogFragment.SpinnerDialogFragmentCallbackListener;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomField;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomFieldsConfig;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomFieldsLoader;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomFieldsUpdateLoader;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.util.ParallaxScollView;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author RatanK
 */
public class HotelBookingActivity extends TravelBaseActivity
        implements SpinnerDialogFragmentCallbackListener, CustomDialogFragmentCallbackListener,
        TravelCustomFieldsFragment.TravelCustomFieldsFragmentCallBackListener {

    protected static final String CLS_TAG = HotelBookingActivity.class.getSimpleName();
    // custom fields loader callback implementation
    private LoaderManager.LoaderCallbacks<TravelCustomFieldsConfig> customFieldsLoaderListener = new LoaderManager.LoaderCallbacks<TravelCustomFieldsConfig>() {

        @Override
        public Loader<TravelCustomFieldsConfig> onCreateLoader(int id, Bundle bundle) {
            PlatformAsyncTaskLoader<TravelCustomFieldsConfig> asyncLoader = null;
            if (update) {
                showProgressBar(R.string.dlg_travel_retrieve_custom_fields_update_progress_message);
                asyncLoader = new TravelCustomFieldsUpdateLoader(getApplicationContext(), formFields);
            } else {
                showProgressBar(R.string.dlg_travel_retrieve_custom_fields_progress_message);
                asyncLoader = new TravelCustomFieldsLoader(getApplicationContext());
            }
            return asyncLoader;
        }

        @Override
        public void onLoadFinished(Loader<TravelCustomFieldsConfig> loader,
                TravelCustomFieldsConfig travelCustomFieldsConfig) {

            hideProgressBar();

            if (travelCustomFieldsConfig == null) {
                // no custom fields
            } else if (travelCustomFieldsConfig != null) {

                if (travelCustomFieldsConfig.errorOccuredWhileRetrieving) {
                    showToast("Could not retrieve custom fields.");
                } else {

                    travelCustomFieldsConfig = travelCustomFieldsConfig;
                    formFields = travelCustomFieldsConfig.formFields;
                    // to overcome the 'cannot perform this action inside of the onLoadFinished'
                    final int WHAT = 1;
                    Handler handler = new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == WHAT) {
                                initTravelCustomFieldsView();
                            }
                        }
                    };
                    handler.sendEmptyMessage(WHAT);
                }
            }

            if (travelCustomFieldsConfig != null && travelCustomFieldsConfig.formFields != null) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".onLoadFinished ********************* : travelCustomFieldsConfig size : " + (
                                travelCustomFieldsConfig != null ?
                                        travelCustomFieldsConfig.formFields.size() :
                                        null));
            }

        }

        @Override
        public void onLoaderReset(Loader<TravelCustomFieldsConfig> data) {
            Log.d(Const.LOG_TAG, " ***** loader reset *****  ");
        }
    };
    protected static final String RETAINER_TAG = "retainer.fragment";
    private static final String GET_HOTEL_BOOKING_RECEIVER = "hotel.booking.receiver";
    private static final String VIOLATION_REASONS_SPINNER_FRAGMENT = "violations.reasons.spinner.fragment";
    private static final String CREDIT_CARDS_SPINNER_FRAGMENT = "violations.reasons.spineer.fragment";
    private static final int HOTEL_BOOKING_ID = 1;
    private static final String DIALOG_FRAGMENT_ID = "HotelBookingConfirm";
    private static final int PRE_SELL_OPTIONS_LOADER_ID = 1;
    private static final int CUSTOM_FIELDS_LOADER_ID = 2;
    // The one RetainerFragment used to hold objects between activity recreates
    public RetainerFragmentV1 retainer;
    // Contains the currently selected card.
    protected SpinnerItem curCardChoice;
    // Contains the list of cards.
    protected SpinnerItem[] cardChoices;
    protected SpinnerItem curViolationReason;
    private LoaderManager lm;
    private String roomDesc;
    private Double amount;
    private String currCode;
    private String sellOptionsURL;
    // pre sell options loader callback implementation
    private LoaderManager.LoaderCallbacks<HotelPreSellOption> preSellOptionsLoaderListener = new LoaderManager.LoaderCallbacks<HotelPreSellOption>() {

        @Override
        public Loader<HotelPreSellOption> onCreateLoader(int id, Bundle bundle) {

            // request initial search
            Log.d(Const.LOG_TAG, " ***** creating preselloption loader *****  ");

            PlatformAsyncTaskLoader<HotelPreSellOption> hotelPreSellOptionAsyncTaskLoader = new HotelPreSellOptionLoader(
                    getApplicationContext(), sellOptionsURL);

            return hotelPreSellOptionAsyncTaskLoader;
        }

        @Override
        public void onLoadFinished(Loader<HotelPreSellOption> loader, HotelPreSellOption hotelPreSellOption) {
            setPreSellOptions(hotelPreSellOption);

        }

        @Override
        public void onLoaderReset(Loader<HotelPreSellOption> loader) {
            // nothing to handle here
        }
    };
    private String[] cancellationPolicyStatements;
    private boolean progressbarVisible;
    private Button reserveButton;
    private HotelRate hotelRate;
    private HotelPreSellOption preSellOption;
    private String location;
    private String durationOfStayForDisplay;
    private int numOfNights;
    private String headerImageURL;
    private String hotelName;
    private BaseAsyncResultReceiver hotelBookingReceiver;
    private SpinnerItem[] violationReasonChoices;
    private ArrayList<String[]> violationReasons;
    private HotelBookingAsyncRequestTask hotelBookingAsyncRequestTask;
    private String currViolationId;
    private boolean ruleViolationExplanationRequired;
    private String currentTripId;
    private List<HotelViolation> violations;
    /**
     * BroadcastReceiver used to detect network connectivity and update the UI accordingly.
     */
    private BroadcastReceiver connectivityReceiver;
    private IntentFilter connectivityFilter;
    private boolean connectivityReceiverRegistered;
    private boolean connected;
    private int msgResourse;
    private boolean isExpanded;
    private ParallaxScollView mListView;
    private TextView roomDescView;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && mListView != null) {

            mListView.setViewsBounds(ParallaxScollView.ZOOM_X2);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetainerFragment();
        // Restore any receivers.
        restoreReceivers();
        setContentView(R.layout.hotel_booking);

        showProgressBar(R.string.hotel_sell_options_retrieving);

        Intent intent = getIntent();

        hotelRate = (HotelRate) intent.getSerializableExtra("roomSelected");
        location = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
        durationOfStayForDisplay = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY);
        numOfNights = intent.getIntExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, 0);
        headerImageURL = intent.getStringExtra("headerImageURL");
        hotelName = intent.getStringExtra("hotelName");
        violationReasons = (ArrayList<String[]>) intent.getSerializableExtra("violationReasons");
        ruleViolationExplanationRequired = intent.getBooleanExtra("ruleViolationExplanationRequired", false);
        currentTripId = intent.getStringExtra("currentTripId");
        violations = (List<HotelViolation>) intent.getSerializableExtra("violations");

        if (intent.hasExtra("travelCustomFieldsConfig")) {
            travelCustomFieldsConfig = (TravelCustomFieldsConfig) intent
                    .getSerializableExtra("travelCustomFieldsConfig");
            if (travelCustomFieldsConfig != null && travelCustomFieldsConfig.formFields != null
                    && travelCustomFieldsConfig.formFields.size() > 0) {
                formFields = travelCustomFieldsConfig.formFields;
            }
        }

        if (hotelRate != null) {

            sellOptionsURL = hotelRate.sellOptions.href;
            roomDesc = hotelRate.description;
            amount = hotelRate.amount;
            currCode = hotelRate.currency;

        }

        if (preSellOption == null) {
            // Initialize the loader.
            lm = getLoaderManager();
            lm.initLoader(PRE_SELL_OPTIONS_LOADER_ID, null, preSellOptionsLoaderListener);
        }

        // initialize the view
        initView();

        connectivityReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                updateUIForConnectivity(intent.getAction());
            }
        };
        connectivityFilter = new IntentFilter(
                com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);
        connectivityFilter
                .addAction(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE);
        // Register the data connectivity receiver.
        Intent stickyIntent = registerReceiver(connectivityReceiver, connectivityFilter);
        if (stickyIntent != null) {
            updateUIForConnectivity(stickyIntent.getAction());
        } else {
            updateUIForConnectivity(null);
        }
        connectivityReceiverRegistered = true;

    }

    private void updateUIForConnectivity(String action) {
        if (action != null) {
            if (action.equalsIgnoreCase(
                    com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE)) {
                connected = true;
            } else if (action.equalsIgnoreCase(
                    com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE)) {
                connected = false;
            }

        }

    }

    private void restoreReceivers() {
        if (retainer.contains(GET_HOTEL_BOOKING_RECEIVER)) {
            hotelBookingReceiver = (BaseAsyncResultReceiver) retainer.get(GET_HOTEL_BOOKING_RECEIVER);
            hotelBookingReceiver.setListener(new HotelBookingReplyListener());
        }

    }

    protected void initRetainerFragment() {
        FragmentManager fm = getFragmentManager();

        retainer = (RetainerFragmentV1) fm.findFragmentByTag(RETAINER_TAG);
        if (retainer == null) {
            retainer = new RetainerFragmentV1();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(retainer, RETAINER_TAG);
            ft.commit();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();

    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

    }

    private void unregisterReceiver() {
        if (hotelBookingReceiver != null) {
            hotelBookingReceiver.setListener(null);
            retainer.put(GET_HOTEL_BOOKING_RECEIVER, hotelBookingReceiver);
        }

    }

    private void initView() {

        // header title
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(hotelName);

        // show header image only if available
        if (headerImageURL != null) {
            URI uri = URI.create(headerImageURL);
            ImageCache imgCache = ImageCache.getInstance(this);
            Bitmap bitmap = imgCache.getBitmap(uri, null);

            if (bitmap != null) {
                mListView = (ParallaxScollView) findViewById(R.id.hotel_room_image);
                View header = LayoutInflater.from(this).inflate(R.layout.hotel_image_header, null);
                ImageView imageview = (ImageView) header.findViewById(R.id.travelCityscape);
                imageview.setImageBitmap(bitmap);

                mListView.setParallaxImageView(imageview);
                mListView.addHeaderView(header);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_expandable_list_item_1, new String[] { });
                mListView.setAdapter(adapter);
            }
        }
        // room desc
        updateRoomDescription();

        // dates
        TextView txtView = (TextView) findViewById(R.id.date_span);
        txtView.setText(durationOfStayForDisplay);

        // number of nights
        txtView = (TextView) findViewById(R.id.hotel_room_night);
        txtView.setText(
                Format.localizeText(this.getApplicationContext(), R.string.hotel_reserve_num_of_nights, numOfNights));

        // amount
        txtView = (TextView) findViewById(R.id.hotel_room_rate);
        txtView.setText(FormatUtil
                .formatAmountWithNoDecimals(amount, this.getResources().getConfiguration().locale, currCode, true,
                        false));

        // rate info on click event
        ImageView rateInfoImg = (ImageView) findViewById(R.id.checkout_icon_price_info);
        rateInfoImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Rate breakdown here...not implemented", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // cancellation policy on click event
        findViewById(R.id.hotel_policy).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showCancellationPolicy();
            }
        });

        // credit cards
        initCardChoiceView();

        // violations
        initViolations();

        // custom fields
        initTravelCustomFieldsView();

        // reserve UI
        reserveButton = (Button) findViewById(R.id.footer_button);
        reserveButton.setEnabled(false);
        reserveButton.setText(R.string.hotel_reserve_this_room);
        if (hotelRate.guaranteeSurcharge != null && hotelRate.guaranteeSurcharge.equals("DepositRequired")) {
            msgResourse = R.string.dlg_travel_hotel_deposit_confirm_message;
        } else {
            msgResourse = R.string.hotel_confirm_reserve_msg;
        }
        reserveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                CustomDialogFragment dialog = new CustomDialogFragment();

                dialog.setTitle(R.string.hotel_confirm_reserve_title);
                dialog.setMessage(R.string.hotel_confirm_reserve_msg);
                dialog.setPositiveButtonText(R.string.hotel_confirm_reserve_ok);
                dialog.setNegativeButtonText(R.string.hotel_confirm_reserve_cancel);

                dialog.show(getFragmentManager(), DIALOG_FRAGMENT_ID);

            }
        });
    }

    private void updateRoomDescription() {
        roomDescView = (TextView) findViewById(R.id.hotel_room_desc);
        roomDescView.setText(roomDesc);

        roomDescView.post(new Runnable() {

            @Override
            public void run() {
                int lineCount = roomDescView.getLineCount();
                // animate txtView more than 3 lines
                if (lineCount > 3) {
                    roomDescView.setMaxLines(3);
                    roomDescView.setEllipsize(TextUtils.TruncateAt.END);
                    roomDescView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.icon_expand_more);

                    roomDescView.setOnClickListener(new OnClickListener() {

                        ObjectAnimator animation = null;

                        @Override
                        public void onClick(View v) {
                            if (isExpanded) {
                                animation = ObjectAnimator.ofInt(v, "maxLines", 3);
                                roomDescView
                                        .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.icon_expand_more);

                            } else {
                                animation = ObjectAnimator.ofInt(v, "maxLines", 1000);
                                roomDescView
                                        .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.icon_expand_less);
                            }
                            isExpanded = !isExpanded;
                            animation.setDuration(10);
                            animation.start();

                        }
                    });
                }

            }
        });

    }

    private void initViolations() {
        if (hotelRate != null && hotelRate.violationValueIds != null && hotelRate.violationValueIds.length > 0) {

            // inflate the violations view stub
            View violationsView = ((ViewStub) findViewById(R.id.violation_view)).inflate();

            // convert the int[] to String[]
            String[] valueIds = new String[hotelRate.violationValueIds.length];
            for (int i = 0; i < hotelRate.violationValueIds.length; i++) {
                valueIds[i] = Integer.toString(hotelRate.violationValueIds[i]);
            }

            // Get the violations from the database and initialize the view
            // TravelUtilHotel.getHotelViolations(getApplicationContext(), valueIds,
            // (int) search_id);
            Log.d(Const.LOG_TAG, CLS_TAG + ".initViolations: violations from db : " + violations);
            if (violations != null && violations.size() > 0) {

                TableLayout tableLayout = (TableLayout) violationsView.findViewById(R.id.violation_message_table);

                TableRow firstRowView = (TableRow) tableLayout.findViewById(R.id.violation_message_table_row);
                LayoutParams trViewLayoutParams = firstRowView.getLayoutParams();

                TextView txtView = (TextView) tableLayout.findViewById(R.id.travel_violation_message);
                LayoutParams txtViewLayoutParams = txtView.getLayoutParams();

                ImageView imgView = (ImageView) tableLayout.findViewById(R.id.travel_violation_icon);
                LayoutParams imgViewLayoutParams = imgView.getLayoutParams();

                // sort the violations in descending enforcement level i.e. max enforcement level will be at top
                Collections.sort(violations, new HotelViolationComparator());

                boolean firstRow = true;
                for (HotelViolation hotelViolation : violations) {

                    if (firstRow) {
                        txtView.setText(hotelViolation.message);
                        if (hotelViolation.enforcementLevel.equalsIgnoreCase("RequiresApproval")) {
                            imgView.setImageResource(R.drawable.icon_warning_red);
                        } else {
                            imgView.setImageResource(R.drawable.icon_warning_yellow);
                        }
                        currViolationId = hotelViolation.violationValueId;
                        firstRow = false;
                    } else {
                        // create a new table row and add to the table layout
                        TableRow trView = new TableRow(this);
                        trView.setLayoutParams(trViewLayoutParams);

                        TextView newTxtView = new TextView(this);
                        newTxtView.setLayoutParams(txtViewLayoutParams);
                        newTxtView.setPadding(txtView.getPaddingLeft(), txtView.getPaddingTop(),
                                txtView.getPaddingRight(), txtView.getPaddingBottom());
                        newTxtView.setText(hotelViolation.message);

                        ImageView newImgView = new ImageView(this);
                        newImgView.setLayoutParams(imgViewLayoutParams);
                        if (hotelViolation.enforcementLevel.equalsIgnoreCase("RequiresApproval")) {
                            newImgView.setImageResource(R.drawable.icon_warning_red);
                        } else {
                            newImgView.setImageResource(R.drawable.icon_warning_yellow);
                        }

                        // now add the image and text views to the table row
                        trView.addView(newImgView);
                        trView.addView(newTxtView);

                        tableLayout.addView(trView);

                    }
                }

                // add the max enforcement level icon to the first row
                if (hotelRate.maxEnforcementLevel >= 30) {
                    ((ImageView) violationsView.findViewById(R.id.hotel_room_max_violation_icon))
                            .setImageResource(R.drawable.icon_status_red);
                    // reserveButton.setEnabled(false);
                } else {
                    ((ImageView) violationsView.findViewById(R.id.hotel_room_max_violation_icon))
                            .setImageResource(R.drawable.icon_status_yellow);
                }

            }

            if (violationReasons != null && violationReasons.size() > 0) {
                // Construct spinner objects which will be used to populate a dialog.
                violationReasonChoices = new SpinnerItem[violationReasons.size()];
                for (int i = 0; i < violationReasons.size(); ++i) {
                    violationReasonChoices[i] = new SpinnerItem(violationReasons.get(i)[0], violationReasons.get(i)[1]);
                }

                initViolationReasonsView();
            }
        }
    }

    // initialize the violation reasons text view
    protected void initViolationReasonsView() {
        TextView violationsView = updateViolationReasonsView();

        // Set the click handler.
        violationsView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (violationReasonChoices != null && violationReasonChoices.length > 0) {
                    SpinnerDialogFragment dialogFragment = new SpinnerDialogFragment(R.string.general_select_reason,
                            R.drawable.sort_check_mark, violationReasonChoices);
                    if (curViolationReason != null) {
                        dialogFragment.curSpinnerItemId = curViolationReason.id;
                    }
                    dialogFragment.show(getFragmentManager(), VIOLATION_REASONS_SPINNER_FRAGMENT);
                } else {
                    // no violation reasons dialog
                    DialogFragmentFactoryV1.getAlertOkayInstance(getString(R.string.general_reason),
                            getString(R.string.hotel_violation_reasons_not_available)).show(getFragmentManager(), null);
                }
            }
        });

    }

    // show selected violation reason or the general string
    private TextView updateViolationReasonsView() {
        TextView violationsView = (TextView) findViewById(R.id.hotel_violation_reason);
        if (violationsView != null) {
            if (curViolationReason != null) {
                violationsView.setText(curViolationReason.name);
            } else {
                violationsView.setText(R.string.general_select_reason);
            }
            // reserveButton.setEnabled(true);
        } else {
            Log.e(Const.LOG_TAG,
                    CLS_TAG + ".updateViolationReasonsView: unable to locate 'hotel_violation_reason' view!");
        }
        return violationsView;
    }

    /**
     * Initializes the card choice view.
     */
    protected void initCardChoiceView() {
        TextView cardSelectionView = updateCardView();

        // Set the click handler.
        cardSelectionView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (isOffline) {
                    showOfflineDialog();
                } else {
                    if (cardChoices != null && cardChoices.length > 0) {
                        SpinnerDialogFragment dialogFragment = new SpinnerDialogFragment(R.string.general_select_card,
                                R.drawable.sort_check_mark, cardChoices);
                        if (curCardChoice != null) {
                            dialogFragment.curSpinnerItemId = curCardChoice.id;
                        }
                        dialogFragment.show(getFragmentManager(), CREDIT_CARDS_SPINNER_FRAGMENT);
                    } else {
                        // no cards dialog
                        DialogFragmentFactoryV1.getAlertOkayInstance(getString(R.string.general_credit_card),
                                getString(R.string.general_credit_cards_not_available))
                                .show(getFragmentManager(), null);
                    }
                }
            }
        });
    }

    private TextView updateCardView() {
        TextView cardSelectionView = (TextView) findViewById(R.id.hotel_credit_card);
        if (cardSelectionView != null) {
            if (curCardChoice != null) {
                cardSelectionView.setText(curCardChoice.name);
            } else {
                cardSelectionView.setText(R.string.general_select_card);
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initCardChoiceView: unable to locate 'card_selection' view!");
        }
        return cardSelectionView;
    }

    // same view used for pre sell options retrieval and booking
    public void showProgressBar(int messageResourceId) {
        if (!progressbarVisible) {
            View progressBar = findViewById(R.id.hotel_booking_screen_load);
            progressbarVisible = true;
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            TextView progressBarMsg = (TextView) findViewById(R.id.hotel_preselloptions_progress_msg);
            progressBarMsg.setText(messageResourceId);
            progressBarMsg.setVisibility(View.VISIBLE);
            progressBarMsg.bringToFront();
        }
    }

    // same view used for pre sell options retrieval and booking
    public void hideProgressBar() {
        if (progressbarVisible) {
            View progressBar = findViewById(R.id.hotel_booking_screen_load);
            progressbarVisible = false;
            progressBar.setVisibility(View.GONE);
            View progressBarMsg = findViewById(R.id.hotel_preselloptions_progress_msg);
            progressBarMsg.setVisibility(View.GONE);
        }
    }

    private void showCancellationPolicy() {
        String statement = (String) getText(R.string.hotel_reserve_cancel_policy_not_available);
        if (cancellationPolicyStatements != null) {
            // format the statements
            statement = TextUtils.join("\n", cancellationPolicyStatements);
        }

        DialogFragmentFactoryV1
                .getAlertOkayInstance(getText(R.string.hotel_reserve_cancel_policy).toString(), statement)
                .show(getFragmentManager(), "");
    }

    private void setPreSellOptions(HotelPreSellOption preSellOption) {
        this.preSellOption = preSellOption;
        hideProgressBar();

        if (preSellOption != null) {
            initPreSellOptions();
            reserveButton.setEnabled(true);

        } else {
            Toast.makeText(this, "could not retrieve sell options", Toast.LENGTH_LONG).show();
        }
    }

    private void initPreSellOptions() {
        if (preSellOption != null) {
            // hotel cancellation policy
            cancellationPolicyStatements = preSellOption.hotelCancellationPolicy;

            // credit cards
            initCardChoices();

            // TODO - affinity programs etc

        } else {
            Toast.makeText(this, "could not retrieve sell options", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initializes the list of spinner objects representing card choices and will initialize 'curCardChoice' to the spinner item
     * representing the default card, if any.
     */
    private void initCardChoices() {
        List<CreditCard> cards = (preSellOption == null ? null : preSellOption.creditCards);

        if (cards != null) {
            CreditCard defaultCard = (preSellOption == null ? null : preSellOption.getDefualtCreditCard());

            // Construct some spinner objects which will be used to populate a dialog.
            cardChoices = new SpinnerItem[cards.size()];
            for (int cardChInd = 0; cardChInd < cards.size(); ++cardChInd) {
                CreditCard card = cards.get(cardChInd);
                String numberSubstring = card.lastFour;
                String displayName = String.format("%s %s", card.name == null ? "" : card.name,
                        numberSubstring == null ? "" : numberSubstring);
                cardChoices[cardChInd] = new SpinnerItem(card.id, displayName);
                if (curCardChoice == null && defaultCard != null && defaultCard.id == card.id) {
                    curCardChoice = cardChoices[cardChInd];
                }
            }

            updateCardView();
        }
    }

    /**
     * Will initialize the travel custom fields view.
     */
    public void initTravelCustomFieldsView() {
        // Check for whether 'custom_fields' view group exists!
        if (formFields != null && formFields.size() > 0) {
            if (findViewById(R.id.hotel_booking_custom_fields) != null) {
                if (findViewById(R.id.hotel_room_violation_view) != null) {
                    findViewById(R.id.view_separator5).setVisibility(View.VISIBLE);
                }
                addTravelCustomFieldsView(false, false);
            }
        }
    }

    /**
     * Will add the travel custom fields view to the existing activity view.
     *
     * @param readOnly       contains whether the fields should be read-only.
     * @param displayAtStart if <code>true</code> will result in the fields designated to be displayed at the start of the booking process
     *                       will be displayed; otherwise, fields at the end of the booking process will be displayed.
     */
    protected void addTravelCustomFieldsView(boolean readOnly, boolean displayAtStart) {
        // Company has static custom fields, so display them via our nifty fragment!
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        travelCustomFieldsFragment = new TravelCustomFieldsFragment();
        travelCustomFieldsFragment.readOnly = readOnly;
        travelCustomFieldsFragment.displayAtStart = displayAtStart;
        travelCustomFieldsFragment.customFields = formFields;
        fragmentTransaction
                .add(R.id.hotel_booking_custom_fields, travelCustomFieldsFragment, TRAVEL_CUSTOM_VIEW_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    // }

    /**
     * Will commit any travel custom field values to the underlying object model and persistence.
     */
    protected void commitTravelCustomFields() {
        if (travelCustomFieldsFragment != null) {
            travelCustomFieldsFragment.saveFieldValues();
        }
    }

    @SuppressLint("ShowToast")
    private void doBooking() {
        if (!isOffline) {

            reserveButton.setEnabled(false);
            boolean hasAllRequiredFields = true;
            StringBuffer requiredFieldsMsg = new StringBuffer();
            // get selected credit card
            String selectedCreditCardId = null;
            if (cardChoices != null) {
                if (curCardChoice == null) {
                    // show credit card required message
                    requiredFieldsMsg.append(getString(R.string.book_missing_field_card_selection));
                    hasAllRequiredFields = false;
                } else {
                    selectedCreditCardId = curCardChoice.id;
                }
            }

            // get selected violation reason and justification
            ArrayList<ViolationReason> selectedViolationReasons = null;
            String ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevelAsString(hotelRate.maxEnforcementLevel);
            if (ruleEnforcementLevel.equals("ERROR") || ruleEnforcementLevel.equals("WARNING")) {
                // violation reason need to be selected
                if (violationReasons == null) {
                    // show message that violations reason are not available
                    requiredFieldsMsg.append(getString(R.string.general_violaiton_reasons_not_available));
                    hasAllRequiredFields = false;
                } else {
                    // check for selected violation reason
                    if (curViolationReason == null) {
                        // show violation reason required message
                        requiredFieldsMsg.append(getString(R.string.book_missing_field_violation_reason));
                        hasAllRequiredFields = false;
                    }

                    // check for provided justification text if needed as per system configuration
                    String justificationText = null;
                    View justificationView = findViewById(R.id.hotel_violation_justification);
                    if (justificationView != null) {
                        justificationText = ((EditText) justificationView).getText().toString();
                        if (justificationText == null && ruleViolationExplanationRequired) {
                            // show violation justification text required message
                            requiredFieldsMsg.append(getString(R.string.general_specify_justification));
                            hasAllRequiredFields = false;
                        }
                    }

                    // if all required fields available then populate the violation reasons list
                    if (hasAllRequiredFields) {
                        selectedViolationReasons = new ArrayList<ViolationReason>();
                        ViolationReason selectedViolationReason = new ViolationReason();
                        selectedViolationReason.ruleValueId = currViolationId;
                        selectedViolationReason.violationReasonCode = curViolationReason.id;
                        if (justificationText != null) {
                            selectedViolationReason.justification = justificationText;
                        }
                        selectedViolationReasons.add(selectedViolationReason);
                    }
                }
            }

            // custom fields
            if (formFields != null) {
                if (validateTravelCustomFields()) {
                    commitTravelCustomFields();
                } else {
                    hasAllRequiredFields = false;
                    requiredFieldsMsg.append("required custom fields missing");
                }
            }

            // do the booking if all the required fields are available
            if (hasAllRequiredFields) {
                showProgressBar(R.string.hotel_booking_retrieving);
                hotelBookingReceiver = new BaseAsyncResultReceiver(new Handler());

                hotelBookingReceiver.setListener(new HotelBookingReplyListener());

                // populate custField objects from formFields
                List<FormField> custFields = null;
                if (formFields != null && formFields.size() > 0) {
                    custFields = new ArrayList<FormField>();
                    for (TravelCustomField tcf : formFields) {
                        // only if a value is selected for the custom field
                        if (tcf.getValue() != null && tcf.getValue().trim() != null) {
                            FormField f = new FormField();
                            f.setId(tcf.getId());
                            f.setValue(tcf.getValue());
                            custFields.add(f);
                        }
                    }
                }

                // create and invoke the async task
                hotelBookingAsyncRequestTask = new HotelBookingAsyncRequestTask(this, HOTEL_BOOKING_ID,
                        hotelBookingReceiver, selectedCreditCardId, currentTripId, selectedViolationReasons, custFields,
                        null, false, preSellOption.bookingURL.href);

                hotelBookingAsyncRequestTask.execute();
            } else {
                // show the required fields messages
                DialogFragmentFactoryV1
                        .getAlertOkayInstance(getString(R.string.general_required_fields), requiredFieldsMsg.toString())
                        .show(getFragmentManager(), null);
                reserveButton.setEnabled(true);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Service Unavailable", Toast.LENGTH_LONG).show();
        }

        // TODO - travel program Id
        // String travelProgramId

    }

    @Override
    public void onSpinnerItemSelected(SpinnerItem selectedSpinnerItem, String fragmentTagName) {
        if (fragmentTagName.equals(VIOLATION_REASONS_SPINNER_FRAGMENT)) {
            curViolationReason = selectedSpinnerItem;
            updateViolationReasonsView();
        } else if (fragmentTagName.equals(CREDIT_CARDS_SPINNER_FRAGMENT)) {
            curCardChoice = selectedSpinnerItem;
            updateCardView();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and stop any outstanding
        // request of async task
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (hotelBookingAsyncRequestTask != null) {
                hotelBookingAsyncRequestTask.cancel(false);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCustomAction() {
        doBooking();

    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sendTravelCustomFieldsUpdateRequest(List<TravelCustomField> fields) {
        update = true;
        formFields = fields;
        travelCustomFieldsConfig.formFields = formFields;
        // Initialize the loader.
        lm.initLoader(CUSTOM_FIELDS_LOADER_ID, null, customFieldsLoaderListener);
    }

    private class HotelBookingReplyListener implements AsyncReplyListener {

        /*
         * (non-Javadoc)
         *
         * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestSuccess(android.os.Bundle)
         */
        public void onRequestSuccess(Bundle resultData) {
            if (resultData != null) {

                hideProgressBar();
                HotelBookingRESTResult bookingResult = (HotelBookingRESTResult) resultData
                        .getSerializable("HotelBookingResult");
                if (bookingResult != null) {

                    Toast.makeText(getApplicationContext(), R.string.hotel_booking_success, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, bookingResult.itineraryLocator);
                    // resultData.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, trip.itinLocator);EXTRA_TRAVEL_RECORD_LOCATOR
                    intent.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, bookingResult.recordLocator);
                    setResult(Activity.RESULT_OK, intent);
                }
                // TODO add GA event for booking

                finish();
                // finishActivity(Const.REQUEST_CODE_BOOK_HOTEL);
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestFail(android.os.Bundle)
         */
        public void onRequestFail(Bundle resultData) {
            Toast.makeText(getApplicationContext(), R.string.hotel_booking_failed, Toast.LENGTH_LONG).show();
            reserveButton.setEnabled(false);
            hideProgressBar();

        }

        /*
         * (non-Javadoc)
         *
         * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestCancel(android.os.Bundle)
         */
        public void onRequestCancel(Bundle resultData) {
            cleanup();
        }

        /*
         * (non-Javadoc)
         *
         * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#cleanup()
         */
        public void cleanup() {
            hotelBookingReceiver.setListener(null);
            hotelBookingReceiver = null;
        }
    }
}

package com.concur.mobile.platform.ui.travel.hotel.activity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.travel.booking.CreditCard;
import com.concur.mobile.platform.travel.provider.TravelUtilHotel;
import com.concur.mobile.platform.travel.search.hotel.HotelPreSellOption;
import com.concur.mobile.platform.travel.search.hotel.HotelPreSellOptionLoader;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.travel.search.hotel.HotelViolation;
import com.concur.mobile.platform.travel.search.hotel.HotelViolationComparator;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactoryV1;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.SpinnerDialogFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.SpinnerDialogFragment.SpinnerDialogFragmentCallbackListener;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.util.ParallaxScollView;
import com.concur.mobile.platform.util.Format;

/**
 * 
 * @author RatanK
 * 
 */
public class HotelBookingActivity extends Activity implements LoaderManager.LoaderCallbacks<HotelPreSellOption>,
        SpinnerDialogFragmentCallbackListener {

    protected static final String CLS_TAG = HotelBookingActivity.class.getSimpleName();

    private static final int HOTEL_PRE_SELL_OPTION_LOADER_ID = 0;
    private static final String VIOLATION_REASONS_SPINNER_FRAGMENT = "violations.reasons.spinner.fragment";
    private static final String CREDIT_CARDS_SPINNER_FRAGMENT = "violations.reasons.spineer.fragment";

    private LoaderManager lm;
    private String roomDesc;
    private Double amount;
    private String currCode;
    private String sellOptionsURL;
    private String[] cancellationPolicyStatements;
    private boolean progressbarVisible;

    private HotelRate hotelRate;
    private HotelPreSellOption preSellOption;

    // Contains the currently selected card.
    protected SpinnerItem curCardChoice;
    // Contains the list of cards.
    protected SpinnerItem[] cardChoices;
    private String location;
    private String durationOfStayForDisplay;
    private int numOfNights;
    private String headerImageURL;
    private String hotelName;
    // private String maxEnforcementLevelString;
    private SpinnerItem[] violationReasonChoices;
    private ArrayList<String[]> violationReasons;
    protected SpinnerItem curViolationReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_booking);

        showProgressBar();

        Intent intent = getIntent();

        hotelRate = (HotelRate) intent.getSerializableExtra("roomSelected");
        location = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
        durationOfStayForDisplay = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY);
        numOfNights = intent.getIntExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, 0);
        headerImageURL = intent.getStringExtra("headerImageURL");
        hotelName = intent.getStringExtra("hotelName");
        // maxEnforcementLevelString = intent.getStringExtra("maxEnforcementLevelString");
        violationReasons = (ArrayList<String[]>) intent.getSerializableExtra("violationReasons");

        if (hotelRate != null) {

            sellOptionsURL = hotelRate.sellOptions.href;
            roomDesc = hotelRate.description;
            amount = hotelRate.amount;
            currCode = hotelRate.currency;

        }

        if (savedInstanceState != null) {
            // retrieve the preselloption
        }

        if (preSellOption == null) {
            // Initialize the loader.
            lm = getLoaderManager();
            lm.initLoader(HOTEL_PRE_SELL_OPTION_LOADER_ID, null, this);
        }

        // restore the values if any
        // initValues(savedInstanceState);

        // initialize the view
        initView();

    }

    /**
     * Initialize values.
     * 
     * @param inState
     *            a bundle containing saved state.
     */
    // protected void initValues(Bundle inState) {
    //
    // // Init the card choices.
    // initCardChoices();
    //
    // initCancellationPolicyView();
    //
    // }

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
                ParallaxScollView mListView = (ParallaxScollView) findViewById(R.id.hotel_room_image);
                View header = LayoutInflater.from(this).inflate(R.layout.hotel_image_header, null);
                ImageView imageview = (ImageView) header.findViewById(R.id.travelCityscape);
                imageview.setImageBitmap(bitmap);

                mListView.setParallaxImageView(imageview);
                mListView.addHeaderView(header);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_expandable_list_item_1, new String[] {});
                mListView.setAdapter(adapter);
            }
        }

        // room desc
        TextView txtView = (TextView) findViewById(R.id.hotel_room_desc);
        txtView.setText(roomDesc);

        // dates
        txtView = (TextView) findViewById(R.id.date_span);
        txtView.setText(durationOfStayForDisplay);

        // number of nights
        txtView = (TextView) findViewById(R.id.hotel_room_night);
        txtView.setText(Format.localizeText(this.getApplicationContext(), R.string.hotel_reserve_num_of_nights,
                numOfNights));

        // amount
        txtView = (TextView) findViewById(R.id.hotel_room_rate);
        txtView.setText(FormatUtil.formatAmountWithNoDecimals(amount, this.getResources().getConfiguration().locale,
                currCode, true, false));

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

        // reserve UI
        Button reserveButton = (Button) findViewById(R.id.footer_button);
        reserveButton.setText(R.string.hotel_reserve_this_room);
        reserveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                doBooking();
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
            List<HotelViolation> violations = TravelUtilHotel.getHotelViolations(getApplicationContext(), valueIds);
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
                } else {
                    ((ImageView) violationsView.findViewById(R.id.hotel_room_max_violation_icon))
                            .setImageResource(R.drawable.icon_status_yellow);
                }

            }

            // initialize the reasons

            // SessionInfo sessInfo = ConfigUtil.getSessionInfo(this);
            // List<ReasonCodeDAO> reasonCodes = TravelUtilHotel.getHotelViolationReasons(getApplicationContext(),
            // sessInfo.getUserId());
            if (violationReasons != null && violationReasons.size() > 0) {
                // Construct spinner objects which will be used to populate a dialog.
                violationReasonChoices = new SpinnerItem[violationReasons.size()];
                for (int i = 0; i < violationReasons.size(); ++i) {
                    violationReasonChoices[i] = new SpinnerItem(violationReasons.get(i)[0], violationReasons.get(i)[1]);
                }

                initViolationReasonsView();
            }

            // initialize the justification
            // TextView justificationView = (TextView)violationsView.findViewById(R.id.hotel_violation_justification);

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

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".updateViolationReasonsView: unable to locate 'hotel_violation_reason' view!");
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
                            getString(R.string.general_credit_cards_not_available)).show(getFragmentManager(), null);
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

    public void showProgressBar() {
        if (!progressbarVisible) {
            View progressBar = findViewById(R.id.hotel_booking_screen_load);
            progressbarVisible = true;
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            View progressBarMsg = findViewById(R.id.hotel_preselloptions_progress_msg);
            progressBarMsg.setVisibility(View.VISIBLE);
            progressBarMsg.bringToFront();
        }
    }

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

        DialogFragmentFactoryV1.getAlertOkayInstance(getText(R.string.hotel_reserve_cancel_policy).toString(),
                statement).show(getFragmentManager(), "");
    }

    private void setPreSellOptions(HotelPreSellOption preSellOption) {
        this.preSellOption = preSellOption;
        hideProgressBar();

        if (preSellOption != null) {
            initPreSellOptions();
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

    private void doBooking() {
        // lm.initLoader(HOTEL_BOOKING_LOADER_ID, null, this);
    }

    @Override
    public Loader<HotelPreSellOption> onCreateLoader(int id, Bundle bundle) {

        // request initial search
        Log.d(Const.LOG_TAG, " ***** creating preselloption loader *****  ");

        PlatformAsyncTaskLoader<HotelPreSellOption> hotelPreSellOptionAsyncTaskLoader = new HotelPreSellOptionLoader(
                this, sellOptionsURL);

        return hotelPreSellOptionAsyncTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<HotelPreSellOption> loader, HotelPreSellOption hotelPreSellOption) {
        setPreSellOptions(hotelPreSellOption);

    }

    @Override
    public void onLoaderReset(Loader<HotelPreSellOption> loader) {
        // nothing to do here
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
}

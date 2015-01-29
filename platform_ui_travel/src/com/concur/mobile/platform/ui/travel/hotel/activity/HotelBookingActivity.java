package com.concur.mobile.platform.ui.travel.hotel.activity;

import java.net.URI;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.travel.booking.CreditCard;
import com.concur.mobile.platform.travel.search.hotel.HotelPreSellOption;
import com.concur.mobile.platform.travel.search.hotel.HotelPreSellOptionLoader;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.util.ParallaxScollView;
import com.concur.mobile.platform.util.Format;

/**
 * 
 * @author RatanK
 * 
 */
public class HotelBookingActivity extends Activity implements LoaderManager.LoaderCallbacks<HotelPreSellOption> {

    protected static final String CLS_TAG = HotelBookingActivity.class.getSimpleName();

    private static final int HOTEL_PRE_SELL_OPTION_LOADER_ID = 0;

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

    /**
     * Initializes the card choice view.
     */
    protected void initCardChoiceView() {
        TextView cardSelectionView = updateCardView();

        // Set the click handler.
        cardSelectionView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (cardChoices != null && cardChoices.length > 0) {
                    showCreditCardsDialog();
                } else {
                    // no cards dialog
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

    private void showCreditCardsDialog() {
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
                initCardChoiceView();
                // removeDialog(DIALOG_SELECT_CARD);
            }
        });
        dlgBldr.setOnCancelListener(new OnCancelListener() {

            public void onCancel(DialogInterface dialog) {
                // removeDialog(DIALOG_SELECT_CARD);
            }
        });
        dlgBldr.create();
        dlgBldr.show();
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

        Toast.makeText(this, statement, Toast.LENGTH_SHORT).show();
        // DialogFragmentFactoryV1.getAlertOkayInstance("Cancellation Policy", statement).show(getFragmentManager(), "");
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

        // TODO - does this need to be fired in a separate thread?
        // TravelUtilHotel.deleteAllHotelDetails(this);

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
        // TODO Auto-generated method stub

    }
}

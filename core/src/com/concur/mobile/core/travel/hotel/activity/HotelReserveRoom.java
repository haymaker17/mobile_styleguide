/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.AsyncImageAdapter;
import com.concur.mobile.core.travel.activity.ImageActivity;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.ImagePair;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail;
import com.concur.mobile.core.travel.hotel.data.HotelRoomDetail;
import com.concur.mobile.core.travel.hotel.service.HotelConfirmRequest;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.LayoutUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>Activity</code> for reserving hotel rooms.
 * 
 * @author AndrewK
 */
public class HotelReserveRoom extends TravelBaseActivity {

    private static final String CLS_TAG = HotelReserveRoom.class.getSimpleName();

    protected static final String HOTEL_CONFIRM_RECEIVER_KEY = "hotel.confirm.receiver";

    private static final int DIALOG_ROOM_DESCRIPTION = DIALOG_ID_BASE + 1;

    // Contains the property id.
    protected String propertyId;

    // Contains the room id.
    protected String roomId;

    // Contains a reference to the HotelChoice object this activity was invoked on.
    protected HotelChoice hotel;

    // Contains a reference to the corresponding HotelDetail object.
    protected HotelChoiceDetail hotelDetail;

    // Contains a reference to the hotel room.
    protected HotelRoomDetail hotelRoom;

    // Contains the broadcast receiver to handle the result of a reserving a room.
    protected ReserveRoomReceiver reserveRoomReceiver;
    // Contains the intent filter used to register the hotel confirm receiver.
    protected IntentFilter reserveRoomFilter;
    // Contains a reference to an outstanding request to confirm a hotel room.
    protected HotelConfirmRequest reserveRoomRequest;

    // Contains the calendar check-in date.
    protected Calendar checkInDateLocal;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        // Restore any receivers.
        restoreReceivers();

        initValues(inState);
        initUI();

        // Fetch booking information fields if created on a non-orientation change
        // and no passed in trip.
        if (!orientationChange) {
            if (cliqbookTripId == null) {
                // Check whether a travel custom fields view fragment already exists. This can
                // can be the case if a device gets rotated while this activity is on the stack
                // and not directly visible. Example, is on HotelReserveRoom screen, go to hotel room details
                // and rotate the device, then press HW back button.
                if (!hasTravelCustomFieldsView()) {
                    if (shouldRequestTravelCustomFields()) {
                        sendTravelCustomFieldsRequest();
                    } else {
                        initTravelCustomFieldsView();
                    }
                }
            }
        }
    }

    @Override
    protected boolean getDisplayAtStart() {
        return false;
    }

    protected void initHotelRoom() {
        if (getConcurCore().getHotelSearchResults() != null) {
            // Locate the instance of HotelChoice represented by property id.
            ArrayList<HotelChoice> hotelChoices = getConcurCore().getHotelSearchResults().hotelChoices;
            Iterator<HotelChoice> hotelIter = hotelChoices.iterator();
            while (hotelIter.hasNext()) {
                hotel = hotelIter.next();
                if (hotel.propertyId != null && hotel.propertyId.equalsIgnoreCase(propertyId)) {
                    break;
                }
                hotel = null;
            }
            // Locate the HotelDetail object.
            hotelDetail = getConcurCore().getHotelDetail(propertyId);
            if (hotel != null && hotelDetail != null) {
                // Locate the hotel room.
                Iterator<HotelRoomDetail> roomIter = hotelDetail.rooms.iterator();
                while (roomIter.hasNext()) {
                    hotelRoom = roomIter.next();
                    if (hotelRoom.bicCode != null && hotelRoom.bicCode.equalsIgnoreCase(roomId)) {
                        break;
                    } else {
                        hotelRoom = null;
                    }
                }
                if (hotelRoom == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initHotelRoom: unable to locate room with bic code '" + roomId
                            + "'.");
                }
            } else {
                // Hotel choice and detail
                Log.e(Const.LOG_TAG, CLS_TAG + ".initHotelRoom: unable to locate hotel detail containing room!");
            }
        } else {
            // TODO: App got re-started and previous search results are gone!
        }
    }

    protected void initValues(Bundle inState) {
        if (inState != null) {
            propertyId = inState.getString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID);
            if (propertyId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: saved instance bundle does not contain property id!");
            }
            roomId = inState.getString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_ROOM_ID);
            if (roomId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: saved instance bundle does not contain room id!");
            }
            checkInDateLocal = (Calendar) inState.getSerializable(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        } else {
            Intent intent = getIntent();
            propertyId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID);
            if (propertyId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: property id not passed into activity!");
            }
            roomId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_ROOM_ID);
            if (roomId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: room id not passed into activity!");
            }
            checkInDateLocal = (Calendar) intent
                    .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        }
        initHotelRoom();

        // Added for MOB-14317
        if (!orientationChange) {
            showDialog(RETRIEVE_PRE_SELL_OPTIONS_DIALOG);
            getPreSellOptions(hotelRoom.choiceId);
        }

        super.initValues(inState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            // Store attendee type receiver.
            if (reserveRoomReceiver != null) {
                retainer.put(HOTEL_CONFIRM_RECEIVER_KEY, reserveRoomReceiver);
                reserveRoomReceiver.setActivity(null);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: retainer is null!");
        }
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer != null) {
            // Restore travel custom fields receiver.
            if (retainer.contains(HOTEL_CONFIRM_RECEIVER_KEY)) {
                reserveRoomReceiver = (ReserveRoomReceiver) retainer.get(HOTEL_CONFIRM_RECEIVER_KEY);
                if (reserveRoomReceiver != null) {
                    reserveRoomReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer contains a null hotel confirm receiver!");
                }
            }
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
        outState.putString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID, propertyId);
        outState.putString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_ROOM_ID, roomId);
        outState.putSerializable(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDateLocal);
    }

    protected void initUI() {

        setContentView(R.layout.travel_hotel_room_reserve);

        // Set the screen title
        getSupportActionBar().setTitle(R.string.hotel_room_reserve_title);

        if (hotelRoom != null) {
            configureHeader();
            configureHotelInformation();
            configureRoomInformation();
            if (!orientationChange) {
                initHotelPreSellOptions();
            }
            super.initUI();
        }
    }

    /**
     * Configures the general hotel and room info.
     */
    private void configureHotelInformation() {

        // Set the room description.
        View view = findViewById(R.id.roomDetailDescription);
        if (view != null) {

            if (hotelRoom.summary != null) {
                ((TextView) view.findViewById(R.id.field_name)).setText(R.string.hotel_room_description);
                ((TextView) view.findViewById(R.id.field_value)).setText(hotelRoom.summary);
                view.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        showDialog(DIALOG_ROOM_DESCRIPTION);
                    }
                });
            } else {
                view.findViewById(R.id.field_image).setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureHotelInformation: missing room description text view!");
        }

        // Set the hotel phone number.
        view = findViewById(R.id.hotelDetailPhone);
        if (view != null) {

            view.findViewById(R.id.field_image).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.field_name)).setText(R.string.segment_phone);

            // Get the phone number.
            String phone = "";
            if (hotel.tollFree != null && hotel.tollFree.length() > 0 && !hotel.tollFree.equalsIgnoreCase("N-A")) {
                phone = hotel.tollFree;
            } else if (hotel.phone != null && hotel.phone.length() > 0) {
                phone = hotel.phone;
            }

            TextView phoneTextView = ((TextView) view.findViewById(R.id.field_value));
            Spannable linkText = new SpannableString(phone);
            Linkify.addLinks(linkText, Linkify.PHONE_NUMBERS);
            phoneTextView.setText(linkText);
            phoneTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureHotelInformation: missing phone text view!");
        }

        // Set the address and linify it.
        final String hotelAddress = ViewUtil.getHotelAddress(hotel);
        View addressView = findViewById(R.id.hotelDetailAddress);
        addressView.findViewById(R.id.field_image).setVisibility(View.GONE);
        ((TextView) addressView.findViewById(R.id.field_name)).setText(R.string.hotel_search_label_location);
        if (ViewUtil.isMappingAvailable(this)) {
            ViewUtil.setText(addressView, R.id.field_value, hotelAddress, Linkify.MAP_ADDRESSES);
        } else {
            ((TextView) addressView.findViewById(R.id.field_value)).setText(hotelAddress);
        }

    }

    /**
     * Configures the rest of the room information and the booking controls.
     */
    private void configureRoomInformation() {

        // Set the room check-in date.
        View view = findViewById(R.id.roomDetailCheckin);
        if (view != null) {
            Intent intent = getIntent();
            view.findViewById(R.id.field_image).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.field_name)).setText(R.string.hotel_search_label_checkin);
            ((TextView) view.findViewById(R.id.field_value)).setText(intent
                    .getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureRoomInformation: missing check-in text view!");
        }

        // Set the room check-out date.
        view = findViewById(R.id.roomDetailCheckout);
        if (view != null) {
            Intent intent = getIntent();
            view.findViewById(R.id.field_image).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.field_name)).setText(R.string.hotel_search_label_checkout);
            ((TextView) view.findViewById(R.id.field_value)).setText(intent
                    .getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureRoomInformation: missing check-out text view!");
        }

        // Set the room rate.
        view = findViewById(R.id.roomDetailDailyRate);
        if (view != null) {
            // view.findViewById(R.id.field_image).setVisibility(View.GONE);
            Double roomRate = Parse.safeParseDouble(hotelRoom.rate);
            if (roomRate != null) {
                ((TextView) view.findViewById(R.id.field_name)).setText(R.string.hotel_room_rate);
                String formattedAmtStr = FormatUtil.formatAmount(roomRate, getResources().getConfiguration().locale,
                        hotelRoom.crnCode, true, true);
                TextView txtView = (TextView) view.findViewById(R.id.field_value1);
                txtView.setText(formattedAmtStr);
                txtView.setTextAppearance(this, R.style.FormFieldValueBlueFareText);

                // init Travel Points
                initTravelPointsInPrice();

            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureRoomInformation: missing room rate text view!");
        }
    }

    /**
     * Configure the header of the view.
     */
    private void configureHeader() {

        // Set the hotel property image.
        if (hotel.propertyImages != null && hotel.propertyImages.size() > 0) {
            ImagePair imgPair = hotel.propertyImages.get(0);
            AsyncImageView asyncImageView = (AsyncImageView) findViewById(R.id.hotelResultVendorImage);
            asyncImageView.setAsyncUri(URI.create(imgPair.thumbnail));
            if (hotel.propertyImages.size() > 1) {
                TextView txtView = (TextView) findViewById(R.id.hotel_vendor_image_press_text);
                if (txtView != null) {
                    txtView.setVisibility(View.VISIBLE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".configureHeader: unable to locate vendor image press.");
                }
                asyncImageView.setOnClickListener(new OnClickListener() {

                    /*
                     * (non-Javadoc)
                     * 
                     * @see android.view.View.OnClickListener#onClick(android.view.View)
                     */
                    public void onClick(View view) {
                        // Display the vendor image gallery dialog.
                        showDialog(Const.DIALOG_TRAVEL_HOTEL_VIEW_IMAGES);
                    }

                });
            }
        } else {
            // Hide the async image view.
            AsyncImageView asyncImageView = (AsyncImageView) findViewById(R.id.hotelResultVendorImage);
            if (asyncImageView != null) {
                asyncImageView.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureHeader: hotel result vendor async image view not found!");
            }
            // Show the default image view.
            ImageView imgView = (ImageView) findViewById(R.id.hotelResultDefaultImage);
            if (imgView != null) {
                imgView.setVisibility(View.VISIBLE);
            }
        }

        // Set the hotel name.
        TextView txtView = (TextView) findViewById(R.id.hotelResultHotelName);
        if (txtView != null) {
            if (hotel.hotel != null) {
                txtView.setText(hotel.hotel);
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureHeader: unable to locate hotel name text view!");
        }

        // Set the hotel address.
        String hotelAddress = ViewUtil.getHotelAddress(hotel);
        if (hotelAddress != null && hotelAddress.length() > 0) {
            ((TextView) findViewById(R.id.hotelAddr)).setText(hotelAddress);
        } else {
            txtView = (TextView) findViewById(R.id.hotelAddr);
            if (txtView != null) {
                txtView.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureHeader: unable to locate hotel address view!");
            }
        }

        // Set the star rating.
        int starRating = 0;
        if (hotel.starRating != null && hotel.starRating.length() > 0) {
            try {
                starRating = Integer.parseInt(hotel.starRating);
            } catch (NumberFormatException numFormExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: non-integral star rating of '" + hotel.starRating + "'.");
            }
        }
        ImageView imgView = (ImageView) findViewById(R.id.star_rating);
        if (imgView != null) {
            if (starRating == 0) {
                imgView.setVisibility(View.GONE);
            } else {
                int starImgResId = -1;
                if (starRating == 1) {
                    starImgResId = R.drawable.stars_1;
                } else if (starRating == 2) {
                    starImgResId = R.drawable.stars_2;
                } else if (starRating == 3) {
                    starImgResId = R.drawable.stars_3;
                } else if (starRating == 4) {
                    starImgResId = R.drawable.stars_4;
                } else if (starRating >= 5) {
                    starImgResId = R.drawable.stars_5;
                }
                if (starImgResId != -1) {
                    imgView.setImageResource(starImgResId);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: invalid star rating of '" + starRating
                            + "...hiding stars.");
                    imgView.setVisibility(View.GONE);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'star_rating' in hotelView!");
        }

        // Set the company preferred rating.
        imgView = (ImageView) findViewById(R.id.hotelResultPreferredRank);
        if (imgView != null) {
            txtView = (TextView) findViewById(R.id.pref_rank_text);
            if (txtView != null) {
                if (hotel.prefRankI != null) {
                    switch (hotel.prefRankI) {
                    case 0: {
                        txtView.setText(R.string.general_not_preferred);
                        imgView.setVisibility(View.GONE);
                        break;
                    }
                    case 1: {
                        txtView.setText(R.string.general_chain_least_preferred);
                        imgView.setImageResource(R.drawable.diamonds_gray_1);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case 2: {
                        txtView.setText(R.string.general_chain_preferred);
                        imgView.setImageResource(R.drawable.diamonds_gray_2);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case 3: {
                        txtView.setText(R.string.general_chain_most_preferred);
                        imgView.setImageResource(R.drawable.diamonds_gray_3);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case 4: {
                        txtView.setText(R.string.general_least_preferred);
                        imgView.setImageResource(R.drawable.diamonds_1);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case 5: {
                        txtView.setText(R.string.general_preferred);
                        imgView.setImageResource(R.drawable.diamonds_2);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case 10: {
                        txtView.setText(R.string.general_most_preferred);
                        imgView.setImageResource(R.drawable.diamonds_3);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    default: {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unhandled hotel pref rank value of '"
                                + hotel.prefRank + "'.");
                        break;
                    }
                    }
                }
            } else {
                txtView = (TextView) findViewById(R.id.pref_rank_text);
                if (txtView != null) {
                    txtView.setText(R.string.general_not_preferred);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'pref_rank_text' in hotel view!");
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel result preferred rank view!");
        }

        // Set the recommendation.
        String recomLocalizedText = HotelChoiceListItem.getRecommendationTextId(hotel.recommendationSource,
                hotel.recommendationSourceNumber);
        TextView recomTextView = (TextView) findViewById(R.id.recom_text);
        if (recomLocalizedText == null) {
            recomTextView.setVisibility(View.GONE);
        } else {
            recomTextView.setVisibility(View.VISIBLE);
            recomTextView.setText(recomLocalizedText);
        }

        // Add listener to view the hotel room details when the header is clicked.
        View header = findViewById(R.id.hotel_header);
        if (header != null) {
            header.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    Intent intent = new Intent(HotelReserveRoom.this, HotelSearchRoomDetails.class);
                    intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID, propertyId);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void cancelBookingRequest() {
        if (reserveRoomRequest != null) {
            reserveRoomRequest.cancel();
        }
    }

    @Override
    protected CharSequence getBookingConfirmDialogMessage() {
        if (hotelRoom.depositRequired) {
            return getText(R.string.dlg_travel_hotel_deposit_confirm_message);
        }
        return getText(R.string.dlg_travel_hotel_confirm_message);
    }

    @Override
    protected CharSequence getBookingConfirmDialogTitle() {
        return getText(R.string.confirm);
    }

    @Override
    protected CharSequence getBookingFailedDialogTitle() {
        return getText(R.string.dlg_hotel_confirm_failed_title);
    }

    @Override
    protected CharSequence getBookingProgressDialogMessage() {
        return getText(R.string.reserving_hotel_room);
    }

    @Override
    protected CharSequence getBookingSucceededDialogMessage() {
        return getText(R.string.dlg_hotel_confirm_succeed_message);
    }

    @Override
    protected CharSequence getBookingSucceededDialogTitle() {
        return getText(R.string.dlg_hotel_confirm_succeed_title);
    }

    @Override
    protected CharSequence getBookingType() {
        return getText(R.string.general_hotel);
    }

    @Override
    protected List<Violation> getViolations() {
        List<Violation> violations = null;
        if (hotelRoom != null) {
            violations = hotelRoom.violations;
        }
        return violations;
    }

    @Override
    protected void sendBookingRequest() {
        sendReserveRoomRequest();
    }

    @Override
    protected void onBookingSucceeded() {
        if (!launchedWithCliqbookTripId) {
            // Set the flag that the trip list should be refetched.
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
            }
            // Retrieve an updated trip summary list, then retrieve the detailed itinerary.
            isShowRatingPrompt = true;
            sendItinerarySummaryListRequest();
        } else {
            // Just finish the activity.
            finish();
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
        case Const.DIALOG_TRAVEL_HOTEL_VIEW_IMAGES: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.hotel_search_property_images));
            builder.setCancelable(true);
            LayoutInflater inflater = LayoutInflater.from(HotelReserveRoom.this);
            Gallery hotelImageGallery = (Gallery) inflater.inflate(R.layout.travel_hotel_room_gallery, null);
            hotelImageGallery.setAdapter(new AsyncImageAdapter(HotelReserveRoom.this, hotel.propertyImages));
            hotelImageGallery.setOnItemClickListener(new OnItemClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View,
                 * int, long)
                 */
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(HotelReserveRoom.this, ImageActivity.class);
                    intent.putExtra(Const.EXTRA_IMAGE_TITLE, getText(R.string.hotel_search_picture));
                    ImagePair imgPair = (ImagePair) parent.getAdapter().getItem(position);
                    intent.putExtra(Const.EXTRA_IMAGE_URL, imgPair.image);
                    startActivity(intent);
                }
            });
            builder.setView(hotelImageGallery);
            dialog = builder.create();
            break;
        }
        case DIALOG_ROOM_DESCRIPTION: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.hotel_room_description);
            builder.setMessage(hotelRoom.summary);
            builder.setCancelable(true);
            builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }

        }
        return dialog;
    }

    public void sendReserveRoomRequest() {

        registerReserveRoomReceiver();

        // Kick-off the request.
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        ConcurService concurService = ConcurCore.getService();
        String reasonCodeId = "";
        if (reasonCode != null) {
            reasonCodeId = reasonCode.id;
        }
        String violationText = (justificationText != null) ? justificationText : "";
        String creditCardId = null;
        if (curCardChoice != null) {
            creditCardId = curCardChoice.id;
        }
        List<TravelCustomField> tcfs = null;
        // If this booking is part of an existing trip, then custom fields are not presented.
        if (cliqbookTripId == null) {
            tcfs = getTravelCustomFields();
        }
        reserveRoomRequest = concurService.sendConfirmHotelRoomRequest(hotelRoom.bicCode, creditCardId,
                hotel.chainCode, propertyId, hotel.hotel, hotelRoom.sellSource, cliqbookTripId, violationText,
                reasonCodeId, tcfs, useTravelPoints);
        if (reserveRoomRequest != null) {
            showDialog(BOOKING_PROGRESS_DIALOG);
        } else {
            getApplicationContext().unregisterReceiver(reserveRoomReceiver);
            unregisterReserveRoomReceiver();
        }

        // Log the flurry event if the user completed this hotel booking using Voice.
        if (getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false)) {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, Flurry.EVENT_NAME_COMPLETED_HOTEL);
        }
    }

    /**
     * Will register an instance of <code>ReserveRoomReceiver</code> with the application context and set the
     * <code>reserveRoomReceiver</code> attribute.
     */
    public void registerReserveRoomReceiver() {
        if (reserveRoomReceiver == null) {
            reserveRoomReceiver = new ReserveRoomReceiver(this);
            if (reserveRoomFilter == null) {
                reserveRoomFilter = new IntentFilter(Const.ACTION_HOTEL_CONFIRM_RESULTS);
            }
            getApplicationContext().registerReceiver(reserveRoomReceiver, reserveRoomFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerReserveCarReceiver: reserveRoomReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>ReserveRoomReceiver</code> with the application context and set the
     * <code>reserveRoomReceiver</code> to <code>null</code>.
     */
    public void unregisterReserveRoomReceiver() {
        if (reserveRoomReceiver != null) {
            getApplicationContext().unregisterReceiver(reserveRoomReceiver);
            reserveRoomReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReserveRoomReceiver: reserveRoomReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for handling notification of the result reserving a room.
     */
    public class ReserveRoomReceiver extends BaseBroadcastReceiver<HotelReserveRoom, HotelConfirmRequest> {

        /**
         * Constructs an instance of <code>ReserveRoomReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReserveRoomReceiver(HotelReserveRoom activity) {
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
        protected void clearActivityServiceRequest(HotelReserveRoom activity) {
            activity.reserveRoomRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(BOOKING_PROGRESS_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(BOOKING_FAILED_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            onHandleSuccessReservation(activity, intent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(HotelConfirmRequest request) {
            activity.reserveRoomRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterReserveRoomReceiver();
        }

    }

    protected void onHandleSuccessReservation(HotelReserveRoom activity, Intent intent) {
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    }

    protected void locateTripId(HotelReserveRoom activity, Intent intent) {
        Intent result = new Intent();
        activity.itinLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR);
        result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, activity.itinLocator);
        if (activity.cliqbookTripId != null) {
            IItineraryCache itinCache = activity.getConcurCore().getItinCache();
            Trip trip = itinCache.getItinerarySummaryByCliqbookTripId(activity.cliqbookTripId);
            if (trip != null) {
                result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, trip.itinLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to locate trip based on cliqbook trip id!");
            }
        }
        activity.setResult(Activity.RESULT_OK, result);
    }

    protected void flurryEvents(HotelReserveRoom activity) {
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_HOTEL);
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            params.put(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_RESERVE, params);

        // MOB-15567 - flurry events for recommendations
        Map<String, String> paramsForRecommendations = new HashMap<String, String>();
        paramsForRecommendations.put(Flurry.PARAM_NAME_PROPERTY_ID, propertyId);
        // is this hotel recommended
        if (hotel.recommendationSource == null) {
            paramsForRecommendations.put(Flurry.PARAM_NAME_RECOMMENDED, Flurry.PARAM_VALUE_NO);
        } else {
            paramsForRecommendations.put(Flurry.PARAM_NAME_RECOMMENDED, Flurry.PARAM_VALUE_YES);
        }
        // does search results had recommendations
        if (getConcurCore().getHotelSearchResults().hasRecommendation) {
            paramsForRecommendations.put(Flurry.PARAM_NAME_SEARCH_HAD_RECOMMENDATIONS, Flurry.PARAM_VALUE_YES);
        } else {
            paramsForRecommendations.put(Flurry.PARAM_NAME_SEARCH_HAD_RECOMMENDATIONS, Flurry.PARAM_VALUE_NO);
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_HOTEL_RECOMMENDATIONS,
                Flurry.EVENT_NAME_HOTEL_RECOMMENDATIONS_HOTEL_RESERVED, paramsForRecommendations);

        if (getTravelPointsInBank() != null) {

            int travelPoints = (hotelRoom.travelPoints == null ? 0 : hotelRoom.travelPoints);
            logEvents(travelPoints, Flurry.PARAM_VALUE_HOTEL);
        }
    }

    @Override
    protected void updatePreSellOptions() {
        initHotelPreSellOptions();
    }

    // Pre-sell options
    private void initHotelPreSellOptions() {
        // update credit cards
        initCreditCards();

        // init hotel cancellation policy
        initCancellationPolicyView();
    }

    private void initCreditCards() {
        initCardChoices();
        initCardChoiceView();
    }

    /**
     * Initializes the Cancellation Policy View.
     */
    @Override
    protected void initCancellationPolicyView() {
        View policyView = findViewById(R.id.cancellation_policy);
        if (policyView != null) {
            if (preSellOption != null && preSellOption.getCancellationPolicy() != null) {

                policyView.setVisibility(View.VISIBLE);
                policyView.findViewById(R.id.field_image).setVisibility(View.VISIBLE);

                ((TextView) policyView.findViewById(R.id.field_name)).setText(R.string.segment_hotel_cancelpolicy);

                List<String> stmts = preSellOption.getCancellationPolicy().getStatements();
                StringBuilder stmtBldr = new StringBuilder();
                for (String stmt : stmts) {
                    stmtBldr.append(stmt);
                }
                ((TextView) policyView.findViewById(R.id.field_value)).setText(stmtBldr);

                policyView.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        showCancellationPolicyDialog();
                    }
                });
            } else {
                policyView.setVisibility(View.GONE);
                policyView.findViewById(R.id.field_image).setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initCancellationPolicyView: unable to locate cancellation policy!");
        }
    }

    private void showCancellationPolicyDialog() {
        List<String> stmts = preSellOption.getCancellationPolicy().getStatements();

        // format the statements
        String statement = TextUtils.join("\n", stmts);

        AlertDialogFragment dialogFrag = DialogFragmentFactory.getAlertOkayInstance(
                R.string.segment_hotel_cancelpolicy, statement);
        dialogFrag.show(getSupportFragmentManager(), CANCELLATION_POLICY_DIALOG_TAG);
    }

    @Override
    protected boolean canRedeemTravelPointsAgainstViolations() {
        return hotelRoom.canRedeemTravelPointsAgainstViolations;
    }

    @Override
    protected int getTravelPointsToUse() {
        int tp = (hotelRoom.travelPoints == null ? 0 : hotelRoom.travelPoints);
        if (tp != 0) {
            tp = Math.abs(tp);
        }
        return tp;
    }

    @Override
    protected String getTravelPointsInBank() {
        HotelSearchReply results = ((ConcurCore) getApplication()).getHotelSearchResults();
        String travelPointsInBank = null;

        if (results.travelPointsBank != null && results.travelPointsBank.getPointsAvailableToSpend() != null) {
            travelPointsInBank = Integer.toString(results.travelPointsBank.getPointsAvailableToSpend());
        }
        return travelPointsInBank;
    }

    @Override
    protected void showTravelPointsInPrice() {
        View priceView = findViewById(R.id.roomDetailDailyRate);
        if (priceView != null) {
            LayoutUtil.initTravelPointsAtItemLevel(priceView, R.id.field_value2, hotelRoom.travelPoints);
        }
    }

    @Override
    protected void hideTravelPointsInPrice() {
        View priceView = findViewById(R.id.roomDetailDailyRate);
        if (priceView != null) {
            priceView.findViewById(R.id.field_value2).setVisibility(View.GONE);
        }
    }
}

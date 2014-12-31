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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.activity.AsyncImageAdapter;
import com.concur.mobile.core.travel.activity.ImageActivity;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.ImagePair;
import com.concur.mobile.core.travel.data.RuleEnforcementLevel;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail;
import com.concur.mobile.core.travel.hotel.data.HotelRoom;
import com.concur.mobile.core.travel.hotel.data.HotelRoomDetail;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.core.view.ListItemAdapter;

/**
 * An extension of <code>BaseActivity</code> for displaying detailed hotel choice data.
 * 
 * @author AndrewK
 */
public class HotelSearchRooms extends TravelBaseActivity {

    private static final String CLS_TAG = HotelSearchRooms.class.getSimpleName();

    // Contains the property id.
    private String propertyId;

    // Contains the Cliqbook trip id.
    private String cliqbookTripId;

    // Contains the check-in date.
    private String checkInDate;

    private Calendar checkInLocal;

    // Contains the check-out date.
    private String checkOutDate;

    // Contains a reference to the HotelChoice object this activity was invoked on.
    private HotelChoice hotel;

    // Contains a reference to the corresponding HotelDetail object.
    private HotelChoiceDetail hotelDetail;

    // Contains a reference to room list view object.
    private ListView roomList;

    // Contains a reference to the room list adapter.
    private ListItemAdapter<HotelRoomListItem> roomListAdapter;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.travel_hotel_search_rooms);

        if (savedInstanceState != null) {
            propertyId = savedInstanceState.getString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID);
            if (propertyId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: saved instance bundle does not contain property id!");
            }
            cliqbookTripId = savedInstanceState.getString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            checkInDate = savedInstanceState.getString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN);
            checkInLocal = (Calendar) savedInstanceState
                    .getSerializable(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
            checkOutDate = savedInstanceState.getString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT);
        } else {
            Intent intent = getIntent();
            propertyId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID);
            if (propertyId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: property id not passed into activity!");
            }
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID)) {
                cliqbookTripId = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            }
            if (intent.hasExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN)) {
                checkInDate = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN);
            }
            if (intent.hasExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR)) {
                checkInLocal = (Calendar) intent
                        .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
            }
            if (intent.hasExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT)) {
                checkOutDate = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT);
            }
        }

        if (propertyId != null) {
            buildView();
        } else {
            // TODO: need to let end-user know that property information is no longer available
            // and needs to be re-fetched.
        }
    }

    private void buildView() {

        ConcurCore concurCore = (ConcurCore) getApplication();
        HotelSearchReply results = concurCore.getHotelSearchResults();
        if (results != null) {

            // Set the screen title
            getSupportActionBar().setTitle(R.string.hotel_search_rooms_title);

            // Locate the instance of HotelChoice represented by property id.
            ArrayList<HotelChoice> hotelChoices = results.hotelChoices;
            Iterator<HotelChoice> hotelIter = hotelChoices.iterator();
            while (hotelIter.hasNext()) {
                hotel = hotelIter.next();
                if (hotel.propertyId != null && hotel.propertyId.equalsIgnoreCase(propertyId)) {
                    break;
                }
                hotel = null;
            }

            // Locate the HotelDetail object.
            hotelDetail = concurCore.getHotelDetail(propertyId);

            if (hotel != null && hotelDetail != null) {

                configureHeader();

                // set the Travel Points header
                if (hotelDetail.hotelBenchmark != null && hotelDetail.hotelBenchmark.getPrice() != null) {
                    String formattedBenchmarkPrice = FormatUtil.formatAmount(hotelDetail.hotelBenchmark.getPrice(),
                            this.getResources().getConfiguration().locale, hotelDetail.hotelBenchmark.getCrnCode(),
                            true, true);

                    String travelPointsInBank = null;

                    if (results.travelPointsBank != null
                            && results.travelPointsBank.getPointsAvailableToSpend() != null) {
                        travelPointsInBank = Integer.toString(results.travelPointsBank.getPointsAvailableToSpend());
                    }

                    initHotelSelectRoomTravelPointsHeader(formattedBenchmarkPrice, travelPointsInBank);
                }

                configureRooms();

                // Set the total result count.
                String countStr = "";
                int count = roomListAdapter.getCount();
                if (count > 1) {
                    countStr = Format.localizeText(this, R.string.generic_results_choice_count, new Object[] { count });
                } else {
                    countStr = getText(R.string.generic_results_one_count).toString();
                }
                ((TextView) findViewById(R.id.footer_navigation_bar_status)).setText(countStr);

            } else {
                // Hotel choice and detail
                ((TextView) findViewById(R.id.footer_navigation_bar_status))
                        .setText(R.string.search_no_result_dialog_title);
            }

            // MOB-15567 - flurry events for recommendations
            if (!orientationChange) {
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
                        Flurry.EVENT_NAME_HOTEL_RECOMMENDATIONS_RATES_VIEWED, paramsForRecommendations);
            }

        } else {
            // App got re-started and previous search results are gone.
            // TODO: re-fetch results and then locate property id.
            ((TextView) findViewById(R.id.footer_navigation_bar_status))
                    .setText(R.string.search_no_result_dialog_title);
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
                    Intent intent = getHotelDetailIntent();
                    intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID, propertyId);
                    startActivity(intent);
                }
            });
        }

    }

    /**
     * Get Hotel Room Detail Intent
     * 
     * @return
     * */
    protected Intent getHotelDetailIntent() {
        return new Intent(HotelSearchRooms.this, HotelSearchRoomDetails.class);
    }

    /**
     * Configures the list of hotel rooms.
     */
    private void configureRooms() {
        roomList = (ListView) findViewById(R.id.hotel_search_result_rooms_list);
        if (roomList != null) {
            // Construct a list of <code>HotelRoom</code> objects that omit rooms that should be hidden.
            List<HotelRoomListItem> rooms = new ArrayList<HotelRoomListItem>();
            if (hotelDetail.rooms != null) {
                for (HotelRoom room : hotelDetail.rooms) {
                    if (room instanceof HotelRoomDetail) {
                        HotelRoomDetail roomDetail = (HotelRoomDetail) room;
                        Integer maxEnforcementLevel = roomDetail.getMaxRuleEnforcementLevel();
                        if (ViewUtil.getRuleEnforcementLevel(maxEnforcementLevel) != RuleEnforcementLevel.HIDE) {
                            rooms.add(new HotelRoomListItem(room));
                        }
                    } else {
                        rooms.add(new HotelRoomListItem(room));
                    }
                }
            }
            roomListAdapter = new ListItemAdapter<HotelRoomListItem>(this, rooms);
            // Prior to setting the adapter on the view, init the image cache receiver to handle
            // updating the list based on images downloaded asychronously.
            imageCacheReceiver = new ImageCacheReceiver<HotelRoomListItem>(roomListAdapter, roomList);
            registerImageCacheReceiver();
            roomList.setAdapter(roomListAdapter);
            roomList.setOnItemClickListener(new OnItemClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View,
                 * int, long)
                 */
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    HotelRoomListItem hotelRoomListItem = (HotelRoomListItem) adapterView.getItemAtPosition(position);
                    if (hotelRoomListItem != null) {
                        HotelRoom hotelRoom = hotelRoomListItem.getHotelRoom();
                        if (hotelRoom != null) {
                            // MOB-8037 - if max enforcement level is 40 then show message and do not allow for reserve
                            HotelRoomDetail roomDetail = (HotelRoomDetail) hotelRoom;
                            Violation maxEnforcementViolation = ViewUtil.getShowButNoBookingViolation(
                                    roomDetail.violations, roomDetail.maxEnforcementLevel);
                            if (maxEnforcementViolation != null) {
                                showReserveNotAllowed(maxEnforcementViolation.message);
                            } else {
                                // Set up a call to the reserve room activity.
                                Intent intent = getReserveRoom();
                                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID, propertyId);
                                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_ROOM_ID, hotelRoom.bicCode);
                                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN, checkInDate);
                                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInLocal);
                                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT, checkOutDate);
                                intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
                                intent.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED,
                                        getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false));
                                Intent launchIntent = getIntent();
                                if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                                    intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                                            launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                                }
                                startActivityForResult(intent, Const.REQUEST_CODE_BOOK_HOTEL);
                            }
                        }
                    }
                }

            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureRooms: unable to locate room list view!");
        }
    }

    /**
     * Get Reserve Room Intent
     * 
     * */
    protected Intent getReserveRoom() {
        return new Intent(HotelSearchRooms.this, HotelReserveRoom.class);
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
        outState.putString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        outState.putString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN, checkInDate);
        outState.putSerializable(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInLocal);
        outState.putString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT, checkOutDate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_CODE_BOOK_HOTEL) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, data);
                finish();
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
        case Const.DIALOG_TRAVEL_HOTEL_VIEW_IMAGES:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.hotel_search_property_images));
            builder.setCancelable(true);
            LayoutInflater inflater = LayoutInflater.from(HotelSearchRooms.this);
            Gallery hotelImageGallery = (Gallery) inflater.inflate(R.layout.travel_hotel_room_gallery, null);
            hotelImageGallery.setAdapter(new AsyncImageAdapter(HotelSearchRooms.this, hotel.propertyImages));
            hotelImageGallery.setOnItemClickListener(new OnItemClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View,
                 * int, long)
                 */
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(HotelSearchRooms.this, ImageActivity.class);
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

        return dialog;
    }
}

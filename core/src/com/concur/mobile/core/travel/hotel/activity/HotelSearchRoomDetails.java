/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.activity.AsyncImageAdapter;
import com.concur.mobile.core.travel.activity.ImageActivity;
import com.concur.mobile.core.travel.data.ImagePair;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail;
import com.concur.mobile.core.travel.hotel.data.HotelRoomFee;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.AsyncImageView;

/**
 * An extension of <code>BaseActivity</code> for displaying detailed hotel choice data.
 * 
 * @author Chris N. Diaz
 */
public class HotelSearchRoomDetails extends BaseActivity {

    private static final String CLS_TAG = HotelSearchRoomDetails.class.getSimpleName();

    // Contains the property id.
    private String propertyId;

    // Contains a reference to the HotelChoice object this activity was invoked on.
    private HotelChoice hotel;

    // Contains a reference to the corresponding HotelDetail object.
    private HotelChoiceDetail hotelDetail;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.travel_hotel_room_details);

        if (savedInstanceState != null) {
            propertyId = savedInstanceState.getString(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID);
            if (propertyId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: saved instance bundle does not contain property id!");
            }
        } else {
            Intent intent = getIntent();
            propertyId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID);
            if (propertyId == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: property id not passed into activity!");
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

        ConcurCore ConcurCore = (ConcurCore) getApplication();

        if (ConcurCore.getHotelSearchResults() != null) {

            // Set the screen title
            getSupportActionBar().setTitle(R.string.hotel_search_room_details_title);

            // Locate the instance of HotelChoice represented by property id.
            ArrayList<HotelChoice> hotelChoices = ConcurCore.getHotelSearchResults().hotelChoices;
            Iterator<HotelChoice> hotelIter = hotelChoices.iterator();
            while (hotelIter.hasNext()) {
                hotel = hotelIter.next();
                if (hotel.propertyId != null && hotel.propertyId.equalsIgnoreCase(propertyId)) {
                    break;
                }
                hotel = null;
            }

            // Locate the HotelDetail object.
            hotelDetail = ConcurCore.getHotelDetail(propertyId);

            if (hotel != null && hotelDetail != null) {

                configureHeader();
                configureRoomFees();
                configureDetails();

            } else {
                // Hotel choice and detail
            }

        } else {
            // App got re-started and previous search results are gone.
            // TODO: re-fetch results and then locate property id.
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
            if (ViewUtil.isMappingAvailable(this)) {
                ViewUtil.setText(findViewById(R.id.hotel_room_details_view), R.id.hotelAddr, hotelAddress,
                        Linkify.MAP_ADDRESSES);
            } else {
                ((TextView) findViewById(R.id.hotelAddr)).setText(hotelAddress);
            }
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

        // Hide the chevron.
        View header = findViewById(R.id.hotel_header);
        if (header != null) {
            header.findViewById(R.id.field_image).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Add room fee information to the UI.
     */
    private void configureRoomFees() {

        if (hotelDetail.fees != null && hotelDetail.fees.size() > 0) {

            // Get the layout where we'll be adding the fees section to.
            LayoutInflater inflater = LayoutInflater.from(this);
            LinearLayout detailsLayout = (LinearLayout) findViewById(R.id.hotel_search_result_room_details_list);

            // Get the separator and set the title.
            View separator = inflater.inflate(R.layout.travel_hotel_room_detail_row_separator, null);
            ((TextView) separator.findViewById(R.id.separator_title)).setText(R.string.general_fees);
            // Add the separator header.
            detailsLayout.addView(separator);

            // Populate the room fees.
            Iterator<HotelRoomFee> roomFeeIter = hotelDetail.fees.iterator();
            while (roomFeeIter.hasNext()) {
                HotelRoomFee roomFee = roomFeeIter.next();
                View roomFeeView = inflater.inflate(R.layout.travel_hotel_room_details_row, null);
                TextView txtView = (TextView) roomFeeView.findViewById(R.id.row_label);
                if (txtView != null) {
                    txtView.setVisibility(View.VISIBLE);
                    txtView.setText(roomFee.feeType);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".configureRoomFees: unable to locate row label!");
                }
                txtView = (TextView) roomFeeView.findViewById(R.id.row_value);
                if (txtView != null) {
                    txtView.setText(roomFee.feeDetail);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".configureRoomFees: unable to locate row value!");
                }
                detailsLayout.addView(roomFeeView);
            }

        }
    }

    /**
     * Configures the hotel room details.
     */
    private void configureDetails() {

        if (hotelDetail.details != null && hotelDetail.details.size() > 0) {

            // Get the layout where we'll be adding the fees section to.
            LayoutInflater inflater = LayoutInflater.from(this);
            LinearLayout detailsLayout = (LinearLayout) findViewById(R.id.hotel_search_result_room_details_list);

            Iterator<com.concur.mobile.core.travel.hotel.data.HotelDetail> detailIter = hotelDetail.details.iterator();
            while (detailIter.hasNext()) {
                com.concur.mobile.core.travel.hotel.data.HotelDetail detail = detailIter.next();

                // Get the separator and set the title.
                View separator = inflater.inflate(R.layout.travel_hotel_room_detail_row_separator, null);
                ((TextView) separator.findViewById(R.id.separator_title)).setText(detail.name);
                // Add the separator header.
                detailsLayout.addView(separator);

                View detailView = inflater.inflate(R.layout.travel_hotel_room_details_row, null);
                TextView txtView = (TextView) detailView.findViewById(R.id.row_value);
                txtView.setText(detail.text);
                detailsLayout.addView(detailView);
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
            LayoutInflater inflater = LayoutInflater.from(HotelSearchRoomDetails.this);
            Gallery hotelImageGallery = (Gallery) inflater.inflate(R.layout.travel_hotel_room_gallery, null);
            hotelImageGallery.setAdapter(new AsyncImageAdapter(HotelSearchRoomDetails.this, hotel.propertyImages));
            hotelImageGallery.setOnItemClickListener(new OnItemClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View,
                 * int, long)
                 */
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(HotelSearchRoomDetails.this, ImageActivity.class);
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

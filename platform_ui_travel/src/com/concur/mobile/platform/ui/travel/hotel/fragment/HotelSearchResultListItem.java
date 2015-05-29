package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.common.util.ViewUtil;
import com.concur.mobile.platform.ui.common.view.ListItem;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.util.Format;

import java.io.Serializable;
import java.net.URI;

/**
 * List item for a hotel
 *
 * @author RatanK
 */
public class HotelSearchResultListItem extends ListItem implements Serializable {

    /**
     * generated version id
     */
    private static final long serialVersionUID = 545199248045235189L;

    private static final String CLS_TAG = HotelSearchResultListItem.class.getSimpleName();

    private Hotel hotel;

    public HotelSearchResultListItem(Hotel hotel) {
        this.hotel = hotel;
    }

    /**
     * Gets the <code>Hotel</code> object backing this list item.
     *
     * @return returns the <code>Hotel</code> object backing this list item.
     */
    public Hotel getHotel() {
        return hotel;
    }

    // needed for the Hotel streaming
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View buildView(final Context context, View convertView, ViewGroup parent) {
        View hotelView = null;
        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            hotelView = inflater.inflate(R.layout.hotel_search_result_row, null);
        } else {
            hotelView = convertView;
        }

        // Set the vendor image, or hide it and set the vendor name.
        ImageView thumbNailImg = (ImageView) hotelView.findViewById(R.id.hotel_image_id);
        if (thumbNailImg != null) {
            thumbNailImg.setVisibility(View.VISIBLE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate image view!");
        }
        if (hotel.imagePairs != null && hotel.imagePairs.size() > 0 && hotel.imagePairs.get(0).thumbnail != null) {

            // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
            // to refresh the appropriate view items once images have been loaded.
            URI uri = URI.create(hotel.imagePairs.get(0).thumbnail);
            listItemTag = uri;
            // Attempt to load the image from the image cache, if not there, then the
            // ImageCache will load it asynchronously and this view will be updated via
            // the ImageCache broadcast receiver available in BaseActivity.
            ImageCache imgCache = ImageCache.getInstance(context);
            Bitmap bitmap = imgCache.getBitmap(uri, null);
            if (bitmap != null) {
                thumbNailImg.setImageBitmap(bitmap);
            }
        } else {
            thumbNailImg.setImageResource(R.drawable.hotel_results_default_image);
        }

        // Set the price with currency symbol
        TextView txtView = (TextView) hotelView.findViewById(R.id.hotel_price);
        if (txtView != null) {
            if (hotel.lowestRate != null) {
                txtView.setVisibility(View.VISIBLE);
                txtView.setText(FormatUtil
                        .formatAmountWithNoDecimals(hotel.lowestRate, context.getResources().getConfiguration().locale,
                                hotel.currencyCode, true, false));
            } else {
                txtView.setVisibility(View.INVISIBLE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate price!");
        }

        // Set the hotel name.
        txtView = (TextView) hotelView.findViewById(R.id.hotel_name);
        if (txtView != null) {
            if (hotel.name != null) {
                txtView.setText(hotel.name);
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel name text view!");
        }

        // Set the hotel city.
        if (hotel.contact != null && hotel.contact.city != null) {
            if (ViewUtil.isMappingAvailable(context)) {
                ViewUtil.setText(hotelView, R.id.hotel_address, hotel.contact.city, Linkify.MAP_ADDRESSES);
                ViewUtil.setVisibility(hotelView, R.id.hotel_address, View.VISIBLE);
            } else {
                ((TextView) hotelView.findViewById(R.id.hotel_address)).setText(hotel.contact.city);
            }
        } else {
            ViewUtil.setVisibility(hotelView, R.id.hotel_address, View.GONE);
        }

        // Set the distance and distance unit.
        txtView = (TextView) hotelView.findViewById(R.id.hotel_distance);
        if (txtView != null) {
            if (hotel.distance != null && hotel.distanceUnit != null) {
                if (hotel.distanceUnit.equalsIgnoreCase("M")) {
                    txtView.setText(Format.localizeText(context, R.string.general_number_of_miles, hotel.distance));
                } else {
                    txtView.setText(Format.localizeText(context, R.string.general_number_of_km, hotel.distance));
                }
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel distance text view!");
        }

        // set the travel points
        if (hotel.travelPointsForLowestRate != null && hotel.travelPointsForLowestRate != 0) {
            txtView = ((TextView) hotelView.findViewById(R.id.travel_points_text));
            if (hotel.travelPointsForLowestRate < 0) {
                txtView.setTextAppearance(context, R.style.TravelPointsNegativeText);
                txtView.setText(Format.localizeText(context, R.string.travel_points_can_be_redeemed, new Object[] {
                        FormatUtil.formatAmountWithNoDecimals(hotel.travelPointsForLowestRate,
                                context.getResources().getConfiguration().locale, hotel.currencyCode, false, false) }));
            } else {
                txtView.setTextAppearance(context, R.style.TravelPointsPositiveText);
                txtView.setText(Format.localizeText(context, R.string.travel_points_can_be_earned, new Object[] {
                        FormatUtil.formatAmountWithNoDecimals(hotel.travelPointsForLowestRate,
                                context.getResources().getConfiguration().locale, hotel.currencyCode, false, false) }));
            }
        }

        // Set the recommendation.
        hotelView.setAlpha(1);
        txtView = null;
        txtView = ((TextView) hotelView.findViewById(R.id.preference_text));
        if (txtView != null) {
            // check if property not available
            String availabilityErrorCode = hotel.availabilityErrorCode;
            if (availabilityErrorCode != null && availabilityErrorCode.trim().length() > 0) {
                txtView.setVisibility(View.VISIBLE);
                if (availabilityErrorCode.equalsIgnoreCase("PropertyNotAvailable")) {
                    txtView.setText(R.string.general_sold_out);
                } else {
                    // unknown error code
                    txtView.setText(R.string.general_not_available);

                }

                txtView.setBackground(context.getResources().getDrawable(R.drawable.strong_red_rectangle));
                txtView.setTextColor(Color.parseColor("#d25533"));
                hotelView.setAlpha(0.5f);// 50% transparent

            } else {
                // Set the company preference
                final int resourceId = com.concur.mobile.platform.ui.travel.util.ViewUtil
                        .getHotelCompanyPreferredTextId(hotel.preferences);

                if (resourceId == -1) {
                    txtView.setVisibility(View.GONE);
                } else {
                    txtView.setVisibility(View.VISIBLE);
                    txtView.setBackground(context.getResources().getDrawable(R.drawable.hotel_preferred_rectangle));
                    txtView.setTextColor(Color.parseColor("#ffffff"));
                    txtView.setText(R.string.hotel_preferred);
                    txtView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        // Set the star rating
        ImageView starsImg = ((ImageView) hotelView.findViewById(R.id.recom_stars));
        if (starsImg != null) {
            if (hotel.preferences == null || hotel.preferences.starRating == null
                    || hotel.preferences.starRating.trim().length() == 0) {
                starsImg.setVisibility(View.GONE);
            } else {
                int starImgResId = -1;
                int starRating = Integer.parseInt(hotel.preferences.starRating);
                if (starRating == 1) {
                    starImgResId = R.drawable.icon_cell_stars_1;
                } else if (starRating == 2) {
                    starImgResId = R.drawable.icon_cell_stars_2;
                } else if (starRating == 3) {
                    starImgResId = R.drawable.icon_cell_stars_3;
                } else if (starRating == 4) {
                    starImgResId = R.drawable.icon_cell_stars_4;
                } else if (starRating == 5) {
                    starImgResId = R.drawable.icon_cell_stars_5;
                }
                if (starImgResId != -1) {
                    starsImg.setImageResource(starImgResId);
                    starsImg.setVisibility(View.VISIBLE);
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".getView: invalid star rating of '" + starRating + "...hiding stars.");
                    starsImg.setVisibility(View.GONE);
                }
            }
        }

        // Set the suggestion
        txtView = (TextView) hotelView.findViewById(R.id.recom_text);
        if (txtView != null) {
            int resourceId = com.concur.mobile.platform.ui.travel.util.ViewUtil
                    .getHotelSuggestionTextId(hotel.recommended);

            if (resourceId == -1) {
                txtView.setVisibility(View.GONE);
            } else {
                txtView.setVisibility(View.VISIBLE);
                txtView.setText(resourceId);
            }
        }

        return hotelView;
    }
}
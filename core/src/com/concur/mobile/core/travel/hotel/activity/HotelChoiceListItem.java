/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.net.URI;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.travel.hotel.data.HotelRecommendation.RecommendationSourceEnum;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ImageCache;
import com.concur.mobile.core.util.LayoutUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>ListItem</code> for displaying a <code>HotelChoice</code> item.
 */
public class HotelChoiceListItem extends ListItem {

    private static final String CLS_TAG = HotelChoiceListItem.class.getSimpleName();

    private HotelChoice hotelChoice;

    /**
     * Constructs an instance of <code>HotelChoiceListItem</code> given a hotel choice.
     * 
     * @param hotelChoice
     *            contains the hotel choice.
     */
    public HotelChoiceListItem(HotelChoice hotelChoice) {
        this.hotelChoice = hotelChoice;
    }

    /**
     * Gets the <code>HotelChoice</code> object backing this list item.
     * 
     * @return returns the <code>HotelChoice</code> object backing this list item.
     */
    public HotelChoice getHotelChoice() {
        return hotelChoice;
    }

    // needed for the Hotel streaming
    public void setHotelChoice(HotelChoice hotelChoice) {
        this.hotelChoice = hotelChoice;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        View hotelView = null;
        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            hotelView = inflater.inflate(R.layout.travel_hotel_search_results_row, null);
        } else {
            hotelView = convertView;
        }

        // Set the vendor image, or hide it and set the vendor name.
        if (hotelChoice.propertyURI != null) {

            ImageView logoImg = (ImageView) hotelView.findViewById(R.id.hotelResultVendorImage);
            if (logoImg != null) {
                // Grab the server address
                String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());

                // Build our URI
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(serverAdd);
                strBldr.append(hotelChoice.propertyURI);

                // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
                // to refresh the appropriate view items once images have been loaded.
                URI uri = URI.create(strBldr.toString());
                listItemTag = uri;
                // Attempt to load the image from the image cache, if not there, then the
                // ImageCache will load it asynchronously and this view will be updated via
                // the ImageCache broadcast receiver available in BaseActivity.
                ImageCache imgCache = ImageCache.getInstance(context);
                Bitmap bitmap = imgCache.getBitmap(uri, null);
                if (bitmap != null) {
                    logoImg.setImageBitmap(bitmap);
                    logoImg.setVisibility(View.VISIBLE);
                } else {
                    // Since the bitmap isn't available at the moment, set the visibility to 'INVISIBLE' so that
                    // the client is not showing a previously loaded image for a different carrier!
                    logoImg.setVisibility(View.INVISIBLE);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate image view!");
            }
        } else {
            // Hide the vendor property.
            ImageView logoImg = (ImageView) hotelView.findViewById(R.id.hotelResultVendorImage);
            if (logoImg != null) {
                logoImg.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate image view!");
            }
            // Set the chain name.
            TextView txtView = (TextView) hotelView.findViewById(R.id.hotelResultChainName);
            if (txtView != null) {
                if (hotelChoice.chainName != null) {
                    txtView.setText(hotelChoice.chainName);
                } else {
                    txtView.setText("");
                }
                txtView.setVisibility(View.VISIBLE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel chain name text view!");
            }
        }

        // Set the star rating.
        int starRating = 0;
        if (hotelChoice.starRating != null && hotelChoice.starRating.length() > 0) {
            try {
                starRating = Integer.parseInt(hotelChoice.starRating);
            } catch (NumberFormatException numFormExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: non-integral star rating of '" + hotelChoice.starRating
                        + "'.");
            }
        }
        ImageView imgView = (ImageView) hotelView.findViewById(R.id.star_rating);
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
                    imgView.setVisibility(View.VISIBLE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: invalid star rating of '" + starRating
                            + "...hiding stars.");
                    imgView.setVisibility(View.GONE);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'star_rating' in hotelView!");
        }

        // Set cheapest room rate.
        TextView txtView = (TextView) hotelView.findViewById(R.id.hotelResultCheapestRate);
        if (txtView != null) {
            Integer travelPoints = null;
            Locale loc = context.getResources().getConfiguration().locale;
            if (hotelChoice.cheapestRoom != null) {
                Double cheapestRoomRate = Parse.safeParseDouble(hotelChoice.cheapestRoom.rate);
                if (cheapestRoomRate != null) {
                    txtView.setText(FormatUtil.formatAmount(cheapestRoomRate, loc, hotelChoice.cheapestRoom.crnCode,
                            true));
                    travelPoints = hotelChoice.cheapestRoom.travelPoints;
                }
            } else if (hotelChoice.cheapestRoomWithViolation != null) {
                Double cheapestRoomRate = Parse.safeParseDouble(hotelChoice.cheapestRoomWithViolation.rate);
                if (cheapestRoomRate != null) {
                    txtView.setText(FormatUtil.formatAmount(cheapestRoomRate, loc,
                            hotelChoice.cheapestRoomWithViolation.crnCode, true));
                    travelPoints = hotelChoice.cheapestRoomWithViolation.travelPoints;
                }
            } else if (hotelChoice.isSoldOut) {
                txtView.setText(R.string.general_sold_out);
            } else if (hotelChoice.isNoRates) {
                txtView.setText(R.string.general_no_rates);
            } else if (hotelChoice.isAdditional) {
                txtView.setText(R.string.general_view_rates);
            }
            // set the Travel Points - TODO - need to revisit this if condition to display
            LayoutUtil.initTravelPointsAtItemLevel(hotelView, R.id.travel_points, travelPoints);

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel cheapest rate text view!");
        }

        // Set the distance.

        txtView = (TextView) hotelView.findViewById(R.id.hotelResultDistance);
        if (txtView != null) {
            if (hotelChoice.distance != null && hotelChoice.distanceUnit != null) {
                StringBuilder strBldr = new StringBuilder(hotelChoice.distance).append(' ').append(
                        hotelChoice.distanceUnit);
                txtView.setText(strBldr.toString());
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel distance text view!");
        }

        // Set the company preferred rating.
        imgView = (ImageView) hotelView.findViewById(R.id.hotelResultPreferredRank);
        if (imgView != null) {
            txtView = (TextView) hotelView.findViewById(R.id.pref_rank_text);
            if (txtView != null) {
                if (hotelChoice.prefRankI != null) {
                    switch (hotelChoice.prefRankI) {
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
                                + hotelChoice.prefRank + "'.");
                        break;
                    }
                    }
                }
            } else {
                txtView = (TextView) hotelView.findViewById(R.id.pref_rank_text);
                if (txtView != null) {
                    txtView.setText(R.string.general_not_preferred);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'pref_rank_text' in hotel view!");
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel result preferred rank view!");
        }

        // Set the hotel name.
        txtView = (TextView) hotelView.findViewById(R.id.hotelResultHotelName);
        if (txtView != null) {
            if (hotelChoice.hotel != null) {
                txtView.setText(hotelChoice.hotel);
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel name text view!");
        }

        // Set the hotel address.
        String hotelAddress = ViewUtil.getHotelAddress(hotelChoice);
        if (hotelAddress != null && hotelAddress.length() > 0) {
            if (ViewUtil.isMappingAvailable(context)) {
                ViewUtil.setText(hotelView, R.id.hotelAddr, hotelAddress, Linkify.MAP_ADDRESSES);
                ViewUtil.setVisibility(hotelView, R.id.hotelAddr, View.VISIBLE);
            } else {
                ((TextView) hotelView.findViewById(R.id.hotelAddr)).setText(hotelAddress);
            }
        } else {
            ViewUtil.setVisibility(hotelView, R.id.hotelAddr, View.GONE);
        }

        // Set the hotel phone.
        if (hotelChoice.phone != null && hotelChoice.phone.length() > 0) {
            txtView = ViewUtil.setText(hotelView, R.id.hotelPhone, hotelChoice.phone, Linkify.PHONE_NUMBERS);
            ViewUtil.setVisibility(hotelView, R.id.hotelPhone, View.VISIBLE);
        } else if (hotelChoice.tollFree != null && hotelChoice.tollFree.length() > 0
                && !hotelChoice.tollFree.equalsIgnoreCase("N-A")) {
            txtView = ViewUtil.setText(hotelView, R.id.hotelPhone, hotelChoice.tollFree, Linkify.PHONE_NUMBERS);
            ViewUtil.setVisibility(hotelView, R.id.hotelPhone, View.VISIBLE);
        } else {
            ViewUtil.setVisibility(hotelView, R.id.hotelPhone, View.GONE);
        }

        // Set the recommendation. can we move these statements into a common place since other hotel activities are duplicating
        // these statements
        String recomLocalizedText = HotelChoiceListItem.getRecommendationTextId(hotelChoice.recommendationSource,
                hotelChoice.recommendationSourceNumber);
        TextView recomTextView = (TextView) hotelView.findViewById(R.id.recom_text);
        if (recomLocalizedText == null) {
            recomTextView.setVisibility(View.GONE);
        } else {
            recomTextView.setVisibility(View.VISIBLE);
            recomTextView.setText(recomLocalizedText);
        }

        return hotelView;
    }

    // utility method to be used by other Hotel related activities involved in the booking workflow
    public static String getRecommendationTextId(RecommendationSourceEnum recommendationSource,
            long recommendationSourceNumber) {
        String recomLocalizedText = null;
        int recomTextId = -1;
        if (recommendationSource != null) {
            boolean useNumberString = (recommendationSourceNumber > 1 ? true : false);
            switch (recommendationSource) {
            case CompanyFavorite:
                recomTextId = (useNumberString) ? R.string.hotel_num_of_colleagues_favorite
                        : R.string.hotel_colleague_favorite;
                break;
            case CompanyPreferred:
                recomTextId = R.string.hotel_company_preferred;
                break;
            case CompanyStay:
                recomTextId = (useNumberString) ? R.string.hotel_num_of_colleagues_stayed
                        : R.string.hotel_colleague_stayed;
                break;
            case ItemRecommendation:
                recomTextId = R.string.hotel_item_recommendation;
                break;
            case MeetingRecommendation:
                recomTextId = (useNumberString) ? R.string.hotel_num_of_meeting_recommendation
                        : R.string.hotel_meeting_recommendation;
                break;
            case UserFavorite:
                recomTextId = R.string.hotel_user_favorite;
                break;
            case UserStay:
                recomTextId = (useNumberString) ? R.string.hotel_num_of_times_user_stayed : R.string.hotel_user_stayed;
                break;
            default:
                Log.e(Const.LOG_TAG, "Hotel getRecommendationTextId : unhandled hotel recommendation value of '"
                        + recommendationSource.toString() + "'.");
                recomTextId = R.string.hotel_recommended;
                break;
            }
            if (recomTextId != -1) {
                recomLocalizedText = com.concur.mobile.base.util.Format.localizeText(ConcurCore.getContext(),
                        recomTextId, new Object[] { recommendationSourceNumber });
            }
        }
        return recomLocalizedText;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}

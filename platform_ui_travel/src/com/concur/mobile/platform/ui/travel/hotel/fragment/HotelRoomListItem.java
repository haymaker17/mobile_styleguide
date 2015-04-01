/**
 *
 */
package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.view.ListItem;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for displaying a hotel room.
 */
public class HotelRoomListItem extends ListItem {

    private static final String CLS_TAG = HotelRoomListItem.class.getSimpleName();

    private HotelRate hotelRoom;
    public String maxEnforcementLevelString;
    private boolean showGDSName;

    /**
     * Constructs an instance of <code>HotelRoomListItem</code> backed by a hotel room.
     *
     * @param hotelRoom contains the hotel room.
     */
    public HotelRoomListItem(HotelRate hotelRoom, boolean showGDSName) {
        this.hotelRoom = hotelRoom;
        this.showGDSName = showGDSName;
    }

    /**
     * Gets the <code>HotelRoom</code> object backing this list item.
     *
     * @return returns the <code>HotelRoom</code> object backing this list item.
     */
    public HotelRate getHotelRoom() {
        return hotelRoom;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        View roomView = null;

        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            roomView = inflater.inflate(R.layout.hotel_room_row, parent, false);
        } else {
            roomView = convertView;
        }

        // Set the room rate.
        TextView txtView = (TextView) roomView.findViewById(R.id.hotel_room_rate);
        if (txtView != null) {
            Double roomRate = hotelRoom.amount;
            if (roomRate != null) {
                // String formattedAmtStr = FormatUtil.formatAmount(roomRate,
                // context.getResources().getConfiguration().locale, hotelRoom.currency, true, true);
                // txtView.setText(formattedAmtStr);
                txtView.setText(FormatUtil
                        .formatAmountWithNoDecimals(roomRate, context.getResources().getConfiguration().locale,
                                hotelRoom.currency, true, false));
                // set the Travel Points
                // LayoutUtil.initTravelPointsAtItemLevel(roomView, R.id.travel_points, hotelRoom.travelPoints);

                // add the max enforcement level icon to the first row
                int enforcementLevel = hotelRoom.maxEnforcementLevel;
                if (enforcementLevel >= 25 && enforcementLevel <= 30) {
                    ((ImageView) roomView.findViewById(R.id.hotel_room_max_violation_icon))
                            .setImageResource(R.drawable.icon_status_red);
                } else if (enforcementLevel >= 10 && enforcementLevel <= 20) {
                    ((ImageView) roomView.findViewById(R.id.hotel_room_max_violation_icon))
                            .setImageResource(R.drawable.icon_status_yellow);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel room rate text view!");
        }

        // Set the summary information.
        txtView = (TextView) roomView.findViewById(R.id.hotel_room_summary);
        if (txtView != null) {
            if (hotelRoom.description != null && hotelRoom.description.length() > 0) {
                txtView.setText(hotelRoom.description);
                txtView.setVisibility(View.VISIBLE);
            } else {
                txtView.setVisibility(View.GONE);
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate hotel room summary text view!");
        }

        // travel points
        if(hotelRoom.travelPoints != null && hotelRoom.travelPoints != 0.0) {
            txtView = (TextView) roomView.findViewById(R.id.travel_points_text);
            txtView.setVisibility(View.VISIBLE);
            if(hotelRoom.travelPoints < 0.0) {
                txtView.setText(Format.localizeText(context, R.string.travel_points_can_be_redeemed, new Object[] { hotelRoom.travelPoints }));
            } else {
                txtView.setText(Format.localizeText(context, R.string.travel_points_can_be_earned, new Object[] { hotelRoom.travelPoints }));
            }
        }


        txtView = (TextView) roomView.findViewById(R.id.hotel_room_deposit_required);
        if (txtView != null) {
            if (hotelRoom.guaranteeSurcharge != null && hotelRoom.guaranteeSurcharge.equals("DepositRequired")) {
                txtView.setText(R.string.hotel_booking_deposit_required);
                txtView.setVisibility(View.VISIBLE);
            } else {
                txtView.setVisibility(View.GONE);
            }
        }

        if (showGDSName) {
            txtView = (TextView) roomView.findViewById(R.id.hotel_room_gds_name);
            if (txtView != null && hotelRoom.source != null) {
                ViewUtil.showGDSName(context, txtView, hotelRoom.source);
            }
        }
        roomView.setAlpha(1);
        TextView tv = (TextView) roomView.findViewById(R.id.hotel_room_out_of_policy);
        if (hotelRoom.greyFlag) {
            if (tv != null) {
                tv.setVisibility(View.VISIBLE);
            }
            roomView.setAlpha(0.5f); // 50% transparent
        } else {
            tv.setVisibility(View.GONE);
        }
        return roomView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}

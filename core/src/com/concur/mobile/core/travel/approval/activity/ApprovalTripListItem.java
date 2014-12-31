package com.concur.mobile.core.travel.approval.activity;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.travel.data.TripToApprove;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> representing an trip approval.
 */
public class ApprovalTripListItem extends ListItem {

    private TripToApprove tripToApprove;
    private String message;

    public ApprovalTripListItem(String message, int listItemViewType) {
        this.message = message;
        this.listItemViewType = listItemViewType;
    }

    public ApprovalTripListItem(TripToApprove tripToApprove, int listItemViewType) {
        this.tripToApprove = tripToApprove;
        this.listItemViewType = listItemViewType;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View tripView = null;

        if (convertView == null) {
            // Create the main row container and static elements
            LayoutInflater inflater = LayoutInflater.from(context);
            tripView = inflater.inflate(R.layout.trip_approval_row, null);
            // change the background set in the layout to gradient
            tripView.findViewById(R.id.trip_app_row_layout).setBackgroundResource(R.drawable.cell_gradient_background);
        } else {
            tripView = convertView;
        }

        if (tripToApprove == null) {
            ((TextView) tripView.findViewById(R.id.trip_app_row_trip_name)).setText(message);

            // hide the other elements
            ((TextView) tripView.findViewById(R.id.trip_app_row_employee_name)).setVisibility(View.GONE);
            ((TextView) tripView.findViewById(R.id.trip_app_row_cost_amount)).setVisibility(View.GONE);
            ((TextView) tripView.findViewById(R.id.trip_app_row_message)).setVisibility(View.GONE);

        } else {
            // format the amount
            String totalTripCost = "";
            Double tripCost = tripToApprove.getTotalTripCost();
            if (tripCost != null) {
                totalTripCost = FormatUtil.formatAmount(tripCost, context.getResources().getConfiguration().locale,
                        tripToApprove.getTotalTripCostCrnCode(), true, true);
            }

            // format the approve by date message
            String approveDateDisplayStr = "";
            if (DateFormat.is24HourFormat(context)) {
                approveDateDisplayStr = FormatUtil.SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_24HOUR_TIMEZONE_DISPLAY_LOCAL
                        .format(tripToApprove.getApproveByDate().getTime());
            } else {
                approveDateDisplayStr = FormatUtil.SHORT_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR_12HOUR_TIMEZONE_DISPLAY_LOCAL
                        .format(tripToApprove.getApproveByDate().getTime());
            }
            String approvalMsg = Format.localizeText(context, R.string.trip_approve_by,
                    new Object[] { approveDateDisplayStr });

            // Fill in the view elements.
            ((TextView) tripView.findViewById(R.id.trip_app_row_employee_name))
                    .setText(tripToApprove.getTravelerName());
            ((TextView) tripView.findViewById(R.id.trip_app_row_cost_amount)).setText(totalTripCost);
            ((TextView) tripView.findViewById(R.id.trip_app_row_trip_name)).setText(tripToApprove.getTripName());
            ((TextView) tripView.findViewById(R.id.trip_app_row_message)).setText(approvalMsg);
        }

        // If the view was converted, invalidate it and request a layout.
        if (convertView != null) {
            tripView.invalidate();
            tripView.requestLayout();
        }
        return tripView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    public TripToApprove getTripToApprove() {
        return tripToApprove;
    }
}
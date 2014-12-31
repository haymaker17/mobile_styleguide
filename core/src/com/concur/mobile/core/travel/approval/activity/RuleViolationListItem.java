package com.concur.mobile.core.travel.approval.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.CarRuleViolation;
import com.concur.mobile.core.travel.data.FlightRuleViolation;
import com.concur.mobile.core.travel.data.HotelRuleViolation;
import com.concur.mobile.core.travel.data.RailRuleViolation;
import com.concur.mobile.core.travel.data.RuleViolation;
import com.concur.mobile.core.travel.data.RuleViolationReason;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> representing an trip rule violation.
 * 
 * @author RatanK
 * 
 */
public class RuleViolationListItem extends ListItem {

    private RuleViolation ruleViolation;

    public RuleViolationListItem(RuleViolation ruleViolation, int listItemViewType) {
        this.ruleViolation = ruleViolation;
        this.listItemViewType = listItemViewType;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View violationView = inflater.inflate(R.layout.trip_rule_violation_summary, null);

        String rateHeader = "";
        String rateText = "";
        boolean showRate = true;
        switch (ruleViolation.getSegmentType()) {
        case Itinerary:
            // itinerary level rule violations do not have rate
            showRate = false;
            break;
        case Air:
            rateHeader = context.getText(R.string.trip_rule_violation_flight_fare).toString();
            rateText = ((FlightRuleViolation) ruleViolation).getQuotedTotal();
            break;
        case Car:
            rateHeader = context.getText(R.string.trip_rule_violation_car_rate).toString();
            rateText = ((CarRuleViolation) ruleViolation).getDailyRate();
            break;
        case Hotel:
            rateHeader = context.getText(R.string.trip_rule_violation_rail_rate).toString();
            rateText = ((HotelRuleViolation) ruleViolation).getRate();
            break;
        case Rail:
            rateHeader = context.getText(R.string.trip_rule_violation_hotel_rate).toString();
            rateText = ((RailRuleViolation) ruleViolation).getRate();
            break;
        default:
            // unknown segment type
            break;
        }

        // rules
        if (ruleViolation.getRules().size() == 0) {
            hideView(violationView, R.id.segment_text);
        } else {
            String rulesStr = TextUtils.join("\n", ruleViolation.getRules());
            ((TextView) violationView.findViewById(R.id.segment_text)).setText(rulesStr);
        }

        // violation reason and booker comments
        if (ruleViolation.getViolationReasons().size() == 0) {
            hideView(violationView, R.id.violation_reason_header);
            hideView(violationView, R.id.violation_reason_text);
            hideView(violationView, R.id.booker_comments_header);
            hideView(violationView, R.id.booker_comments_text);
        } else {
            ((TextView) violationView.findViewById(R.id.violation_reason_header))
                    .setText(R.string.trip_rule_violation_reason_code);
            ((TextView) violationView.findViewById(R.id.booker_comments_header))
                    .setText(R.string.trip_rule_violation_booker_comments);

            StringBuilder violationReasonStr = new StringBuilder();
            StringBuilder bookerCommentsStr = new StringBuilder();
            for (RuleViolationReason violationReason : ruleViolation.getViolationReasons()) {
                violationReasonStr.append(violationReason.getReasonCode());
                bookerCommentsStr.append(violationReason.getBookerComments());
            }
            ((TextView) violationView.findViewById(R.id.violation_reason_text)).setText(violationReasonStr);

            // if comments not available (as optional), hide the comments header and text
            if (bookerCommentsStr.toString().trim().length() == 0) {
                hideView(violationView, R.id.booker_comments_header);
                hideView(violationView, R.id.booker_comments_text);
            } else {
                ((TextView) violationView.findViewById(R.id.booker_comments_text)).setText(bookerCommentsStr);
            }
        }

        // rates
        if (showRate && rateText != null) {
            ((TextView) violationView.findViewById(R.id.segment_rate_header)).setText(rateHeader);
            ((TextView) violationView.findViewById(R.id.segment_rate_text)).setText(rateText);
        } else {
            hideView(violationView, R.id.segment_rate_header);
            hideView(violationView, R.id.segment_rate_text);
        }

        return violationView;
    }

    private void hideView(View view, int id) {
        view.findViewById(id).setVisibility(View.GONE);
    }
}

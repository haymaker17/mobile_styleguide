package com.concur.mobile.core.travel.rail.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.rail.data.RailChoice;
import com.concur.mobile.core.travel.rail.data.RailChoiceLeg;
import com.concur.mobile.core.travel.rail.data.RailChoiceSegment;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

public class RailSearchResultsFares extends BaseActivity {

    private static final String CLS_TAG = RailSearchResultsFares.class.getSimpleName();

    public static final String KEY_GROUP_ID = "groupid";
    public static final String KEY_BUCKET = "bucket";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.rail_search_results_fares);

        HashMap<String, RailStation> stationMap = ((ConcurCore) getApplication()).getCodeRailStationMap();

        Intent i = getIntent();

        // Set the screen title
        getSupportActionBar().setTitle(R.string.rail_results_fares_title);

        String groupId = i.getStringExtra(RailSearchResults.SELECTED_TRAIN);

        // Set the depart/location and date(s).
        String stationCode = i.getStringExtra(RailSearch.DEP_LOCATION);
        RailStation depLocation = stationMap.get(stationCode);
        stationCode = i.getStringExtra(RailSearch.ARR_LOCATION);
        RailStation arrLocation = stationMap.get(stationCode);

        Calendar depDateTime = (Calendar) i.getSerializableExtra(RailSearch.DEP_DATETIME);
        Calendar retDateTime = null;
        if (i.hasExtra(RailSearch.RET_DATETIME)) {
            retDateTime = (Calendar) i.getSerializableExtra(RailSearch.RET_DATETIME);
        }

        // Set departure/arrival header title.
        String loc = com.concur.mobile.base.util.Format.localizeText(this, R.string.rail_search_label_dep_to_arr_loc,
                new Object[] { depLocation.getName(), arrLocation.getName() });
        ((TextView) findViewById(R.id.travel_name)).setText(loc);

        StringBuilder dates = new StringBuilder(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA,
                depDateTime));
        if (retDateTime != null) {
            dates.append(" - ");
            dates.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, retDateTime));
        }
        // Set departure/arrival dates
        ((TextView) findViewById(R.id.date_span)).setText(dates.toString());

        ConcurCore app = (ConcurCore) getApplication();
        ArrayList<RailChoice> choices = app.getRailSearchResults().choiceMap.get(groupId);

        final RailChoice firstChoice = choices.get(0);
        populateTrain(firstChoice);
        populateFares(choices);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void populateTrain(RailChoice firstChoice) {

        // Get the outbound segment and populate it
        RailChoiceSegment outbound = firstChoice.getOutboundSegment();
        if (outbound.legs.get(0).isBus()) {
            // Change the label
            ((TextView) findViewById(R.id.railResultOutboundEquipmentLabel)).setText(R.string.rail_general_bus);
        }
        ((TextView) findViewById(R.id.railResultOutboundTrain)).setText(outbound.getDepTrainNumber());

        // Force our timezone for formatting to UTC since that's what we use everywhere internally.
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        ((TextView) findViewById(R.id.railResultOutboundDepStation)).setText(outbound.getDepStation());
        ((TextView) findViewById(R.id.railResultOutboundDepTime)).setText(Format.safeFormatCalendar(timeFormat,
                outbound.getDepDateTime()));

        ((TextView) findViewById(R.id.railResultOutboundArrStation)).setText(outbound.getArrStation());
        ((TextView) findViewById(R.id.railResultOutboundArrTime)).setText(Format.safeFormatCalendar(timeFormat,
                outbound.getArrDateTime()));

        StringBuilder elapsed = new StringBuilder("(");
        elapsed.append(outbound.getElapsedTime(this)).append(')');
        ((TextView) findViewById(R.id.railResultOutboundTotalTime)).setText(elapsed.toString());

        // Hide the min/max price.
        findViewById(R.id.railPriceLayout).setVisibility(View.GONE);

        // Get the return segment and populate it
        RailChoiceSegment ret = firstChoice.getReturnSegment();
        if (ret != null) {
            if (ret.legs.get(0).isBus()) {
                // Change the label
                ((TextView) findViewById(R.id.railResultReturnEquipmentLabel)).setText(R.string.rail_general_bus);
            }
            ((TextView) findViewById(R.id.railResultReturnTrain)).setText(ret.getDepTrainNumber());

            ((TextView) findViewById(R.id.railResultReturnDepStation)).setText(ret.getDepStation());
            ((TextView) findViewById(R.id.railResultReturnDepTime)).setText(Format.safeFormatCalendar(timeFormat,
                    ret.getDepDateTime()));

            ((TextView) findViewById(R.id.railResultReturnArrStation)).setText(ret.getArrStation());
            ((TextView) findViewById(R.id.railResultReturnArrTime)).setText(Format.safeFormatCalendar(timeFormat,
                    ret.getArrDateTime()));

            elapsed = new StringBuilder("(");
            elapsed.append(ret.getElapsedTime(this)).append(')');
            ((TextView) findViewById(R.id.railResultReturnTotalTime)).setText(elapsed.toString());
        } else {
            ((TextView) findViewById(R.id.railResultReturnEquipmentLabel)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.railResultReturnTrain)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.railResultReturnDepStation)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.railResultReturnDepTime)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.railResultReturnArrow)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.railResultReturnArrStation)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.railResultReturnArrTime)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.railResultReturnTotalTime)).setVisibility(View.GONE);
        }

    }

    protected void populateFares(ArrayList<RailChoice> choices) {

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

        // Get the fare layout and populate it
        LinearLayout departureLegLayout = (LinearLayout) findViewById(R.id.railSearchResultsFaresLayout);

        int count = 0;
        for (final RailChoice choice : choices) {

            RailChoiceSegment outboundSeg = choice.getOutboundSegment();
            RailChoiceSegment returnSeg = choice.getReturnSegment();

            // Work out our seat class strings
            StringBuffer outboundClass = new StringBuffer();
            ArrayList<RailChoiceLeg> legs = outboundSeg.legs;
            for (int i = 0; i < legs.size(); i++) {
                if (i > 0) {
                    outboundClass.append(" / ");
                }
                outboundClass.append(legs.get(i).seatClassName);
            }

            StringBuffer returnClass = null;
            if (returnSeg != null) {
                returnClass = new StringBuffer();
                legs = returnSeg.legs;
                for (int i = 0; i < legs.size(); i++) {
                    if (i > 0) {
                        returnClass.append(" / ");
                    }
                    returnClass.append(legs.get(i).seatClassName);
                }
            }

            View choiceView = inflater.inflate(R.layout.rail_search_results_fares_row, null);

            Locale loc = RailSearchResultsFares.this.getResources().getConfiguration().locale;
            double cost = choice.cost == null ? 0.0 : choice.cost;
            TextView txtView = (TextView) choiceView.findViewById(R.id.railResultCost);
            (txtView).setText(FormatUtil.formatAmount(cost, loc, choice.currency, true));

            switch (ViewUtil.getRuleEnforcementLevel(choice.maxEnforcementLevel)) {
            case NONE: {
                txtView.setTextAppearance(RailSearchResultsFares.this, R.style.FareNormal);
                break;
            }
            case WARNING: {
                txtView.setTextAppearance(RailSearchResultsFares.this, R.style.FareWarning);
                break;
            }
            case ERROR: {
                txtView.setTextAppearance(RailSearchResultsFares.this, R.style.FareError);
                break;
            }
            case INACTIVE: {
                txtView.setTextAppearance(RailSearchResultsFares.this, R.style.FareInactive);
                break;
            }
            case HIDE: {
                // No-op.
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateFares: rule enforcement level of hide!");
                break;
            }
            }

            // Outbound route text
            ((TextView) choiceView.findViewById(R.id.railResultOutboundDepStation))
                    .setText(outboundSeg.getDepStation());
            ((TextView) choiceView.findViewById(R.id.railResultOutboundArrStation))
                    .setText(outboundSeg.getArrStation());

            // Outbound class text
            ((TextView) choiceView.findViewById(R.id.railSearchResultsFaresOutboundClass)).setText(outboundClass
                    .toString());

            if (returnSeg != null) {
                // Return route text
                ((TextView) choiceView.findViewById(R.id.railResultReturnDepStation))
                        .setText(returnSeg.getDepStation());
                ((TextView) choiceView.findViewById(R.id.railResultReturnArrStation))
                        .setText(returnSeg.getArrStation());

                // Return class text
                ((TextView) choiceView.findViewById(R.id.railSearchResultsFaresReturnClass)).setText(returnClass
                        .toString());
            } else {
                // Hide it all
                choiceView.findViewById(R.id.railResultReturnDepStation).setVisibility(View.GONE);
                choiceView.findViewById(R.id.railResultReturnArrow).setVisibility(View.GONE);
                choiceView.findViewById(R.id.railResultReturnArrStation).setVisibility(View.GONE);
                choiceView.findViewById(R.id.railSearchResultsFaresReturnClass).setVisibility(View.GONE);
            }

            // Add listener to the fare entry to launch the details screen.
            choiceView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    launchDetails(choice);
                }
            });
            departureLegLayout.addView(choiceView);

            // Don't add more than 50 choices for performance reasons.
            if (++count > 50) {
                break;
            }

        } // end for-loop

    } // end populateFares()

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        switch (id) {
        default: {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            dlg = ConcurCore.createDialog(this, id);
            break;
        }
        }
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // Propagate back
            setResult(resultCode, data);
            finish();
        }
    }

    protected void launchDetails(RailChoice choice) {

        // MOB-14778 - if max enforcement level is 40 then show message and do not allow for reserve
        Violation maxEnforcementViolation = ViewUtil.getShowButNoBookingViolation(choice.violations,
                choice.maxEnforcementLevel);
        if (maxEnforcementViolation != null) {
            showReserveNotAllowed(maxEnforcementViolation.message);
        } else {

            Intent intent = getResultantIntent();

            Intent callingIntent = getIntent();

            intent.putExtra(RailSearch.DEP_LOCATION, callingIntent.getStringExtra(RailSearch.DEP_LOCATION));
            intent.putExtra(RailSearch.ARR_LOCATION, callingIntent.getStringExtra(RailSearch.ARR_LOCATION));
            intent.putExtra(RailSearch.DEP_DATETIME, callingIntent.getSerializableExtra(RailSearch.DEP_DATETIME));
            if (callingIntent.hasExtra(RailSearch.RET_DATETIME)) {
                intent.putExtra(RailSearch.RET_DATETIME, callingIntent.getSerializableExtra(RailSearch.RET_DATETIME));
            }

            intent.putExtra(KEY_GROUP_ID, choice.groupId);
            intent.putExtra(KEY_BUCKET, choice.bucket);

            if (callingIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                        callingIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
            }

            startActivityForResult(intent, 0);
        }
    }

    /**
     * get detail intent
     * **/
    protected Intent getResultantIntent() {
        return new Intent(this, RailSearchDetail.class);
    }

}

package com.concur.mobile.core.travel.rail.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.rail.data.RailChoice;
import com.concur.mobile.core.travel.rail.data.RailChoiceSegment;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.rail.service.RailSearchReply;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

public class RailSearchResults extends ActionBarActivity {

    public static final String SELECTED_TRAIN = "train_group_id";
    private ListView m_lstvRailSearchResultsList = null;
    private OnItemClickListener mOnItemClickListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rail_search_results);

        setupListeners();
        m_lstvRailSearchResultsList = (ListView) findViewById(android.R.id.list);

        HashMap<String, RailStation> stationMap = ((ConcurCore) getApplication()).getCodeRailStationMap();

        Intent i = getIntent();

        // Set the screen title
        getSupportActionBar().setTitle(R.string.rail_results_title);

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

        RailSearchReply results = ((ConcurCore) getApplication()).getRailSearchResults();
        int count = 0;
        if (results != null) {
            RailChoiceAdapter tla = new RailChoiceAdapter(this, results.choiceMap);
            m_lstvRailSearchResultsList.setAdapter(tla);
            m_lstvRailSearchResultsList.setOnItemClickListener(mOnItemClickListener);
            count = results.choiceMap.size();
        }

        // Set the total result count.
        String countStr = "";
        if (count > 1) {
            countStr = com.concur.mobile.base.util.Format.localizeText(this, R.string.generic_results_choice_count,
                    new Object[] { count });
        } else {
            countStr = getText(R.string.generic_results_one_count).toString();
        }
        ((TextView) findViewById(R.id.footer_navigation_bar_status)).setText(countStr);
    }

    private class RailChoiceAdapter extends BaseAdapter {

        private final Context context;
        private final LinkedHashMap<String, ArrayList<RailChoice>> choiceMap;
        private final String[] choiceKeys;

        public RailChoiceAdapter(Context context, LinkedHashMap<String, ArrayList<RailChoice>> choiceMap) {
            this.context = context;
            this.choiceMap = choiceMap;
            this.choiceKeys = choiceMap.keySet().toArray(new String[1]);
        }

        @Override
        public int getCount() {
            return choiceMap.size();
        }

        @Override
        public ArrayList<RailChoice> getItem(int position) {
            return choiceMap.get(choiceKeys[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View choiceView = null;

            // Either convert an existing view or create a new one.
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                choiceView = inflater.inflate(R.layout.rail_search_results_row, null);
            } else {
                choiceView = convertView;
            }

            // Get out list of choices for parsing and such.
            // This code assumes that choices are in lowest to highest price
            // order. The server
            // returns them that way. If that changes then this will break.
            ArrayList<RailChoice> choices = getItem(position);
            RailChoice firstChoice = choices.get(0);
            RailChoice lastChoice = null;
            if (choices.size() > 1) {
                lastChoice = choices.get(choices.size() - 1);
            }

            Locale loc = RailSearchResults.this.getResources().getConfiguration().locale;
            double cost = firstChoice.cost == null ? 0.0 : firstChoice.cost;
            ((TextView) choiceView.findViewById(R.id.railResultCostMin)).setText(FormatUtil.formatAmount(cost, loc,
                    firstChoice.currency, true));

            if (lastChoice == null) {
                // Hide the multi-choice elements
                ViewUtil.setVisibility(choiceView, R.id.railResultCostSep, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultCostMax, View.GONE);
            } else {
                cost = lastChoice.cost == null ? 0.0 : lastChoice.cost;
                ((TextView) choiceView.findViewById(R.id.railResultCostMax)).setText(FormatUtil.formatAmount(cost, loc,
                        lastChoice.currency, true));
                // Show the multi-choice elements
                ViewUtil.setVisibility(choiceView, R.id.railResultCostSep, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultCostMax, View.VISIBLE);
            }

            // Get the outbound segment and populate it
            RailChoiceSegment outbound = firstChoice.getOutboundSegment();
            if (outbound.legs.get(0).isBus()) {
                // Set the label to general bus.
                ((TextView) choiceView.findViewById(R.id.railResultOutboundEquipmentLabel))
                        .setText(R.string.rail_general_bus);
            } else {
                // Set the label to general train.
                ((TextView) choiceView.findViewById(R.id.railResultOutboundEquipmentLabel))
                        .setText(R.string.rail_general_train);
            }
            ((TextView) choiceView.findViewById(R.id.railResultOutboundTrain)).setText(outbound.getDepTrainNumber());

            // Force our timezone for formatting to UTC since that's what we use
            // everywhere internally.
            java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            ((TextView) choiceView.findViewById(R.id.railResultOutboundDepStation)).setText(outbound.getDepStation());
            ((TextView) choiceView.findViewById(R.id.railResultOutboundDepTime)).setText(Format.safeFormatCalendar(
                    timeFormat, outbound.getDepDateTime()));

            ((TextView) choiceView.findViewById(R.id.railResultOutboundArrStation)).setText(outbound.getArrStation());
            ((TextView) choiceView.findViewById(R.id.railResultOutboundArrTime)).setText(Format.safeFormatCalendar(
                    timeFormat, outbound.getArrDateTime()));

            StringBuilder elapsed = new StringBuilder("(");
            elapsed.append(outbound.getElapsedTime(context)).append(')');
            ((TextView) choiceView.findViewById(R.id.railResultOutboundTotalTime)).setText(elapsed.toString());

            if (outbound.hasAcela()) {
                ViewUtil.setVisibility(choiceView, R.id.railResultOutboundAcela, View.VISIBLE);
            } else {
                ViewUtil.setVisibility(choiceView, R.id.railResultOutboundAcela, View.GONE);
            }

            // Get the return segment and populate it
            RailChoiceSegment ret = firstChoice.getReturnSegment();
            if (ret != null) {

                if (ret.legs.get(0).isBus()) {
                    // Set the label to general bus.
                    ((TextView) choiceView.findViewById(R.id.railResultReturnEquipmentLabel))
                            .setText(R.string.rail_general_bus);
                } else {
                    // Set the label to general bus.
                    ((TextView) choiceView.findViewById(R.id.railResultReturnEquipmentLabel))
                            .setText(R.string.rail_general_train);
                }
                ((TextView) choiceView.findViewById(R.id.railResultReturnTrain)).setText(ret.getDepTrainNumber());

                ((TextView) choiceView.findViewById(R.id.railResultReturnDepStation)).setText(ret.getDepStation());
                ((TextView) choiceView.findViewById(R.id.railResultReturnDepTime)).setText(Format.safeFormatCalendar(
                        timeFormat, ret.getDepDateTime()));

                ((TextView) choiceView.findViewById(R.id.railResultReturnArrStation)).setText(ret.getArrStation());
                ((TextView) choiceView.findViewById(R.id.railResultReturnArrTime)).setText(Format.safeFormatCalendar(
                        timeFormat, ret.getArrDateTime()));

                elapsed = new StringBuilder("(");
                elapsed.append(ret.getElapsedTime(context)).append(')');
                ((TextView) choiceView.findViewById(R.id.railResultReturnTotalTime)).setText(elapsed.toString());

                // Show return views.
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnEquipmentLabel, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnTrain, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnDepStation, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnDepTime, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnArrow, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnArrStation, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnArrTime, View.VISIBLE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnTotalTime, View.VISIBLE);

                if (ret.hasAcela()) {
                    ViewUtil.setVisibility(choiceView, R.id.railResultReturnAcela, View.VISIBLE);
                } else {
                    ViewUtil.setVisibility(choiceView, R.id.railResultReturnAcela, View.GONE);
                }
            } else {
                // Hide return views.
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnEquipmentLabel, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnTrain, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnDepStation, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnDepTime, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnArrow, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnArrStation, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnArrTime, View.GONE);
                ViewUtil.setVisibility(choiceView, R.id.railResultReturnTotalTime, View.GONE);
            }
            // show GDSName only for DEV & QA env.
            TextView gdsNameView = (TextView) choiceView.findViewById(R.id.railGDSName);
            if (gdsNameView != null) {
                ViewUtil.showGDSName(context, gdsNameView, firstChoice.gdsName);
            }

            return choiceView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // Propagate back
            setResult(resultCode, data);
            finish();
        }
    }

    /**
     * Calling intent when you click on item.
     * **/
    protected Intent getResultIntent() {
        return new Intent(this, RailSearchResultsFares.class);
    }

    private void setupListeners() {
        mOnItemClickListener = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ArrayList<RailChoice> choices = ((RailChoiceAdapter) m_lstvRailSearchResultsList.getAdapter())
                        .getItem(position);

                Intent intent = getResultIntent();
                intent.putExtra(SELECTED_TRAIN, choices.get(0).groupId);

                Intent callingIntent = getIntent();
                intent.putExtra(RailSearch.DEP_LOCATION, callingIntent.getStringExtra(RailSearch.DEP_LOCATION));
                intent.putExtra(RailSearch.ARR_LOCATION, callingIntent.getStringExtra(RailSearch.ARR_LOCATION));
                intent.putExtra(RailSearch.DEP_DATETIME, callingIntent.getSerializableExtra(RailSearch.DEP_DATETIME));
                if (callingIntent.hasExtra(RailSearch.RET_DATETIME)) {
                    intent.putExtra(RailSearch.RET_DATETIME,
                            callingIntent.getSerializableExtra(RailSearch.RET_DATETIME));
                }
                if (callingIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                    intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                            callingIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                }

                startActivityForResult(intent, 0);
            }
        };
    }
}

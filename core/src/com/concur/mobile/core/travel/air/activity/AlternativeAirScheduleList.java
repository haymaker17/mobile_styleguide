/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.air.service.AlternativeAirScheduleReply;
import com.concur.mobile.core.travel.data.SegmentOption;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItemAdapter;

/**
 * An extension of <code>BaseActivity</code> for the purpose of displaying a list of air search results. <br>
 * <b>NOTE:</b><br>
 * This activity uses the latest instance of <code>AirFilterResults</code> associated with the application object.
 */
public class AlternativeAirScheduleList extends BaseActivity {

    private static final String CLS_TAG = AlternativeAirScheduleList.class.getSimpleName();
    private ListView listView;
    private TextView errorView;
    private ListItemAdapter<AlternativeFlightScheduleListItem> airChoiceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_search_alternative_results);

        // initValues(savedInstanceState);
        initUI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void initUI() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            AlternativeAirScheduleReply reply = getConcurCore().getAlternativeFlightSchedules();
            if (reply != null) {
                // Init the screen header.
                initScreenHeader();

                // Init the travel header.
                initTravelHeader(reply, bundle);

                // Init the list.
                initResultsList(reply);

            } else {
                finish();
                // Finish the activity since there are no filter results!
                setResult(Activity.RESULT_CANCELED);
            }
        } else {
            // Finish the activity since there is no bundle data!
            finish();
            setResult(Activity.RESULT_CANCELED);
        }

    }

    /**
     * Will initialize the results list.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initResultsList(AlternativeAirScheduleReply filterReply) {

        listView = (ListView) findViewById(R.id.alternative_list_view);
        errorView = (TextView) findViewById(R.id.alternative_nodata);
        if (listView != null && errorView != null) {
            List<AlternativeFlightScheduleListItem> airChoiceListItems = null;
            List<SegmentOption> list = filterReply.listofSegmentOptions;
            if (list != null && list.size() > 0) {
                listView.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
                airChoiceListItems = new ArrayList<AlternativeFlightScheduleListItem>(list.size());
                for (SegmentOption airChoice : list) {
                    airChoiceListItems.add(new AlternativeFlightScheduleListItem(airChoice));
                }
                airChoiceAdapter = new ListItemAdapter<AlternativeFlightScheduleListItem>(this, airChoiceListItems);
                listView.setAdapter(airChoiceAdapter);
            } else {
                listView.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initResultsList: unable to locate list view and/or errorView!");
        }
    }

    /**
     * Will initialize the travel header with from/to information.
     * 
     * @param reply
     *            the filter reply.
     */
    protected void initTravelHeader(AlternativeAirScheduleReply reply, Bundle bundle) {
        final String departcityName = bundle.getString(AirSegmentDetail.START_CITY_NAME);
        final String arrivaleCityName = bundle.getString(AirSegmentDetail.END_CITY_NAME);
        final String departCityCode = bundle.getString(AirSegmentDetail.START_CITY_CODE);
        final String arrivaleCityCode = bundle.getString(AirSegmentDetail.END_CITY_CODE);

        StringBuilder sb = new StringBuilder();
        final String departIATACode = sb.append("(").append(departCityCode).append(")").append(departcityName)
                .toString();
        sb.setLength(0);

        final String arriveIATACode = sb.append("(").append(arrivaleCityCode).append(")").append(arrivaleCityName)
                .toString();

        // The travel header
        TextView tv = (TextView) findViewById(R.id.travel_name);
        tv.setText(Format.localizeText(this, R.string.segmentlist_air_fromto, new Object[] { departIATACode,
                arriveIATACode }));

        Calendar departDateTime = (Calendar) bundle.getSerializable(AirSegmentDetail.DEPART_DATE);
        tv = (TextView) findViewById(R.id.date_span);
        sb.setLength(0);
        if (departDateTime != null) {
            sb.append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(departDateTime.getTime()));
            tv.setText(sb.toString());
        } else {
            tv.setText("");
        }
    }

    /**
     * Will initialize the screen header.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initScreenHeader() {
        getSupportActionBar().setTitle(R.string.segment_air_label_flight_schedule);
    }

}

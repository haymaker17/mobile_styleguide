package com.concur.mobile.core.travel.air.fragment;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.travel.air.activity.AirPriceToBeatSearch;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.widget.CalendarPicker;
import com.concur.mobile.core.widget.CalendarPickerDialog;
import com.concur.mobile.platform.util.Format;

/**
 * Fragment to display the Air Price to Beat search
 * 
 * @author RatanK
 * 
 */
public class AirPriceToBeatSearchFragment extends BaseFragment implements View.OnClickListener {

    private static final String CLS_TAG = AirPriceToBeatSearchFragment.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = AirPriceToBeatSearchFragment.class.getSimpleName()
            + ".calendar.dialog.fragment";

    protected static final String ON_CLICK_LISTENER_KEY = "on.click.listener";
    private static final String MODE_KEY = "mode";

    private static final String STATE_DEPART_LOC_KEY = "depart_loc";
    private static final String STATE_ARRIVE_LOC_KEY = "arrive_loc";
    private static final String STATE_DEPART_DT_KEY = "depart_datetime";

    private static final String IS_CALENDAR_VISIBLE_KEY = "is.calendar.visible";

    public static enum SearchMode {
        None, OneWay, RoundTrip, MultiSegment
    };

    private SearchMode searchMode;
    private LocationChoice departLocation;
    private LocationChoice arriveLocation;
    private Calendar departDate;

    private Button modeOneWay;
    private Button modeRoundTrip;
    private Button searchButton;

    private View root;

    private AirPriceToBeatSearch baseActivity;

    private PriceToBeatSearchFragmentOnClickListener listener;
    private CalendarPickerDialog calendarDialog;
    private boolean roundTrip;

    private String headerTxt;

    public void setHeaderTxt(String headerTxt) {
        this.headerTxt = headerTxt;
    }

    public LocationChoice getDepartLocation() {
        return departLocation;
    }

    public void setDepartLocation(LocationChoice departLocation) {
        this.departLocation = departLocation;
    }

    public LocationChoice getArriveLocation() {
        return arriveLocation;
    }

    public void setArriveLocation(LocationChoice arriveLocation) {
        this.arriveLocation = arriveLocation;
    }

    public Calendar getDepartDate() {
        return departDate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState == null) {
            searchMode = SearchMode.None;
            Calendar now = Calendar.getInstance();
            departDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            departDate.clear();
            departDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 9, 0, 0);
            departDate.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            String mode = savedInstanceState.getString(MODE_KEY);
            if (mode != null) {
                searchMode = SearchMode.valueOf(mode);
            }

            Bundle locBundle = savedInstanceState.getBundle(STATE_DEPART_LOC_KEY);
            if (locBundle != null) {
                departLocation = new LocationChoice(locBundle);
            }

            locBundle = savedInstanceState.getBundle(STATE_ARRIVE_LOC_KEY);
            if (locBundle != null) {
                arriveLocation = new LocationChoice(locBundle);
            }

            departDate = (Calendar) savedInstanceState.getSerializable(STATE_DEPART_DT_KEY);

            headerTxt = savedInstanceState.getString("headerTxt");
        }

        // inflate the search fragment
        root = inflater.inflate(R.layout.air_price_to_beat_search, container, false);

        // now initialise the view

        getBaseActivity().getSupportActionBar().setTitle(R.string.home_action_air_price_to_beat);

        TextView txtView = (TextView) root.findViewById(R.id.air_search_header);
        txtView.setText(headerTxt);

        modeOneWay = (Button) root.findViewById(R.id.air_search_oneway);
        modeOneWay.setOnClickListener(this);

        modeRoundTrip = (Button) root.findViewById(R.id.air_search_roundtrip);
        modeRoundTrip.setOnClickListener(this);

        searchButton = (Button) root.findViewById(R.id.footer_button_one);
        searchButton.setText(R.string.general_search);
        searchButton.setOnClickListener(this);

        setFieldName(R.id.air_search_depart_loc, R.string.air_search_label_departcity);
        setFieldName(R.id.air_search_arrive_loc, R.string.air_search_label_arrivecity);
        setFieldName(R.id.air_search_depart_date, R.string.air_search_label_departdate);

        updateLocationViews();
        updateDepartDateView();

        // Set up the handlers
        root.findViewById(R.id.air_search_depart_loc).setOnClickListener(this);
        root.findViewById(R.id.air_search_arrive_loc).setOnClickListener(this);
        root.findViewById(R.id.air_search_depart_date).setOnClickListener(this);

        // Set us up the default
        switch (searchMode) {
        case None:
        case RoundTrip:
            selectModeButton(modeRoundTrip);
            break;
        case OneWay:
            selectModeButton(modeOneWay);
            break;
        default:
            break;
        }

        calendarDialog = (CalendarPickerDialog) getFragmentManager().findFragmentByTag(TAG_CALENDAR_DIALOG_FRAGMENT);
        if (calendarDialog != null) {
            calendarDialog.setOnDateSetListener(new AirDateSetListener());
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(MODE_KEY, searchMode.name());

        if (departLocation != null) {
            outState.putBundle(STATE_DEPART_LOC_KEY, departLocation.getBundle());
        }
        if (arriveLocation != null) {
            outState.putBundle(STATE_ARRIVE_LOC_KEY, arriveLocation.getBundle());
        }

        outState.putSerializable(STATE_DEPART_DT_KEY, departDate);

        outState.putString("headerTxt", headerTxt);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AirPriceToBeatSearch) {
            baseActivity = (AirPriceToBeatSearch) activity;
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".onAttach: activity is not instance of BaseActivity!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseActivity = null;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.air_search_depart_loc) {
            listener.onDepartLocClicked();
        } else if (id == R.id.air_search_arrive_loc) {
            listener.onArriveLocClicked();
        } else if (id == R.id.air_search_depart_date) {
            showCalendarDialog();
        } else if (id == R.id.air_search_oneway || id == R.id.air_search_roundtrip || id == R.id.air_search_multi) {
            selectModeButton(v);
        } else if (id == R.id.footer_button_one) {
            listener.onSearchClicked(departLocation, arriveLocation, departDate, roundTrip);// , travelPointsEnabled);
        }
    }

    private void showCalendarDialog() {
        Bundle bundle;
        calendarDialog = new CalendarPickerDialog();

        bundle = new Bundle();
        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, departDate.get(Calendar.YEAR));
        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, departDate.get(Calendar.MONTH));
        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, departDate.get(Calendar.DAY_OF_MONTH));
        bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);

        calendarDialog.setArguments(bundle);
        calendarDialog.setOnDateSetListener(new AirDateSetListener());

        calendarDialog.show(getFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (baseActivity != null) {
            RetainerFragment retainer = baseActivity.getRetainer();
            if (retainer != null) {
                // Store the listener
                if (listener != null) {
                    retainer.put(ON_CLICK_LISTENER_KEY, listener);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: retainer is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: baseActivity is null!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Will restore the listener
     */
    protected void restoreReceivers() {
        if (baseActivity != null) {
            RetainerFragment retainer = baseActivity.getRetainer();
            if (retainer != null) {
                if (retainer.contains(ON_CLICK_LISTENER_KEY)) {
                    listener = (PriceToBeatSearchFragmentOnClickListener) retainer.get(ON_CLICK_LISTENER_KEY);
                    Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: retainer contains listener ? "
                            + (listener == null));
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: retainer is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: baseActivity is null!");
        }
    }

    public void updateLocationViews() {
        if (departLocation != null) {
            setFieldValue(R.id.air_search_depart_loc, departLocation.getName());
        }

        if (arriveLocation != null) {
            setFieldValue(R.id.air_search_arrive_loc, arriveLocation.getName());
        }

        if (departLocation != null && arriveLocation != null) {
            searchButton.setEnabled(true);
        } else {
            searchButton.setEnabled(false);
        }
    }

    public void updateDepartDateView() {
        setFieldValue(R.id.air_search_depart_date,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, departDate));
    }

    private void setFieldName(int parentView, int textId) {
        TextView tv = (TextView) (root.findViewById(parentView).findViewById(R.id.field_name));
        tv.setText(textId);
    }

    private void setFieldValue(int parentView, CharSequence text) {
        TextView tv = (TextView) (root.findViewById(parentView).findViewById(R.id.field_value));
        tv.setText(text);
    }

    protected void selectModeButton(View v) {
        if (!v.isSelected()) {
            // Only do something if we are clicking a different button
            final int id = v.getId();
            if (id == R.id.air_search_oneway) {
                modeOneWay.setSelected(true);
                modeRoundTrip.setSelected(false);
                searchMode = SearchMode.OneWay;
            } else {
                modeRoundTrip.setSelected(true);
                modeOneWay.setSelected(false);
                searchMode = SearchMode.RoundTrip;
            }
            updateSearchMode();
        }
    }

    private void updateSearchMode() {
        roundTrip = searchMode.name().equals(SearchMode.RoundTrip.name());
    }

    // interface to call back the activity behavior
    public interface PriceToBeatSearchFragmentOnClickListener {

        public void onDepartLocClicked();

        public void onArriveLocClicked();

        public void onSearchClicked(LocationChoice departLocation, LocationChoice arriveLocation, Calendar departDate,
                boolean roundTrip);
    }

    // set the listener
    public void setPriceToBeatSearchFragmentOnClickListener(PriceToBeatSearchFragmentOnClickListener listener) {
        this.listener = listener;
    }

    /**
     * Handle the date selection event
     */
    class AirDateSetListener implements CalendarPickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            departDate.set(year, monthOfYear, dayOfMonth);

            updateDepartDateView();
            calendarDialog.dismiss();
        }
    }
}

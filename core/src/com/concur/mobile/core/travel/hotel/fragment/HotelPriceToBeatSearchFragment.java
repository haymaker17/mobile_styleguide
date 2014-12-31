package com.concur.mobile.core.travel.hotel.fragment;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.hotel.activity.HotelPriceToBeatSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.SpinnerItem;

/**
 * Fragment to display the Hotel Price to Beat search
 * 
 * @author RatanK
 * 
 */
public class HotelPriceToBeatSearchFragment extends BaseFragment implements View.OnClickListener {

    private static final String CLS_TAG = HotelPriceToBeatSearchFragment.class.getSimpleName();

    private static final String ON_CLICK_LISTENER_KEY = "on.click.listener";

    private LocationChoice location;
    private String monthNumber;
    public Button searchButton;

    private View root;
    private View locView;
    private View monthView;
    protected View distanceView;
    protected View distanceUnitView;

    private HotelPriceToBeatSearch baseActivity;
    private PriceToBeatSearchFragmentOnClickListener listener;

    private Dialog distanceDialog;
    private Dialog unitsDialog;
    private Dialog monthOfStayDialog;

    // Distance drop-down.
    public final static SpinnerItem[] distanceItems = new SpinnerItem[] { new SpinnerItem("1", "1"),
            new SpinnerItem("2", "2"), new SpinnerItem("5", "5"), new SpinnerItem("10", "10"),
            new SpinnerItem("15", "15"), new SpinnerItem("25", "25"), new SpinnerItem("100", "> 25") };

    // Distance value.
    public SpinnerItem currentDistance;

    // Distance unit options.
    public final static SpinnerItem[] distanceUnits = new SpinnerItem[] {
            new SpinnerItem("M", R.string.search_distance_unit_miles),
            new SpinnerItem("K", R.string.search_distance_unit_km), };

    public SpinnerItem currentDistanceUnit;

    // Month options.
    public final static SpinnerItem[] monthItems = new SpinnerItem[] {
            new SpinnerItem("1", R.string.general_month_jan), new SpinnerItem("2", R.string.general_month_feb),
            new SpinnerItem("3", R.string.general_month_mar), new SpinnerItem("4", R.string.general_month_apr),
            new SpinnerItem("5", R.string.general_month_may), new SpinnerItem("6", R.string.general_month_jun),
            new SpinnerItem("7", R.string.general_month_jul), new SpinnerItem("8", R.string.general_month_aug),
            new SpinnerItem("9", R.string.general_month_sep), new SpinnerItem("10", R.string.general_month_oct),
            new SpinnerItem("11", R.string.general_month_nov), new SpinnerItem("12", R.string.general_month_dec), };

    public SpinnerItem currentMonth;

    private String headerTxt;

    public void setHeaderTxt(String headerTxt) {
        this.headerTxt = headerTxt;
    }

    public LocationChoice getLocation() {
        return location;
    }

    public void setLocation(LocationChoice location) {
        this.location = location;
    }

    public String getLatitude() {
        return location.latitude;
    }

    public String getLongitude() {
        return location.longitude;
    }

    public String getRadius() {
        return (currentDistance != null ? currentDistance.id : "5");
    }

    public String getScale() {
        return (currentDistanceUnit != null ? currentDistanceUnit.id : "M");
    }

    public String getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(String monthNumber) {
        this.monthNumber = monthNumber;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState == null) {
            if (currentMonth == null) {
                // default to current month
                Calendar now = Calendar.getInstance();
                int currMonthNumber = now.get(Calendar.MONTH);
                currentMonth = HotelPriceToBeatSearchFragment.monthItems[currMonthNumber];
            }
        } else {

            Bundle locBundle = savedInstanceState.getBundle("Location");
            if (locBundle != null) {
                location = new LocationChoice(locBundle);
            }

            if (savedInstanceState.containsKey("CurrentDistance")) {
                currentDistance = (SpinnerItem) savedInstanceState.getSerializable("CurrentDistance");
            }

            if (savedInstanceState.containsKey("CurrentDistanceUnit")) {
                currentDistanceUnit = (SpinnerItem) savedInstanceState.getSerializable("CurrentDistanceUnit");
            }

            if (savedInstanceState.containsKey("CurrentMonth")) {
                currentMonth = (SpinnerItem) savedInstanceState.getSerializable("CurrentMonth");
            }

            headerTxt = savedInstanceState.getString(Const.EXTRA_PRICE_TO_BEAT_HEADER_TXT);
        }

        // inflate the search fragment
        root = inflater.inflate(R.layout.hotel_price_to_beat_search, container, false);

        getBaseActivity().getSupportActionBar().setTitle(R.string.home_action_hotel_price_to_beat);

        TextView txtView = (TextView) root.findViewById(R.id.hotel_search_header);
        txtView.setText(headerTxt);

        searchButton = (Button) root.findViewById(R.id.footer_button_one);
        searchButton.setText(R.string.general_search);
        searchButton.setEnabled(false);
        searchButton.setOnClickListener(this);

        configureLocationButton();

        configureDistanceValue();

        configureDistanceUnit();

        configureMonthOfStay();

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (location != null) {
            outState.putBundle("Location", location.getBundle());
        }

        if (currentDistance != null) {
            outState.putSerializable("CurrentDistance", currentDistance);
        }

        if (currentDistanceUnit != null) {
            outState.putSerializable("CurrentDistanceUnit", currentDistanceUnit);
        }

        if (currentMonth != null) {
            outState.putSerializable("CurrentMonth", currentMonth);
        }

        outState.putString(Const.EXTRA_PRICE_TO_BEAT_HEADER_TXT, headerTxt);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof HotelPriceToBeatSearch) {
            baseActivity = (HotelPriceToBeatSearch) activity;
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
        if (id == R.id.hotel_search_loc) {
            listener.onLocClicked();
        } else if (id == R.id.hotel_search_distance) {
            showDistanceDialog();
        } else if (id == R.id.hotel_search_distance_units) {
            showDistanceUnitDialog();
        } else if (id == R.id.hotel_month_of_stay) {
            showMonthOfStayDialog();
        } else if (id == R.id.footer_button_one) {
            listener.onSearchClicked(location, currentDistance.id, currentDistanceUnit.id, monthNumber);
        }
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

    /**
     * Handle the initial setup of the location button
     */
    protected void configureLocationButton() {
        locView = root.findViewById(R.id.hotel_search_loc);
        if (locView != null) {
            TextView txtView = (TextView) locView.findViewById(R.id.field_name);
            txtView.setText(R.string.hotel_search_label_location);

            // Initialize the display
            updateLocationButton();

            locView.setOnClickListener(this);
        }
    }

    /**
     * Update the button with the current location selection
     */
    public void updateLocationButton() {
        if (locView != null) {
            TextView txtView = (TextView) locView.findViewById(R.id.field_value);
            if (location == null) {
                SpannableString s = new SpannableString(getText(R.string.search_location_prompt));
                s.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                txtView.setText(s);
            } else {
                txtView.setText(location.getName());
            }

            checkSearchButton();
        }
    }

    protected void configureDistanceValue() {
        distanceView = root.findViewById(R.id.hotel_search_distance);
        if (distanceView != null) {
            // Set the field name.
            TextView txtView = (TextView) distanceView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.hotel_search_distance_label);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceValue: unable to locate field name text view!");
            }
            if (currentDistance == null) {
                currentDistance = distanceItems[2];
            }
            updateDistance();

            distanceView.setOnClickListener(this);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceValue: unable to locate distance search distance!");
        }
    }

    /**
     * Will update the distance view based on the current spinner item value.
     */
    public void updateDistance() {
        if (distanceView != null) {
            TextView txtView = (TextView) distanceView.findViewById(R.id.field_value);
            if (txtView != null) {
                if (currentDistance != null) {
                    txtView.setText(currentDistance.name);
                } else {
                    txtView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateDistance: unable to locate distance field_value!");
            }
        }
    }

    protected void configureDistanceUnit() {

        distanceUnitView = root.findViewById(R.id.hotel_search_distance_units);
        if (distanceUnitView != null) {
            // Set the field name.
            TextView txtView = (TextView) distanceUnitView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText("");
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceUnit: unable to locate field name text view!");
            }
            if (currentDistanceUnit == null) {
                currentDistanceUnit = distanceUnits[0];
            }

            updateDistanceUnit();

            distanceUnitView.setOnClickListener(this);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceValue: unable to locate distance search distance unit!");
        }
    }

    /**
     * Will update the distance unit view based on the current spinner value.
     */
    public void updateDistanceUnit() {
        if (distanceUnitView != null) {
            TextView txtView = (TextView) distanceUnitView.findViewById(R.id.field_value);
            if (txtView != null) {
                if (currentDistanceUnit != null) {
                    txtView.setText(currentDistanceUnit.name);
                } else {
                    txtView.setText("");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateDistanceUnit: unable to locate distance_unit field_value!");
            }
        }
    }

    /**
     * Handle the initial setup of the month of stay
     */
    protected void configureMonthOfStay() {
        monthView = root.findViewById(R.id.hotel_month_of_stay);
        TextView txtView = (TextView) monthView.findViewById(R.id.field_name);
        txtView.setText(R.string.hotel_price_to_beat_search_month_of_stay);

        updateMonthOfStay();

        monthView.setOnClickListener(this);
    }

    public void updateMonthOfStay() {
        if (monthView != null) {
            TextView txtView = (TextView) monthView.findViewById(R.id.field_value);
            if (txtView != null) {
                monthNumber = currentMonth.id;
                txtView.setText(currentMonth.name);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateMonthOfStay: unable to locate month_of_stay_field_value!");
            }
        }
    }

    /**
     * Will check fields and enable/disable the search button.
     */
    private void checkSearchButton() {
        if (location != null) {
            searchButton.setEnabled(true);
        } else {
            searchButton.setEnabled(false);
        }
    }

    private void showDistanceDialog() {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
        dlgBldr.setCancelable(true);
        ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(getActivity(),
                android.R.layout.simple_spinner_item, HotelPriceToBeatSearchFragment.distanceItems) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getDropDownView(position, convertView, parent);
            }
        };

        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int selectedItem = -1;
        if (currentDistance != null) {
            for (int i = 0; i < HotelPriceToBeatSearchFragment.distanceItems.length; i++) {
                if (currentDistance.id.equals(HotelPriceToBeatSearchFragment.distanceItems[i].id)) {
                    selectedItem = i;
                    break;
                }
            }
        }

        dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentDistance = HotelPriceToBeatSearchFragment.distanceItems[which];
                updateDistance();
                distanceDialog.dismiss();
            }
        });

        dlgBldr.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                distanceDialog.dismiss();
            }
        });

        distanceDialog = dlgBldr.create();
        distanceDialog.show();
    }

    private void showDistanceUnitDialog() {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
        dlgBldr.setCancelable(true);
        ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(getActivity(),
                android.R.layout.simple_spinner_item, HotelPriceToBeatSearchFragment.distanceUnits) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getDropDownView(position, convertView, parent);
            }
        };

        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int selectedItem = -1;
        if (currentDistanceUnit != null) {
            for (int i = 0; i < HotelPriceToBeatSearchFragment.distanceUnits.length; i++) {
                if (currentDistanceUnit.id.equals(HotelPriceToBeatSearchFragment.distanceUnits[i].id)) {
                    selectedItem = i;
                    break;
                }
            }
        }

        dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentDistanceUnit = HotelPriceToBeatSearchFragment.distanceUnits[which];
                updateDistanceUnit();
                unitsDialog.dismiss();
            }
        });

        dlgBldr.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                unitsDialog.dismiss();
            }
        });

        unitsDialog = dlgBldr.create();
        unitsDialog.show();
    }

    private void showMonthOfStayDialog() {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());
        dlgBldr.setCancelable(true);

        ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(getActivity(),
                android.R.layout.simple_spinner_item, HotelPriceToBeatSearchFragment.monthItems) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getDropDownView(position, convertView, parent);
            }
        };

        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int selectedItem = -1;
        if (currentMonth != null) {
            for (int i = 0; i < HotelPriceToBeatSearchFragment.monthItems.length; i++) {
                if (currentMonth.id.equals(HotelPriceToBeatSearchFragment.monthItems[i].id)) {
                    selectedItem = i;
                    break;
                }
            }
        }

        dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentMonth = HotelPriceToBeatSearchFragment.monthItems[which];
                updateMonthOfStay();
                monthOfStayDialog.dismiss();
            }
        });

        dlgBldr.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                monthOfStayDialog.dismiss();
            }
        });

        monthOfStayDialog = dlgBldr.create();
        monthOfStayDialog.show();
    }

    // interface to call back the activity behavior
    public interface PriceToBeatSearchFragmentOnClickListener {

        public void onLocClicked();

        public void onSearchClicked(LocationChoice location, String distance, String distanceUnit, String monthNumber);
    }

    // set the listener
    public void setPriceToBeatSearchFragmentOnClickListener(PriceToBeatSearchFragmentOnClickListener listener) {
        this.listener = listener;
    }

}

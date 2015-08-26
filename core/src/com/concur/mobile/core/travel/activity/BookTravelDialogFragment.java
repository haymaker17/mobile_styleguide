/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.eva.activity.VoiceSearchActivity;
import com.concur.mobile.core.travel.air.activity.AirSearch;
import com.concur.mobile.core.travel.air.activity.VoiceAirSearchActivity;
import com.concur.mobile.core.travel.car.activity.CarSearch;
import com.concur.mobile.core.travel.car.activity.VoiceCarSearchActivity;
import com.concur.mobile.core.travel.hotel.activity.VoiceHotelSearchActivity;
import com.concur.mobile.core.travel.hotel.jarvis.activity.RestHotelSearch;
import com.concur.mobile.core.travel.rail.activity.RailSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;

import java.util.ArrayList;

/**
 * <code>DialogFragment</code> to prompt user to book something, i.e. a Hotel, Flight, Car, Train, etc.
 * 
 * @author Chris N. Diaz
 * 
 */
public class BookTravelDialogFragment extends DialogFragment {

    private static final String CLS_TAG = BookTravelDialogFragment.class.getSimpleName();

    /**
     * An enumeration describing an booking action.
     * 
     * @author Chris N. Diaz
     */
    public enum BookTravelAction {
        BOOK_AIR, BOOK_HOTEL, BOOK_CAR, BOOK_RAIL
    }

    /**
     * Argument used to indicate if this DialogFragment was launched from the "More Menu".
     */
    public static final String IS_FROM_MORE_MENU_ARG = "IS_FROM_MORE_MENU_ARG";

    protected boolean isFromMoreMenu = false;

    /**
     * Private, no-arg, constructor to force initialization of <code>BookTravelDialogFragment(boolean)</code> constructor.
     */
    public BookTravelDialogFragment() {
        super();
    }

    /**
     * Creates a new instance of this DialogFragment and hiding or showing the "Book Rail" option based on the
     * <code>isRailUser</code> parameter.
     * 
     * @param isRailUser
     *            if <code>true</code> show the "Book Rail" option, otherwise, <code>false</code> will hide the option.
     */

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getText(R.string.home_action_title));

        BookTravelOptionListAdapter bookActionAdapter = new BookTravelOptionListAdapter(this);
        bookActionAdapter.options.add(BookTravelAction.BOOK_AIR);
        bookActionAdapter.options.add(BookTravelAction.BOOK_HOTEL);
        bookActionAdapter.options.add(BookTravelAction.BOOK_CAR);

        if (ViewUtil.isRailUser(getActivity())) {
            bookActionAdapter.options.add(BookTravelAction.BOOK_RAIL);
        }

        builder.setSingleChoiceItems(bookActionAdapter, -1, new BookTravelDialogListener(bookActionAdapter));

        return builder.create();

    } // onCreateDialog()

    /**
     * An extension of <code>BaseAdapter</code> for selecting a receipt image option.
     * 
     * @author Chris N. Diaz
     */
    class BookTravelOptionListAdapter extends BaseAdapter {

        /**
         * Contains a list of available options.
         */
        ArrayList<BookTravelAction> options = new ArrayList<BookTravelAction>();
        BookTravelDialogFragment dialogFragment;

        public BookTravelOptionListAdapter(BookTravelDialogFragment dialogFragment) {
            super();
            this.dialogFragment = dialogFragment;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        public int getCount() {
            return options.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        public Object getItem(int position) {
            return options.get(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            view = inflater.inflate(R.layout.context_menu_icon_option, null);

            int textResId = 0;
            final BookTravelAction bookingAction = options.get(position);
            final boolean allowVoiceBooking = Preferences.shouldAllowVoiceBooking(); // MOB-11596 MOB-13636
            switch (bookingAction) {
            case BOOK_AIR:
                textResId = R.string.home_action_book_air;

                // Add mic to launch Air Voice Search
                if (allowVoiceBooking) {
                    addMicButton(VoiceAirSearchActivity.class, view, dialogFragment);
                }

                break;
            case BOOK_HOTEL:
                textResId = R.string.home_action_book_hotel;

                // Add mic to launch Hotel Voice Search
                if (allowVoiceBooking) {
                    addMicButton(VoiceHotelSearchActivity.class, view, dialogFragment);
                }

                break;
            case BOOK_CAR:
                textResId = R.string.home_action_book_car;

                // Add mic to launch Car Voice Search
                if (allowVoiceBooking) {
                    addMicButton(VoiceCarSearchActivity.class, view, dialogFragment);
                }

                break;
            case BOOK_RAIL:
                textResId = R.string.home_action_book_rail;
                break;
            default:
                break;
            }

            // Set the text.
            if (textResId != 0) {
                TextView txtView = (TextView) view.findViewById(R.id.text);
                if (txtView != null) {
                    txtView.setPadding(10, 8, 0, 8);
                    txtView.setText(getActivity().getText(textResId));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate text view!");
                }
            }

            return view;

        } // getView()

    } // BookTravelOptionListAdapter class

    /**
     * Adds the mic icon and click listener for the given <code>VoiceSearchActivity</code>
     * 
     * @param voiceActivity
     *            the activity to launch when the mic button is selected.
     * @param view
     *            the view to search for the mic icon.
     * @param dialogFragment
     */
    protected void addMicButton(final Class<? extends VoiceSearchActivity> voiceActivity, View view,
            final BookTravelDialogFragment dialogFragment) {

        ImageView imgView = (ImageView) view.findViewById(R.id.icon);
        if (imgView != null) {
            imgView.setImageResource(R.drawable.menu_mic_button);
            imgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Activity a = getActivity();

                    // Check whether user has permission to book air via mobile.
                    if (voiceActivity == VoiceAirSearchActivity.class) {
                        if (ViewUtil.isAirUser(a)) {
                            // Check for a complete travel profile.
                            if (ViewUtil.isTravelProfileComplete(a) || ViewUtil.isTravelProfileCompleteMissingTSA(a)) {

                                Intent i = new Intent(getActivity(), VoiceAirSearchActivity.class);
                                if (isFromMoreMenu) {
                                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                                } else {
                                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                                }
                                a.startActivity(i);

                            } else {
                                a.showDialog(Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE);
                            }
                        } else {
                            a.showDialog(Const.DIALOG_TRAVEL_NO_AIR_PERMISSION);
                        }
                    } else {

                        Intent i = new Intent(getActivity(), voiceActivity);
                        if (isFromMoreMenu) {
                            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                        } else {
                            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                        }
                        a.startActivity(i);
                    }

                    dialogFragment.dismiss();
                }
            });
        }
    } // addMicButton()

    /**
     * An implementation of <code>DialogInterface.OnClickListener</code> for handling user selection receipt option.
     * 
     * @author Chris N. Diaz
     */
    class BookTravelDialogListener implements DialogInterface.OnClickListener {

        BookTravelOptionListAdapter adapter;

        public BookTravelDialogListener(BookTravelOptionListAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
         */
        public void onClick(DialogInterface dialog, int which) {

            Intent i;
            Activity a = getActivity();
            BookTravelAction bookingAction = (BookTravelAction) adapter.getItem(which);

            switch (bookingAction) {
            case BOOK_AIR:
                // Check whether user has permission to book air via mobile.
                if (ViewUtil.isAirUser(a)) {
                    // Check for a complete travel profile.
                    if (ViewUtil.isTravelProfileComplete(a) || ViewUtil.isTravelProfileCompleteMissingTSA(a)) {
                        i = getAirSearchIntent(a);
                        if (isFromMoreMenu) {
                            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                        } else {
                            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                        }
                        startActivity(i);
                    } else {
                        a.showDialog(Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE);
                    }
                } else {
                    a.showDialog(Const.DIALOG_TRAVEL_NO_AIR_PERMISSION);
                }
                break;
            case BOOK_CAR:
                i = getCarSearchIntent(a);
                if (isFromMoreMenu) {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                } else {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                }
                startActivity(i);
                break;
            case BOOK_HOTEL:
                i = getHotelSearchIntent(a);
                if (isFromMoreMenu) {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                } else {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                }
                startActivityForResult(i, Const.REQUEST_CODE_BOOK_HOTEL);
                break;
            case BOOK_RAIL:
                i = getRailSearchIntent(a);
                if (isFromMoreMenu) {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME_MORE);
                } else {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
                }
                startActivity(i);
                break;

            } // switch-case

            dialog.dismiss();

        } // onClick()

    } // BookTravelDialogListener class

    /**
     * returns air search intent
     * */
    public Intent getAirSearchIntent(Activity act) {
        return new Intent(act, AirSearch.class);
    }

    /**
     * returns car search intent
     * */
    public Intent getCarSearchIntent(Activity act) {
        return new Intent(act, CarSearch.class);
    }

    /**
     * returns hotel search intent
     * */
    public Intent getHotelSearchIntent(Activity act) {
        if (Preferences.shouldShowHotelJarvisUI())
            return new Intent(act, RestHotelSearch.class);
        else
            return new Intent(act, HotelSearch.class);
    }

    /**
     * returns rail search intent
     * */
    public Intent getRailSearchIntent(Activity act) {
        return new Intent(act, RailSearch.class);
    }

} // BookTravelDialogFragment class


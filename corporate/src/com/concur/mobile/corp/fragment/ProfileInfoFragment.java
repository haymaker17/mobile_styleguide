package com.concur.mobile.corp.fragment;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.fragment.BaseFragment;

public class ProfileInfoFragment extends BaseFragment {

    private EditText fname, lname, phone, address, city, zipcode;
    private Spinner country, state;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.profile_main, null);

        // Tweak the action bar
        final ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.home_navigation_profile);

        setGeneralProfile(root);

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // TODO - MOB-23434 - mulitbuild jira - do not check in the change in package name into develop
        inflater.inflate(com.concur.breeze.jarvis.R.menu.profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        // TODO - MOB-23434 - mulitbuild jira - do not check in the change in package name into develop
        case com.concur.breeze.jarvis.R.id.menuSaveProfile:
            // do nothing
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setGeneralProfile(View root) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // find general profile
        View profileView = (View) root.findViewById(R.id.profile_view);
        if (profileView != null) {
            setName(profileView);
            setPhone(profileView);
            setAddress(profileView);
            setCityStateZip(profileView);
            setCountry(profileView);

            TextView txtView = (TextView) root.findViewById(R.id.other_title);
            txtView.setText(activity.getResources().getString(R.string.profile_other));

            setEmergencyContact(root);
            setBankInfo(root);
            setNewCard(root);
        } else {
            activity.finish();
        }
    }

    private void setName(View profileView) {
        // set icon
        setProfileIcon(profileView, R.id.name_icon, R.drawable.profile_icon_name);

        // set first name
        View fnameView = (View) profileView.findViewById(R.id.fname_view);
        if (fnameView != null) {
            setProfileLabels(fnameView, R.id.field_name, R.string.profile_fname);
            // first name value
            fname = (EditText) fnameView.findViewById(R.id.field_value);
        }

        // set last name
        View lnameView = (View) profileView.findViewById(R.id.lname_view);
        if (lnameView != null) {
            setProfileLabels(lnameView, R.id.field_name, R.string.profile_lname);
            // first name value
            lname = (EditText) lnameView.findViewById(R.id.field_value);
        }
    }

    private void setPhone(View profileView) {
        // set icon
        setProfileIcon(profileView, R.id.phone_icon, R.drawable.profile_icon_phone);
        // set phone title
        View phoneView = (View) profileView.findViewById(R.id.phone_view);
        if (phoneView != null) {
            setProfileLabels(phoneView, R.id.field_name, R.string.profile_phone);
            // first phone value
            phone = (EditText) phoneView.findViewById(R.id.field_value);
            phone.setInputType(android.text.InputType.TYPE_CLASS_PHONE | InputType.TYPE_CLASS_NUMBER);
        }
    }

    private void setCountry(View profileView) {
        // set icon
        setProfileIcon(profileView, R.id.address3_icon, R.drawable.profile_icon_address);

        // set country name
        View countryView = (View) profileView.findViewById(R.id.country_view);
        if (countryView != null) {
            setProfileLabels(countryView, R.id.field_name, R.string.profile_country);
            // state value
            // city = (EditText) cityView.findViewById(R.id.field_value);
        }
    }

    private void setAddress(View profileView) {
        // set address title
        View addressView = (View) profileView.findViewById(R.id.address_view);
        if (addressView != null) {
            setProfileLabels(addressView, R.id.field_name, R.string.profile_address);
            // first address value
            address = (EditText) addressView.findViewById(R.id.field_value);
        }
    }

    private void setCityStateZip(View profileView) {
        // set city name
        View cityView = (View) profileView.findViewById(R.id.city_view);
        if (cityView != null) {
            setProfileLabels(cityView, R.id.field_name, R.string.profile_city);
            // first city value
            city = (EditText) cityView.findViewById(R.id.field_value);
        }

        // set zipcode name
        View zipCodeView = (View) profileView.findViewById(R.id.zipcode_view);
        if (zipCodeView != null) {
            setProfileLabels(zipCodeView, R.id.field_name, R.string.profile_zipcode);
            // first zipcode value
            zipcode = (EditText) zipCodeView.findViewById(R.id.field_value);
        }

        // set state name
        View stateView = (View) profileView.findViewById(R.id.state_view);
        if (stateView != null) {
            setProfileLabels(stateView, R.id.field_name, R.string.profile_state);
            // state value
            // city = (EditText) cityView.findViewById(R.id.field_value);
        }
    }

    private void setNewCard(View root) {

        // set phone title
        View newCardView = (View) root.findViewById(R.id.add_card_view);
        if (newCardView != null) {
            // set icon
            setProfileIcon(newCardView, R.id.field_icon, R.drawable.profile_icon_credit_card);
            // set title
            setProfileLabels(newCardView, R.id.field_name, R.string.profile_credit_card);
            // setinfo
            setProfileLabels(newCardView, R.id.field_value, R.string.profile_add_credit_card);
            // disable click events
            newCardView.setEnabled(false);
        }
    }

    private void setBankInfo(View root) {

        // set phone title
        View bankInfoView = (View) root.findViewById(R.id.bank_account_info_view);
        if (bankInfoView != null) {
            // set icon
            setProfileIcon(bankInfoView, R.id.field_icon, R.drawable.profile_icon_bank);
            // set title
            setProfileLabels(bankInfoView, R.id.field_name, R.string.profile_banck_account);
            // setinfo
            setProfileLabels(bankInfoView, R.id.field_value, R.string.profile_add_banck_account);
            // disable click events
            bankInfoView.setEnabled(false);
        }
    }

    private void setEmergencyContact(View root) {
        // set phone title
        View emergencyContactView = (View) root.findViewById(R.id.emergency_contact_view);
        if (emergencyContactView != null) {
            // set icon
            setProfileIcon(emergencyContactView, R.id.field_icon, R.drawable.profile_icon_contact);
            // set title
            setProfileLabels(emergencyContactView, R.id.field_name, R.string.profile_emergency_contact);
            // setinfo
            setProfileLabels(emergencyContactView, R.id.field_value, R.string.profile_add_emergency_contact);
            // disable click events
            emergencyContactView.setEnabled(false);
        }
    }

    private void setProfileIcon(View profileView, int resId, int drawable) {
        ImageView imgView = (ImageView) profileView.findViewById(resId);
        imgView.setImageDrawable(activity.getResources().getDrawable(drawable));
    }

    private void setProfileLabels(View profileView, int resId, int stringResId) {
        TextView fnameTitle = (TextView) profileView.findViewById(resId);
        fnameTitle.setText(activity.getResources().getString(stringResId));
    }
}

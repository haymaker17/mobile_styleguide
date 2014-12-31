package com.concur.mobile.platform.ui.travel.hotel.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment;

/**
 * 
 * @author tejoa
 * 
 */
public class HotelChoiceDetailsActivity extends Activity {

    public static final String FRAGMENT_HOTEL_DETAILS = "FRAGMENT_HOTEL_DETAILS";

    private HotelChoiceDetailsFragment hotelDetailsFrag;

    // private HotelSearchResultListItem hotel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.map) {
            Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT).show();
            // onMapsClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void onMapsClicked() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_search_and_result);

        hotelDetailsFrag = (HotelChoiceDetailsFragment) getFragmentManager().findFragmentByTag(FRAGMENT_HOTEL_DETAILS);
        if (hotelDetailsFrag == null) {
            hotelDetailsFrag = new HotelChoiceDetailsFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.container, hotelDetailsFrag, FRAGMENT_HOTEL_DETAILS);
            ft.commit();
        }

    }

}

package com.concur.mobile.core.expense.travelallowance.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.fragment.FixedTravelAllowanceListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceItinerary;

/**
 * Created by D049515 on 15.06.2015.
 */
public class TravelAllowanceActivity extends AppCompatActivity {

    private static final String ADJUSTMENTS_FRAGMENT_TAG = "adjustments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.travel_allowance_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        toolbar.setTitle("Travel Allowances");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                case 0:
                    return new FixedTravelAllowanceListFragment();
                case 1:
                    return new TravelAllowanceItinerary();
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {

                switch (position) {
                    case 0:
                        return "Adjustments";
                    case 1:
                        return "Itineraries";
                }

                return super.getPageTitle(position);
            }
        };

        pager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        if (fragmentManager != null && fragmentManager.findFragmentByTag(ADJUSTMENTS_FRAGMENT_TAG) == null) {
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.add(R.id.adjustment_fragment_container, new FixedTravelAllowanceListFragment(),
//                    ADJUSTMENTS_FRAGMENT_TAG);
//            transaction.commit();
//            // getSupportFragmentManager().executePendingTransactions();
//        }
        
    }
}

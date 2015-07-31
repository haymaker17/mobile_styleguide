package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.activity.ItineraryUpdateActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.SimpleItineraryListAdapter;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.List;

/**
 * Created by D049515 on 27.07.2015.
 */
public class SimpleTAItineraryListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String CLASS_TAG = SimpleTAItineraryListFragment.class.getSimpleName();
    public static final String ON_REFRESH_MSG = CLASS_TAG + ".refreshItineraries";

    private boolean expenseReportIsSubmitted;
    private String expenseReportName;
    private String expenseReportKey;
    private SwipeRefreshLayout swipeRefreshLayout;
    private IFragmentCallback callback;
    private SimpleItineraryListAdapter adapter;
    private List<Itinerary> itineraryList;

    private RecyclerView recyclerView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.callback = (IFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentCallback") ;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ta_itinerary_simple_list, container, false);

        itineraryList = (List<Itinerary>) getArguments().getSerializable(BundleId.ITINERARY_LIST);

        if (getArguments() != null) {
            this.expenseReportIsSubmitted = getArguments().getBoolean(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false);
            this.expenseReportName = getArguments().getString(BundleId.EXPENSE_REPORT_NAME, StringUtilities.EMPTY_STRING);
            this.expenseReportKey = getArguments().getString(BundleId.EXPENSE_REPORT_KEY, StringUtilities.EMPTY_STRING);
        }

        adapter = new SimpleItineraryListAdapter(itineraryList);
        adapter.setOnClickListener(this);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        this.recyclerView.setLayoutManager(llm);
        this.recyclerView.setAdapter(adapter);

        FloatingActionButton createItineraryFAB = (FloatingActionButton) view.findViewById(R.id.fab);
        if (createItineraryFAB != null) {
            if (expenseReportIsSubmitted) {
                createItineraryFAB.setVisibility(View.GONE);
            } else {
                createItineraryFAB.setVisibility(View.VISIBLE);
            }
            createItineraryFAB.setOnClickListener(this);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            Intent intent = new Intent(getActivity(), ItineraryUpdateActivity.class);
            Itinerary itin = new Itinerary();
            itin.setName(expenseReportName);
            itin.setExpenseReportID(expenseReportKey);
            intent.putExtra(BundleId.ITINERARY, itin);
            startActivity(intent);
        } else {
            int pos = this.recyclerView.getChildAdapterPosition(view);
            Itinerary itinerary = itineraryList.get(pos);
            Intent intent = new Intent(getActivity(), ItineraryUpdateActivity.class);
            intent.putExtra(BundleId.ITINERARY, itinerary);
            startActivity(intent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRefresh() {
        if (this.callback != null) {
            this.callback.sendMessage(ON_REFRESH_MSG);
        } else {
            onRefreshFinished(null);
        }
    }

    public void onRefreshFinished(Bundle result) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (result != null) {
            this.itineraryList = (List<Itinerary>) result.getSerializable(BundleId.ITINERARY_LIST);
            adapter.refreshAdapter(itineraryList);
        }
    }

}

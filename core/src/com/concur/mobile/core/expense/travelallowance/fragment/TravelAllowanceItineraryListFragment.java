package com.concur.mobile.core.expense.travelallowance.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.util.Const;

public class TravelAllowanceItineraryListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener{


	public static final String ON_REFRESH_MSG = "refreshItineraries";

	private String expenseReportKey;

	private SwipeRefreshLayout swipeRefreshLayout;

	private IFragmentCallback callback;

	ListAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		if (bundle != null && bundle.containsKey(Const.EXTRA_EXPENSE_REPORT_KEY)) {
			expenseReportKey = bundle.getString(Const.EXTRA_EXPENSE_REPORT_KEY);
		}

		ConcurCore app = (ConcurCore) getActivity().getApplication();
		TravelAllowanceItineraryController controller = app.getTaItineraryController();

		List<CompactItinerary> compactItinList = controller.getCompactItineraryList();

		adapter = new TravelAllowanceItineraryListAdapter(getActivity(), compactItinList);

		setListAdapter(adapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			callback = (IFragmentCallback) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement IFragementCallback") ;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		callback = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.travel_allowance_itinerary_list, container, false);

		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setOnRefreshListener(this);

		return view;
	}


	@Override
    public void onRefresh() {
		if (callback != null) {
			callback.sendMessage(ON_REFRESH_MSG);
		} else {
			onRefreshFinished();
		}
    }

	public void onRefreshFinished() {
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setRefreshing(false);
			refreshAdapter();
		}
	}

	private void refreshAdapter() {
		ConcurCore app = (ConcurCore) getActivity().getApplication();
		TravelAllowanceItineraryController controller = app.getTaItineraryController();

		adapter =  new TravelAllowanceItineraryListAdapter(getActivity(), controller.getCompactItineraryList());
		setListAdapter(adapter);
	}
}

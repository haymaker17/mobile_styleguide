package com.concur.mobile.core.expense.travelallowance.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;

import com.concur.core.R;

public class TravelAllowanceItineraryListFragment extends ListFragment {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		List<Object> dummyList = new ArrayList<Object>();
		dummyList.add(new String(""));
		dummyList.add(new Object());
		dummyList.add(new Object());
		dummyList.add(new Object());
		dummyList.add(new String(""));
		dummyList.add(new Object());
		dummyList.add(new Object());

		ListAdapter adapter = new TravelAllowanceItineraryListAdapter(getActivity(), dummyList);

		setListAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.travel_allowance_itinerary_list, container, false);
	}


	

}

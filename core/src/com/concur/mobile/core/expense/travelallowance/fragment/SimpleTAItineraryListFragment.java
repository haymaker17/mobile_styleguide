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
import android.widget.PopupMenu;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.activity.AssignableItineraryListActivity;
import com.concur.mobile.core.expense.travelallowance.activity.ItineraryUpdateActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.SimpleItineraryListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.Date;
import java.util.List;

/**
 * Created by D049515 on 27.07.2015.
 */
public class SimpleTAItineraryListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String CLASS_TAG = SimpleTAItineraryListFragment.class.getSimpleName();
    public static final String ON_REFRESH_MSG_ITIN = CLASS_TAG + ".refreshItineraries";
    public static final String ON_REFRESH_MSG_TA = CLASS_TAG + ".refreshAllowances";
    private static final int REQUEST_CODE_UPDATE_ITINERARY = 0x01;

    private boolean expenseReportIsSubmitted;
    private String expenseReportName;
    private String expenseReportKey;
    private Date expenseReportDate;
    private SwipeRefreshLayout swipeRefreshLayout;
    private IFragmentCallback callback;
    private SimpleItineraryListAdapter adapter;
    private List<Itinerary> itineraryList;
    private TravelAllowanceItineraryController itineraryController;

    private RecyclerView recyclerView;

    private PopupMenu menu;

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
                    + " must implement IFragmentCallback");
        }
        if (this.itineraryController == null) {
            ConcurCore app = (ConcurCore) getActivity().getApplication();
            this.itineraryController = app.getTaController().getTaItineraryController();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        super.onDetach();

        this.callback = null;

        if (this.menu != null) {
            this.menu.setOnMenuItemClickListener(null);
            this.menu = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ta_itinerary_simple_list, container, false);

        itineraryList = (List<Itinerary>) getArguments().getSerializable(BundleId.ITINERARY_LIST);

        if (getArguments() != null) {
            this.expenseReportIsSubmitted = getArguments().getBoolean(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false);
            this.expenseReportName = getArguments().getString(BundleId.EXPENSE_REPORT_NAME, StringUtilities.EMPTY_STRING);
            this.expenseReportKey = getArguments().getString(BundleId.EXPENSE_REPORT_KEY, StringUtilities.EMPTY_STRING);
            this.expenseReportDate = (Date) getArguments().getSerializable(BundleId.EXPENSE_REPORT_DATE);
        }

        adapter = new SimpleItineraryListAdapter(getActivity(), itineraryList);
        adapter.setOnClickListener(this);
        adapter.setOnDeleteClickListener(this);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        //this.recyclerView.setHasFixedSize(true);
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

        if (expenseReportIsSubmitted) {
            adapter.setDeleteEnabled(false);
        } else {
            adapter.setDeleteEnabled(true);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            Intent intent = new Intent(getActivity(), AssignableItineraryListActivity.class);
            intent.putExtras(getActivity().getIntent());
            getActivity().startActivityForResult(intent, REQUEST_CODE_UPDATE_ITINERARY);
        } else if (view.getId() == R.id.iv_row_action_icon) {
            int pos = this.recyclerView.getChildAdapterPosition((View) view.getParent());
            Itinerary itinerary = itineraryList.get(pos);
            this.menu = new PopupMenu(getActivity(), view);
            menu.inflate(R.menu.ta_itin_list_row_menu);
            Intent intent = new Intent();
            intent.putExtra(BundleId.ITINERARY, itinerary);
            // Since menuInfo can't be set to a menuItem the Intent is used to pass extra info to the menu item. According to the
            // doc this is OK as long as the menuItem is handled after click. See also onMenuItemClick method.
            menu.getMenu().findItem(R.id.remove).setIntent(intent);
            menu.getMenu().findItem(R.id.delete).setIntent(intent);
            menu.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) getActivity());
            menu.show();
        } else {
            int pos = this.recyclerView.getChildAdapterPosition((View) view.getParent());
            Itinerary itinerary = itineraryList.get(pos);
            Intent intent = new Intent(getActivity(), ItineraryUpdateActivity.class);
            intent.putExtra(BundleId.ITINERARY, itinerary);
            intent.putExtra(BundleId.EXPENSE_REPORT_KEY, expenseReportKey);
            getActivity().startActivityForResult(intent, REQUEST_CODE_UPDATE_ITINERARY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRefresh() {
        if (this.callback != null) {
            this.callback.sendMessage(ON_REFRESH_MSG_ITIN);
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
            if (this.itineraryList == null) {
                this.itineraryList = itineraryController.getItineraryList();
            }
            adapter.refreshAdapter(this.itineraryList);
        }
    }

    public void deleteItinerary(Itinerary itinerary) {
        this.itineraryList.remove(itinerary);
        adapter.refreshAdapter(this.itineraryList);
    }


}

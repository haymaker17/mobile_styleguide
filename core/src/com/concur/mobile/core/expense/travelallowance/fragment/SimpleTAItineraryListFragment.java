package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.activity.ItineraryUpdateActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.SimpleItineraryListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by D049515 on 27.07.2015.
 */
public class SimpleTAItineraryListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, IControllerListener {

    private static final String CLASS_TAG = SimpleTAItineraryListFragment.class.getSimpleName();
    public static final String ON_REFRESH_MSG_ITIN = CLASS_TAG + ".refreshItineraries";
    public static final String ON_REFRESH_MSG_TA = CLASS_TAG + ".refreshAllowances";
    private static final int REQUEST_CODE_UPDATE_ITINERARY = 0x01;

    private boolean expenseReportIsSubmitted;
    private String expenseReportName;
    private String expenseReportKey;
    private SwipeRefreshLayout swipeRefreshLayout;
    private IFragmentCallback callback;
    private SimpleItineraryListAdapter adapter;
    private List<Itinerary> itineraryList;
    private List<Itinerary> itineraryDeletionList;
    private TravelAllowanceItineraryController itineraryController;

    private RecyclerView recyclerView;

    private class ContextMenuRecyclerView extends RecyclerView {
        private RecyclerContextMenuInfo contextMenuInfo;

        public ContextMenuRecyclerView() {
            super(getActivity());
        }

        @Override
        protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
            return contextMenuInfo;
        }

        @Override
        public boolean showContextMenuForChild(View originalView) {
            final int longPressPosition = getChildAdapterPosition(originalView);
            if (longPressPosition >= 0) {
                final long longPressId = getAdapter().getItemId(longPressPosition);
                contextMenuInfo = new RecyclerContextMenuInfo(longPressPosition, longPressId);
                return super.showContextMenuForChild(originalView);
            }
            return false;
        }

        public class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {
            public RecyclerContextMenuInfo(int position, long id) {
                this.position = position;
                this.id = id;
            }
            final public int position;
            final public long id;
        }
    }

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
            this.itineraryController = app.getTaItineraryController();
        }
        if (this.itineraryController != null) {
            this.itineraryController.registerListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        super.onDetach();
        if (this.itineraryController != null) {
            this.itineraryController.unregisterListener(this);
        }
        this.callback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ta_itinerary_simple_list, container, false);

        itineraryList = (List<Itinerary>) getArguments().getSerializable(BundleId.ITINERARY_LIST);
        itineraryDeletionList = new ArrayList<Itinerary>();

        if (getArguments() != null) {
            this.expenseReportIsSubmitted = getArguments().getBoolean(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false);
            this.expenseReportName = getArguments().getString(BundleId.EXPENSE_REPORT_NAME, StringUtilities.EMPTY_STRING);
            this.expenseReportKey = getArguments().getString(BundleId.EXPENSE_REPORT_KEY, StringUtilities.EMPTY_STRING);
        }

        adapter = new SimpleItineraryListAdapter(getActivity(), itineraryList);
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

        View vFooter = view.findViewById(R.id.v_ta_footer);
        if (vFooter != null) {
            if (expenseReportIsSubmitted) {
                vFooter.setVisibility(View.GONE);
            } else {
                Button btnDelete = (Button) vFooter.findViewById(R.id.right_button);
                if (btnDelete != null) {
                    btnDelete.setText(getText(R.string.delete).toString().toUpperCase());
                    btnDelete.setOnClickListener(this);
                }
            }
        }

//        if (expenseReportIsSubmitted) {
//            adapter.setDeleteEnabled(false);
//        } else {
//            adapter.setDeleteEnabled(true);
//        }
        adapter.setDeleteEnabled(false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (this.itineraryList != null && itineraryList.size() != 0 && info.position >= 0 && info.position + 1 < itineraryList.size()) {
            Itinerary itin = this.itineraryList.get(info.position);
            itineraryController.executeDeleteItinerary(itin);
        }
        return true;
    }

        @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            Intent intent = new Intent(getActivity(), ItineraryUpdateActivity.class);
            Itinerary itin = new Itinerary();
            itin.setName(expenseReportName);
            itin.setExpenseReportID(expenseReportKey);
            intent.putExtra(BundleId.ITINERARY, itin);
            intent.putExtra(BundleId.EXPENSE_REPORT_KEY, expenseReportKey);
            getActivity().startActivityForResult(intent, REQUEST_CODE_UPDATE_ITINERARY);
        } else if (view.getId() == R.id.right_button) {
            deleteItineraries();
        }
        else if (view.getId() == R.id.cb_selection) {
            CheckBox cbSelection = (CheckBox) view;
            FloatingActionButton fab = (FloatingActionButton) this.getActivity().findViewById(R.id.fab);
            View vFooter = this.getActivity().findViewById(R.id.v_ta_footer);
            int pos = this.recyclerView.getChildAdapterPosition((View) view.getParent());
            Itinerary itinerary = itineraryList.get(pos);
            if (cbSelection != null) {
                if (cbSelection.isChecked()) {
                    //Currently we only support single deletion, hence we clear the complete list
                    this.itineraryDeletionList = new ArrayList<Itinerary>();
                    this.itineraryDeletionList.add(itinerary);
                    if (fab != null) {
                        fab.setVisibility(View.GONE);
                    }
                    if (vFooter != null) {
                        vFooter.setVisibility(View.VISIBLE);
                    }
                } else {
                    this.itineraryDeletionList.remove(itinerary);
                    if (fab != null) {
                        if (this.itineraryDeletionList.size() == 0) {
                            fab.setVisibility(View.VISIBLE);
                        }
                    }
                    if (vFooter != null) {
                        if (this.itineraryDeletionList.size() == 0) {
                            vFooter.setVisibility(View.GONE);
                        }
                    }
                }
                if (adapter != null) {
                    adapter.setDeletionList(itineraryDeletionList);
                    adapter.notifyDataSetChanged();
                }
            }
        }
        else {
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
            adapter.refreshAdapter(itineraryList);
        }
    }

    /**
     * Deletes all itineraries listed in the corresponding member variable.
     */
    private void deleteItineraries() {
        if (this.itineraryDeletionList == null || this.itineraryDeletionList.size() == 0) {
            return;
        }
        Iterator<Itinerary> itItinerary = itineraryDeletionList.iterator();
        while (itItinerary.hasNext()) {
            Itinerary itinerary = itItinerary.next();
            if (StringUtilities.isNullOrEmpty(itinerary.getItineraryID())) {
                itItinerary.remove(); //transient .. should not be on this UI
                this.itineraryList.remove(itinerary);
            } else {
                itineraryController.executeDeleteItinerary(itinerary);
            }
        }
    }

    @Override
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {
        if (action == ControllerAction.DELETE) {
            if (isSuccess) {
                this.itineraryList = itineraryController.getItineraryList();
                adapter.refreshAdapter(this.itineraryList);
                if (this.getView() != null) {
                    View vFooter = this.getView().findViewById(R.id.v_ta_footer);
                    if (vFooter != null) {
                        vFooter.setVisibility(View.GONE);
                    }
                }
                if (this.callback != null) {//We need to refresh the adjustments as they probably have been removed
                    this.callback.sendMessage(ON_REFRESH_MSG_TA);
                }
                this.itineraryDeletionList = new ArrayList<Itinerary>();
                Toast.makeText(this.getActivity(), R.string.general_delete_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getActivity(), R.string.general_delete_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

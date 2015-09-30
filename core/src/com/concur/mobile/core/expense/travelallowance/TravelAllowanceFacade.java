package com.concur.mobile.core.expense.travelallowance;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.travelallowance.activity.AssignableItineraryListActivity;
import com.concur.mobile.core.expense.travelallowance.activity.TravelAllowanceActivity;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceConfigurationController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;
import com.concur.mobile.core.expense.travelallowance.expensedetails.ExpenseEntryTAFieldFactory;
import com.concur.mobile.core.expense.travelallowance.service.IRequestListener;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This facade should handle the entire travel allowance UI logic for the old concur activities. Basic goal is to leave a very
 * small footprint in the old concur activities.
 *
 * Created by Patricius Komarnicki on 21.08.2015.
 */
public class TravelAllowanceFacade extends Fragment {


    public interface ExpenseEntriesTACallback {
        void enableTAItineraryButton(final Class<?> taStartActivity, final boolean isEditMode, final boolean isInApproval);
        void taDateRefreshFinished();
    }

    public interface ExpenseEntryTACallback {
        void populateTAFields(List<ExpenseReportFormField> expenseReportFormFields);
        void saveTA();
        void saveTAFinished();
    }

    private static final String CLASS_TAG = TravelAllowanceFacade.class.getSimpleName();

    public static final String FRAGMENT_TAG = CLASS_TAG;

    public static final String ARG_EXPENSE_REPORT = CLASS_TAG + ".expense.report";
    public static final String ARG_EXPENSE_REPORT_EXP_DETAIL = CLASS_TAG + ".expense.report.expense.detail";
    public static final String ARG_IS_MANAGER = CLASS_TAG + ".is.manager";


    private ExpenseEntriesTACallback expenseEntriesCallback;
    private ExpenseEntryTACallback expenseEntryCallback;

    private Context context;

    private TravelAllowanceItineraryController itineraryController;
    private FixedTravelAllowanceController fixedTAController;
    private TravelAllowanceConfigurationController configController;

    private ExpenseReport expRep;
    private ExpenseReportEntryDetail expRepEntryDetail;
    private boolean isManager;

    private boolean configRefreshDone = false;
    private boolean itineraryRefreshDone = false;
    private boolean assignableItineraryRefreshDone = false;

    private boolean taUpdateSucceeded = false;

    private boolean isTaSwitchedOn = false;

    private List<ExpenseReportFormField> expenseDetailsTAFormFields;

    private IRequestListener itinRefreshListener = new IRequestListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestSuccess",
                    "Itinerary refresh finished. Is config already done?" + configRefreshDone));
            itineraryRefreshDone();
        }

        @Override
        public void onRequestFailed() {
            Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestFailed",
                    "Itinerary refresh finished. Is config already done?" + configRefreshDone));
            itineraryRefreshDone();
        }
    };


    private IRequestListener assignableItinRefreshListener = new IRequestListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestSuccess",
                    "Assignable Itinerary refresh finished."));
            assignableItineraryRefreshDone();
        }

        @Override
        public void onRequestFailed() {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestFailed",
                    "Assignable Itinerary refresh finished."));
            assignableItineraryRefreshDone();
        }
    };

    private IRequestListener configRefreshListener = new IRequestListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestSuccess",
                    "Config Controller refresh finished. Is itinerary refresh already done?" + itineraryRefreshDone));
            configRefreshDone();
        }

        @Override
        public void onRequestFailed() {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestFailed",
                    "Config Controller refresh finished. Is itinerary refresh already done?" + itineraryRefreshDone));
            configRefreshDone();
        }
    };

    private IRequestListener updateTaListener = new IRequestListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestFailed",
                    "Update fixed TA finished."));
            updateFixedTaDone(true);
        }

        @Override
        public void onRequestFailed() {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestFailed",
                    "Update fixed TA finished."));
            updateFixedTaDone(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle arguments = getArguments();
        if (arguments != null) {
            isManager = arguments.getBoolean(ARG_IS_MANAGER, false);
            expRep = (ExpenseReport) arguments.getSerializable(ARG_EXPENSE_REPORT);
            expRepEntryDetail = (ExpenseReportEntryDetail) arguments.getSerializable(ARG_EXPENSE_REPORT_EXP_DETAIL);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        expenseEntriesCallback = null;
        expenseEntryCallback = null;
        context = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity.getApplicationContext();
        isTaSwitchedOn = ViewUtil.hasTravelAllowanceFixed(context);
        initializeController(context);
        try {
            if (activity instanceof ExpenseEntriesTACallback) {
                expenseEntriesCallback = (ExpenseEntriesTACallback) activity;
            }

            if (activity instanceof ExpenseEntryTACallback) {
                expenseEntryCallback = (ExpenseEntryTACallback) activity;
            }
        } catch (ClassCastException e) {
            Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onAttach", "Class cast exception"));
        }
    }

    private void initializeController(Context context) {
        ConcurCore app = (ConcurCore) context.getApplicationContext();
        if (itineraryController == null) {
            itineraryController = app.getTaController().getTaItineraryController();
        }
        if (fixedTAController == null) {
            fixedTAController = app.getTaController().getFixedTravelAllowanceController();
        }
        if (configController == null) {
            configController = app.getTaController().getTAConfigController();
        }
    }

    public void refreshVisibility() {
        if (!isTaSwitchedOn) {
            // in case TA is switched of in the site settings do nothing.
            return;
        }
        if (expRep == null) {
            Log.e(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                            " expense report key is null"));
            return;
        }

        Log.d(DebugUtils.LOG_TAG_TA,
                DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                        "isManager: " + isManager + ", isSubmitted(): " + expRep.isSubmitted()));

        if (isManager || expRep.isSubmitted()) {
            // In approval case or if report is submitted (for traveler case) only show the button if itineraries are available.
            if (itineraryController.getItineraryList().size() > 0) {
                Log.d(DebugUtils.LOG_TAG_TA,
                        DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                                " Button visible: Approver case and list > 0."));
                if (expenseEntriesCallback != null) {
                    expenseEntriesCallback.enableTAItineraryButton(TravelAllowanceActivity.class, false, isManager);
                }
                return;
            }
        } else {
            // Check the TA configuration whether meals and lodging is switched on
            if (configController != null
                    && configController.getTravelAllowanceConfigurationList() != null) {
                TravelAllowanceConfiguration config = configController.getTravelAllowanceConfigurationList();
                Log.d(DebugUtils.LOG_TAG_TA,
                        DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                                " Button visible: Traveler case employee config: LodgingTat = "
                                        + config.getLodgingTat() + " MealsTat = " + config.getMealsTat()));
                if (TravelAllowanceConfiguration.FIXED.equals(config.getLodgingTat())
                        || TravelAllowanceConfiguration.FIXED.equals(config.getMealsTat())) {


                    if (itineraryController != null && itineraryController.getItineraryList().size() == 0) {
                        if (expenseEntriesCallback != null) {
                            expenseEntriesCallback.enableTAItineraryButton(AssignableItineraryListActivity.class,
                                    true, isManager);
                        }
                    } else {
                        if (expenseEntriesCallback != null) {
                            expenseEntriesCallback.enableTAItineraryButton(TravelAllowanceActivity.class,
                                    true, isManager);
                        }
                    }
                    return;
                }
            }
        }
    }



    public void refreshTaData() {
        if (configController.getTravelAllowanceConfigurationList() == null) {
            configController.refreshConfiguration(configRefreshListener);
        } else {
            configRefreshDone = true;
        }

        fixedTAController.refreshFixedTravelAllowances(expRep.reportKey, null);
        itineraryController.getItineraryList().clear();
        itineraryController.refreshItineraries(expRep.reportKey, isManager, itinRefreshListener);
        itineraryController.refreshAssignableItineraries(expRep.reportKey, assignableItinRefreshListener);
    }

    private synchronized void onRefreshDone() {
        if (configRefreshDone && itineraryRefreshDone && assignableItineraryRefreshDone) {
            configRefreshDone = false;
            itineraryRefreshDone = false;
            assignableItineraryRefreshDone = false;
            refreshVisibility();
            if (expenseEntriesCallback != null) {
                expenseEntriesCallback.taDateRefreshFinished();
            }
        }
    }

    public void setupExpenseEntryTAFields() {
        if ("FXMLS".equals(expRepEntryDetail.expKey) || "FXLDG".equals(expRepEntryDetail.expKey)) {
            ExpenseEntryTAFieldFactory fieldFactory = new ExpenseEntryTAFieldFactory(context, expRepEntryDetail);
            boolean isEditable = !expRep.isSubmitted();
            fieldFactory.setIsEditable(isEditable);
            List<ExpenseReportFormField> frmFlds = fieldFactory.getFormFields();
            if (expenseEntryCallback != null) {
                expenseEntryCallback.populateTAFields(frmFlds);
            }
        }
    }


    public void saveExpenseEntryTA() {
        if (expenseEntryCallback != null) {
            expenseEntryCallback.saveTA();
        }

        if (expenseDetailsTAFormFields == null || expenseDetailsTAFormFields.size() == 0) {
            expenseEntryCallback.saveTAFinished();
            return;
        }

        ConcurCore app = (ConcurCore) context.getApplicationContext();
        boolean isEditable = !expRep.isSubmitted();
        ExpenseEntryTAFieldFactory fieldFactory = new ExpenseEntryTAFieldFactory(context, expRepEntryDetail);
        fieldFactory.setIsEditable(isEditable);

        FixedTravelAllowance ta = fieldFactory.generateFromFormFields(expenseDetailsTAFormFields, expRepEntryDetail.taDayKey);
        List<FixedTravelAllowance> taList = new ArrayList<FixedTravelAllowance>();
        taList.add(ta);

        FixedTravelAllowanceController controller = app.getTaController().getFixedTravelAllowanceController();
        controller.executeUpdate(taList, expRep.reportKey, updateTaListener);

    }

    public void setExpenseDetailsTAFormFields(List<ExpenseReportFormField> formFields) {
        this.expenseDetailsTAFormFields = formFields;
    }


    public boolean isTaUpdateSucceeded() {
        return taUpdateSucceeded;
    }

    public void resetTaUpdateSucceeded() {
        this.taUpdateSucceeded = false;
    }

    private void itineraryRefreshDone() {
        itineraryRefreshDone = true;
        onRefreshDone();
    }


    private void assignableItineraryRefreshDone() {
        assignableItineraryRefreshDone = true;
        onRefreshDone();
    }

    private void configRefreshDone() {
        configRefreshDone = true;
        onRefreshDone();
    }

    private void updateFixedTaDone(boolean isSuccess) {
        this.taUpdateSucceeded = isSuccess;
        if (expenseEntryCallback != null) {
            expenseEntryCallback.saveTAFinished();
        }
        fixedTAController.refreshFixedTravelAllowances(expRep.reportKey, null);
    }
}

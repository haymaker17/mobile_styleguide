package com.concur.mobile.core.expense.travelallowance;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.travelallowance.activity.ItineraryUpdateActivity;
import com.concur.mobile.core.expense.travelallowance.activity.TravelAllowanceActivity;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceConfigurationController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;
import com.concur.mobile.core.expense.travelallowance.expensedetails.ExpenseEntryTAFieldFactory;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * This facade should handle the entire travel allowance UI logic for the old concur activities. Basic goal is to leave a very
 * small footprint in the old concur activities.
 *
 * Created by Patricius Komarnicki on 21.08.2015.
 */
public class TravelAllowanceFacade implements IControllerListener {

    public interface ExpenseEntriesTACallback {
        void enableTAItineraryButton(final Class<?> taStartActivity, final boolean isEditMode);
    }

    public interface ExpenseEntryTACallback {
        void populateTravelAllowanceFields(List<ExpenseReportFormField> expenseReportFormFields);
    }


    private static final String CLASS_TAG = TravelAllowanceFacade.class.getSimpleName();

    private boolean isInApproval;
    private ExpenseReport expRep;


    private TravelAllowanceItineraryController itineraryController;
    private FixedTravelAllowanceController fixedTravelAllowanceController;
    private TravelAllowanceConfigurationController configurationController;

    private boolean configRefreshDone = false;
    private boolean itineraryRefreshDone = false;


    private WeakReference<ExpenseEntriesTACallback> expenseEntriesCallbackReference;
    private WeakReference<ExpenseEntryTACallback> expenseEntryCallbackReference;

    public TravelAllowanceFacade(ExpenseReport expenseReport, boolean isInApproval, TravelAllowanceController taController,
                                 ExpenseEntriesTACallback callback) {
        this.expenseEntriesCallbackReference = new WeakReference<ExpenseEntriesTACallback>(callback);
        this.fixedTravelAllowanceController = taController.getFixedTravelAllowanceController();
        this.itineraryController = taController.getTaItineraryController();
        this.configurationController = taController.getTAConfigController();

        this.expRep = expenseReport;
        this.isInApproval = isInApproval;
    }

    public TravelAllowanceFacade(ExpenseEntryTACallback callback) {
        this.expenseEntryCallbackReference = new WeakReference<ExpenseEntryTACallback>(callback);
    }



    public void refreshVisibility() {
        if (expRep == null) {
            return;
        }
        if (isInApproval || expRep.isSubmitted()) {
            // In approval case or if report is submitted (for traveler case) only show the button if itineraries are available.
            if (itineraryController.getItineraryList().size() > 0) {
                Log.d(DebugUtils.LOG_TAG_TA,
                        DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                                " Button visible: Approver case and list > 0."));
                if (expenseEntriesCallbackReference.get() != null) {
                    expenseEntriesCallbackReference.get().enableTAItineraryButton(TravelAllowanceActivity.class, false);
                }
                return;
            }
        } else {
            // Check the TA configuration whether meals and lodging is switched on
            if (configurationController != null
                    && configurationController.getTravelAllowanceConfigurationList() != null) {
                TravelAllowanceConfiguration config = configurationController.getTravelAllowanceConfigurationList();
                if (TravelAllowanceConfiguration.FIXED.equals(config.getLodgingTat())
                        || TravelAllowanceConfiguration.FIXED.equals(config.getMealsTat())) {
                    Log.d(DebugUtils.LOG_TAG_TA,
                            DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                                    " Button visible: Traveler case employee config: LodgingTat = "
                                            + config.getLodgingTat() + " MealsTat = " + config.getMealsTat()));
                    if (itineraryController.getItineraryList().size() == 0) {
                        if (expenseEntriesCallbackReference.get() != null) {
                            expenseEntriesCallbackReference.get().enableTAItineraryButton(ItineraryUpdateActivity.class,
                                    true);
                        }
                    } else {
                        if (expenseEntriesCallbackReference.get() != null) {
                            expenseEntriesCallbackReference.get().enableTAItineraryButton(TravelAllowanceActivity.class,
                                    true);
                        }
                    }
                    return;
                }
            }
        }
    }
    

    @Override
    public synchronized void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {
        if (controller instanceof TravelAllowanceItineraryController && action == ControllerAction.REFRESH) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                    "Itinerary Controller refresh finished. Is config already done?" + configRefreshDone));
            itineraryRefreshDone = true;
            TravelAllowanceItineraryController itinController = (TravelAllowanceItineraryController) controller;
            itinController.unregisterListener(this);

            if (configRefreshDone) {
                refreshVisibility();
            }

        }

        if (controller instanceof TravelAllowanceConfigurationController && action == ControllerAction.REFRESH) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                    "Config Controller refresh finished. Is itinerary refresh already done?" + itineraryRefreshDone));
            configRefreshDone = true;
            TravelAllowanceConfigurationController configurationController = (TravelAllowanceConfigurationController) controller;
            configurationController.unregisterListener(this);

            if (itineraryRefreshDone) {
                refreshVisibility();
            }
        }
    }

    public void refreshTaData() {
        if (configurationController.getTravelAllowanceConfigurationList() == null) {
            configurationController.registerListener(this);
            configurationController.refreshConfiguration();
        } else {
            configRefreshDone = true;
        }

        fixedTravelAllowanceController.refreshFixedTravelAllowances(this.expRep.reportKey);
        itineraryController.getItineraryList().clear();
        itineraryController.registerListener(this);
        itineraryController.refreshItineraries(expRep.reportKey, isInApproval);
    }

    public void setupExpenseEntryTAFields(Context context, ExpenseReportEntryDetail expRepEntDet) {
            if (expRepEntDet.expKey.equals("FXMLS") || expRepEntDet.expKey.equals("FXLDG")) {
                ExpenseEntryTAFieldFactory fieldFactory = new ExpenseEntryTAFieldFactory(context,
                        expRepEntDet);
                List<ExpenseReportFormField> frmFlds = fieldFactory.getFormFields();
                if (expenseEntryCallbackReference.get() != null) {
                    expenseEntryCallbackReference.get().populateTravelAllowanceFields(frmFlds);
                }
            }
    }


}

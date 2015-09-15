package com.concur.mobile.core.expense.travelallowance;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.travelallowance.activity.AssignableItineraryListActivity;
import com.concur.mobile.core.expense.travelallowance.activity.ItineraryUpdateActivity;
import com.concur.mobile.core.expense.travelallowance.activity.TravelAllowanceActivity;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceConfigurationController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;
import com.concur.mobile.core.expense.travelallowance.expensedetails.ExpenseEntryTAFieldFactory;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This facade should handle the entire travel allowance UI logic for the old concur activities. Basic goal is to leave a very
 * small footprint in the old concur activities.
 *
 * Created by Patricius Komarnicki on 21.08.2015.
 */
public class TravelAllowanceFacade implements IControllerListener {


    public interface ExpenseEntriesTACallback {
        void enableTAItineraryButton(final Class<?> taStartActivity, final boolean isEditMode, final boolean isInApproval);

        void taDateRefreshFinished();
    }

    public interface ExpenseEntryTACallback {
        void populateTravelAllowanceFields(List<ExpenseReportFormField> expenseReportFormFields);
    }

    public interface SaveExpenseEntryTACallback {
        void saveTA();
        void saveTAFinished();
    }


    private static final String CLASS_TAG = TravelAllowanceFacade.class.getSimpleName();

    private boolean isInApproval;
    private ExpenseReport expRep;


    private TravelAllowanceItineraryController itineraryController;
    private FixedTravelAllowanceController fixedTravelAllowanceController;
    private TravelAllowanceConfigurationController configurationController;

    private boolean configRefreshDone = false;
    private boolean itineraryRefreshDone = false;

    private List<ExpenseReportFormField> expenseDetailsTAFormFields;

    private boolean taUpdateSucceeded = false;

    private ExpenseEntriesTACallback expenseEntriesCallbackReference;
    private ExpenseEntryTACallback expenseEntryCallbackReference;
    private SaveExpenseEntryTACallback saveExpenseEntryTACallbackReference;


    public TravelAllowanceFacade(ExpenseReport expenseReport, boolean isInApproval, TravelAllowanceController taController,
                                 ExpenseEntriesTACallback callback) {
        this.expenseEntriesCallbackReference = callback;
        this.fixedTravelAllowanceController = taController.getFixedTravelAllowanceController();
        this.itineraryController = taController.getTaItineraryController();
        this.configurationController = taController.getTAConfigController();

        this.expRep = expenseReport;
        this.isInApproval = isInApproval;
    }

    public TravelAllowanceFacade(ExpenseEntryTACallback callback) {
        this.expenseEntryCallbackReference = callback;
    }



    public void refreshVisibility() {
        if (expRep == null) {
            Log.d(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                            " expense report is null"));
            return;
        }

        Log.d(DebugUtils.LOG_TAG_TA,
                DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                        "isInApproval: " + isInApproval + ", isSubmitted(): " + expRep.isSubmitted()));

        if (isInApproval || expRep.isSubmitted()) {
            // In approval case or if report is submitted (for traveler case) only show the button if itineraries are available.
            if (itineraryController.getItineraryList().size() > 0) {
                Log.d(DebugUtils.LOG_TAG_TA,
                        DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                                " Button visible: Approver case and list > 0."));
                if (expenseEntriesCallbackReference != null) {
                    expenseEntriesCallbackReference.enableTAItineraryButton(TravelAllowanceActivity.class, false, isInApproval);
                }
                return;
            }
        } else {
            // Check the TA configuration whether meals and lodging is switched on
            if (configurationController != null
                    && configurationController.getTravelAllowanceConfigurationList() != null) {
                TravelAllowanceConfiguration config = configurationController.getTravelAllowanceConfigurationList();
                Log.d(DebugUtils.LOG_TAG_TA,
                        DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                                " Button visible: Traveler case employee config: LodgingTat = "
                                        + config.getLodgingTat() + " MealsTat = " + config.getMealsTat()));
                if (TravelAllowanceConfiguration.FIXED.equals(config.getLodgingTat())
                        || TravelAllowanceConfiguration.FIXED.equals(config.getMealsTat())) {

                    if (expenseEntriesCallbackReference == null) {
                        Log.d(DebugUtils.LOG_TAG_TA,
                                DebugUtils.buildLogText(CLASS_TAG, "refreshVisibility",
                                        "callback: " + expenseEntriesCallbackReference));
                    }


                    if (itineraryController != null && itineraryController.getItineraryList().size() == 0) {
                        if (expenseEntriesCallbackReference != null) {
                            expenseEntriesCallbackReference.enableTAItineraryButton(AssignableItineraryListActivity.class,
                                    true, isInApproval);
                        }
                    } else {
                        if (expenseEntriesCallbackReference != null) {
                            expenseEntriesCallbackReference.enableTAItineraryButton(TravelAllowanceActivity.class,
                                    true, isInApproval);
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
                if (expenseEntriesCallbackReference != null) {
                    expenseEntriesCallbackReference.taDateRefreshFinished();
                }
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
                if (expenseEntriesCallbackReference != null) {
                    expenseEntriesCallbackReference.taDateRefreshFinished();
                }
            }
        }

        if (controller instanceof FixedTravelAllowanceController && action == ControllerAction.UPDATE) {
            controller.unregisterListener(this);
            SaveExpenseEntryTACallback saveTACallback = saveExpenseEntryTACallbackReference;
            this.taUpdateSucceeded = isSuccess;
            if (saveTACallback != null) {
                saveTACallback.saveTAFinished();
            }
            ((FixedTravelAllowanceController) controller).refreshFixedTravelAllowances(expRep.reportKey, null);
        }
    }

    public void refreshTaData() {
        if (configurationController.getTravelAllowanceConfigurationList() == null) {
            configurationController.registerListener(this);
            configurationController.refreshConfiguration();
        } else {
            configRefreshDone = true;
        }

        fixedTravelAllowanceController.refreshFixedTravelAllowances(this.expRep.reportKey, null);
        itineraryController.getItineraryList().clear();
        itineraryController.registerListener(this);
        itineraryController.refreshItineraries(expRep.reportKey, isInApproval, null);
    }

    public void setupExpenseEntryTAFields(Context context, ExpenseReport expenseReport, ExpenseReportEntryDetail expRepEntDet) {
            if (expRepEntDet.expKey.equals("FXMLS") || expRepEntDet.expKey.equals("FXLDG")) {
                ExpenseEntryTAFieldFactory fieldFactory = new ExpenseEntryTAFieldFactory(context,
                        expRepEntDet);
                boolean isEditable = !expenseReport.isSubmitted();
                fieldFactory.setIsEditable(isEditable);
                List<ExpenseReportFormField> frmFlds = fieldFactory.getFormFields();
                if (expenseEntryCallbackReference != null) {
                    expenseEntryCallbackReference.populateTravelAllowanceFields(frmFlds);
                }
            }
    }


    public void setSaveExpenseEntryTACallback (SaveExpenseEntryTACallback callback) {
        this.saveExpenseEntryTACallbackReference = callback;
    }

    public void saveExpenseEntryTA(Context context, ExpenseReport expenseReport, ExpenseReportEntryDetail expRepEntDet) {
        this.expRep = expenseReport;
        SaveExpenseEntryTACallback saveCallback = saveExpenseEntryTACallbackReference;
        if (saveCallback != null) {
            saveCallback.saveTA();
        }

        if (expenseDetailsTAFormFields == null || expenseDetailsTAFormFields.size() == 0) {
            saveCallback.saveTAFinished();
            return;
        }

        ConcurCore app = (ConcurCore) context.getApplicationContext();
        boolean isEditable = !expenseReport.isSubmitted();
        ExpenseEntryTAFieldFactory fieldFactory = new ExpenseEntryTAFieldFactory(context, expRepEntDet,
                app.getTaController().getFixedTravelAllowanceController());
        fieldFactory.setIsEditable(isEditable);

        FixedTravelAllowance ta = fieldFactory.generateFromFormFields(expenseDetailsTAFormFields, expRepEntDet.taDayKey);
        List<FixedTravelAllowance> taList = new ArrayList<FixedTravelAllowance>();
        taList.add(ta);

        FixedTravelAllowanceController controller = app.getTaController().getFixedTravelAllowanceController();
        controller.registerListener(this);
        controller.executeUpdate(taList, expRepEntDet.rptKey);

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
}

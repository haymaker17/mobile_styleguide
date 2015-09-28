package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;

/**
 * This is the main controller for travel allowance. This controller includes several sub controller wich handles the TA
 * configuration, TA itineraries and fixed allowances.
 * 
 * Created by Patricius Komarnicki on 28.08.2015.
 */
public class TravelAllowanceController {

    private TravelAllowanceItineraryController taItineraryController;
    private FixedTravelAllowanceController fixedTravelAllowanceController;
    private TravelAllowanceConfigurationController taConfigController;

    private Context context;

    public TravelAllowanceController(Context context) {
        this.context = context;
//        initializeTaConfig();
    }

    private void initializeTaConfig() {
        if (getTAConfigController().getTravelAllowanceConfigurationList() == null) {
            getTAConfigController().refreshConfiguration(null);
        }
    }

    public TravelAllowanceItineraryController getTaItineraryController() {
        if (taItineraryController == null) {
            taItineraryController = new TravelAllowanceItineraryController(context);
        }
        return taItineraryController;
    }

    /**
     * Creates an instance of a {@link FixedTravelAllowanceController}
     * @return The controller
     */
    public FixedTravelAllowanceController getFixedTravelAllowanceController() {
        if (this.fixedTravelAllowanceController == null) {
            this.fixedTravelAllowanceController = new FixedTravelAllowanceController(context);
        }
        return this.fixedTravelAllowanceController;
    }

    /**
     * Creates an instance of a {@link TravelAllowanceConfigurationController}
     * @return The controller
     */
    public TravelAllowanceConfigurationController getTAConfigController() {
        if (this.taConfigController == null) {
            this.taConfigController = new TravelAllowanceConfigurationController(context);
        }
        return this.taConfigController;
    }

}
